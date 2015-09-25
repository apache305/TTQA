package Main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import Util.ComUtil;
import Util.ModelComFunc;
import Util.TEMModelSampling.modelparameters;

/**
 * Class for Topic Expertise Model
 * 
 * Main properties: 1. Each user has an expertise distribution on each topic 2.
 * Topic label, expertise label and vote score are on post level 3. Vote score
 * is generated by a Gaussian distribution 4. Each topic has a word distribution
 * and tag distribution
 * 
 */

public class TEMModel extends LDABasedModel  {

	public FGMM fgmm;
	float[][] GMMData; // GMM data

	private static final long serialVersionUIdD = 1L;

	public boolean debug = false;// for convenience of debug
	public int U, K, ENum, TagNum, V, S;// total number of users, topics,
											// expertise, tags, terms,
											// votes(score)

	// dirichlet priors
	public float alpha;// user specific topic distribution
	public float beta;// topic user specific expertise distribution;
	public float eta;// topic specific tag distribution
	public float gamma;// topic expertise specific word distribution
	public float xi;// expertise specific vote distribution

	// model parameters
	public float[][] theta;// U*K
	public float[][][] phi;// K*U*E
	public float[][] psi;// K*T
	public float[][][] varphi;// K*E*V
	public float[][] tau;// E*S
	private int Z[][];// U*N topic label for each post
	private int E[][];// U*N expertise label for each post

	// Temporary count variables while sampling
	private int CUK[][]; // U*K
	private int CUKsum[]; // U
	private int CKUE[][][]; // K*U*E
	private int CKUEsum[][]; // K*U
	private int CKT[][]; // K*T  //tag
	private int CKTsum[]; // K
	private int CKV[][]; // K*V
	private int CKVsum[]; // K
	// private int CES[][]; // E*S
	// private int CESsum[]; // E

	//private int iterations;// times of iterations
	private int saveStep;// number of iterations between two saving
	private int beginSaveIters;// begin save model at this iteration
	
	DataWoker trainSet=null;
	DataWoker testSet=null;
	
	int iterNum=0;

	

	public TEMModel(DataWoker trainUsers, DataWoker testUsers,int iternum) {
		super.trainSet=trainUsers;
		super.testSet=testUsers;

		this.trainSet=trainUsers;
		this.testSet=testUsers;
		this.iterNum=iternum;
		K = 30;
		alpha =  50.0f /(float)K ;
		beta = 0.01f ;
		gamma = 0.01f;
		eta = 0.001f;
		xi = 0.2f;//what is this.
		ENum = 10 ;
		

	}

	public void initModel() {
		
		this.U= this.trainSet.users.size();//number of user.
//		this.T= this.trainSet.timeCountMap.size();//number of time label
		this.TagNum= this.trainSet.tagCountMap.size();//number of tag
		this.V= this.trainSet.termCountMap.size();//number of words
		this.ENum= 10;//this.trainSet.voteStep;//number of expertise.//
		this.S= this.trainSet.voteCountMap.size(); //vote??
		


		

		//V = docSet.indexToTermMap.size();//. term map, 
		//S = docSet.indexToVoteMap.size();// vote map

		// model parameters
		theta = new float[U][K];// U*K
		phi = new float[K][U][ENum];// K*U*E
		psi = new float[K][TagNum];// K*T
		varphi = new float[K][1][V];// K*E*V
		tau = new float[ENum][S];// E*S

		// temporary count variables while sampling
		CUK = new int[U][K]; // U*K
		CUKsum = new int[U]; // U
		CKUE = new int[K][U][ENum]; // K*U*E
		CKUEsum = new int[K][U]; // K*U
		CKT = new int[K][TagNum]; // K*T
		CKTsum = new int[K]; // K
		CKV = new int[K][V]; // K*E*V
		CKVsum = new int[K]; // K*E
		// CES = new int[ENum][S]; // E*S
		// CESsum = new int[ENum]; // E

		// initialize topic and expertise index
		Z = new int[U][];// U*N topic label for each post
		E = new int[U][];// U*N expertise label for each post
		for (int u = 0; u < this.U; u++) {
			//Z[u] = new int [   docSet.docs.get(u).docWords.length];

			Z[u]  = new int [ this.trainSet.users.get(u).answerPosts.size()];// one topic for one post
			E[u]  = new int [this.trainSet.users.get(u).answerPosts.size()];//one experitse for one post
			//E[u] = new int[docSet.docs.get(u).docWords.length];
			for (int n = 0; n < this.trainSet.users.get(u).answerPosts.size(); n++) {//for each post
				AnswerPost eachPost=this.trainSet.users.get(u).answerPosts.get(n);
				if (eachPost!= null) {// the n-th post
					// randomly assign initial topic and expertise index
					int initTopic = (int) (Math.random() * K);
					int initExpert = (int) (Math.random() * ENum);
					Z[u][n] = initTopic;
					E[u][n] = initExpert;

					CUK[u][initTopic]++;
					CUKsum[u]++;
					CKUE[initTopic][u][initExpert]++; // K*U*E
					CKUEsum[initTopic][u]++; // K*U
					
					
					//for each tag
					for(int tagID:eachPost.tags){
						this.CKT[initTopic][tagID]++;
						this.CKTsum[initTopic]++;
					}
					
					//for each word
					for(int wordID:eachPost.words){
						this.CKV[initTopic][wordID]++;
						this.CKVsum[initTopic]++;
					}
					
				}
			}
		}

		initGMM( ENum);
	}

	private void initGMM( int expertNum) {
		// get data for GMM
		fgmm = new FGMM();

		for (int u = 0; u < this.U; u++) {
			//for each post.
			for (int n = 0; n < this.trainSet.users.get(u).answerPosts.size(); n++) {
				AnswerPost eachPost=this.trainSet.users.get(u).answerPosts.get(n);
				if (eachPost!= null)
					fgmm.idmap.put(u + "_" + n, fgmm.idmap.size());
			}
		}
		GMMData = new float[fgmm.idmap.size()][1];
		int[] clusterids = new int[fgmm.idmap.size()];
		int count = 0;
		for (int u = 0; u < this.U; u++) {
			for (int n = 0; n < this.trainSet.users.get(u).answerPosts.size(); n++) {
				AnswerPost eachPost=this.trainSet.users.get(u).answerPosts.get(n);
				if (eachPost!= null){
					//float vote = Float.parseFloat(docSet.indexToVoteMap.get(docSet.docs.get(u).votes[n])) + 0f;
					// System.out.println(vote);
					float vote= (float)eachPost.score;
					GMMData[count][0] = vote;
					clusterids[count] = E[u][n];
					count++;
				}
			}
		}
		fgmm.init2(GMMData, expertNum, clusterids);
	}

	public void trainModel(){
		
		for (int i = 0; i < this.iterNum; i++) {
			if(i%10==0){
				System.out.println(String.format("Round:%d", i));
			}
			//
			if (i % 1 == 0) {
				System.out.print("\t");
				for (int k = 0; k < fgmm.ksize; k++)
					System.out.print(fgmm.clusterDataIndex.get(k).size() + " ");
				System.out.println();
				System.out.println("lambda:");
				System.out.print("\t");
				for (int k = 0; k < fgmm.ksize; k++)
					ComUtil.print(fgmm.p_lambda[k], " ", "\n\t");
				System.out.println("mu:");
				System.out.print("\t");
				for (int k = 0; k < fgmm.ksize; k++)
					ComUtil.print(fgmm.p_mu[k], " ", "\n\t");
			}
			System.out.println();
			System.out.println("Iteration " + i);
			/*if ((i >= beginSaveIters)
					&& (((i - beginSaveIters) % saveStep) == 0)) {
				// Saving the model
				System.out.println("Saving model at iteration " + i + " ... ");
				// Firstly update parameters
				this.updateEstimatedParameters();
				// Secondly print model variables
				// saveIteratedModel(i, docSet, minPostNum);
			}*/
			

			if (i % 50 == 0) {
				if (!ModelComFunc.checkEqual(CUK, CUKsum, "CUK")
						|| !ModelComFunc.checkEqual(CKUE, CKUEsum, "CKUE")
						|| !ModelComFunc.checkEqual(CKT, CKTsum, "CKT")
						|| !ModelComFunc.checkEqual(CKV, CKVsum, "CKV")
				// || !ModelComFunc.checkEqual(CES, CESsum, "CES")
				) {
					try {
						System.err.println("Model check equal error !");
						System.exit(0);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			// Use Gibbs Sampling to update Z[][][] and E[][][]
			for (int u = 0; u < this.U; u++) {
				//if (u % 100 == 0)
					//System.out.println("\tNow u = " + u);
				for (int n = 0; n <  this.trainSet.users.get(u).answerPosts.size() ; n++) {
					// System.out.println("\tNow u: " + u + "\tn: " + n);
					AnswerPost a=this.trainSet.users.get(u).answerPosts.get(n);
					if (a != null)
						sampleTopicZandExpertE(a, u, n);
					// for (int l = 0; l <
					// docSet.docs.get(u).docWords[n].length; l++) {
					// sampleTopicZandExpertE(docSet, u, n, l);
					// }
				}
			}
		}
		System.out.println("Final Results");
		System.out.print("\t");
		for (int k = 0; k < fgmm.ksize; k++)
			System.out.print(fgmm.clusterDataIndex.get(k).size() + " ");
		System.out.println();
		System.out.println("lambda:");
		System.out.print("\t");
		for (int k = 0; k < fgmm.ksize; k++)
			ComUtil.print(fgmm.p_lambda[k], " ", "\n\t");
		System.out.println("mu:");
		System.out.print("\t");
		for (int k = 0; k < fgmm.ksize; k++)
			ComUtil.print(fgmm.p_mu[k], " ", "\n\t");
	}

	private void sampleTopicZandExpertE(AnswerPost a, int u, int n) {
		// get unique terms and tags
		ArrayList<Integer> UniqueWords = new ArrayList<Integer>();
		ArrayList<Integer> wCounts = new ArrayList<Integer>();
		ArrayList<Integer> UniqueTags = new ArrayList<Integer>();
		ArrayList<Integer> tCounts = new ArrayList<Integer>();
		ComUtil.uniqe(a.words, UniqueWords, wCounts);
		ComUtil.uniqe(a.tags, UniqueTags, tCounts);

		// decrease counts
		int oldTopic = Z[u][n];
		int oldExpert = E[u][n];
		CUK[u][oldTopic]--;// U*K
		CUKsum[u]--;// U
		CKUE[oldTopic][u][oldExpert]--; // K*U*E
		CKUEsum[oldTopic][u]--; // K*U

		for (int l = 0; l < a.words.size(); l++) {
			int term = a.words.get(l);
			CKV[oldTopic][term]--; // K*V
			CKVsum[oldTopic]--; // K
		}
		for (int l = 0; l < a.tags.size(); l++) {
			int tag = a.tags.get(l);
			CKT[oldTopic][tag]--; // K*T
			CKTsum[oldTopic]--; // K
		}

		// compute probability based on Gibbs Updating Rule
		// sets store all the combinations of E and Z
		// then we can compute probability for all the combinations
		// of E and Z and sample e and z based on the probability
		ArrayList<int[]> sets = new ArrayList<int[]>();
		for (int i = 0; i < K; i++) {
			for (int j = 0; j < ENum; j++) {
				int[] tmp = new int[2];
				tmp[0] = i;
				tmp[1] = j;
				sets.add(tmp);
			}
		}

		// get GMM data index
		int n_pos = fgmm.idmap.get(u + "_" + n);
		double[] probsGMM = fgmm.LearnProbs(GMMData, n_pos);

		double[] p = new double[sets.size()];
		int[] overflow = new int[sets.size()];
		for (int i = 0; i < sets.size(); i++) {
			int z = sets.get(i)[0];
			int e = sets.get(i)[1];
			// p[i] = (CKV[z][term] + gamma) / (CKVsum[z] + gamma * V)
			// * (CUK[u][z] + alpha) / (CUKsum[u] + alpha * K)
			// * (CKUE[z][u][e] + beta)
			// / (CKUEsum[z][u] + beta * ENum) * (CKT[z][tag] + eta)
			// / (CKTsum[z] + eta * TagNum);
			// // * (CES[e][vote] + xi) / (CESsum[e] + xi * S);

			p[i] = (CUK[u][z] + alpha) / (CUKsum[u] + alpha * K)
					* (CKUE[z][u][e] + beta) / (CKUEsum[z][u] + beta * ENum);

			for (int m1 = 0; m1 < UniqueWords.size(); m1++) {
				int count = 0;
				for (int m2 = 0; m2 < wCounts.get(m1); m2++) {
					int word = UniqueWords.get(m1);
					p[i] *= (CKV[z][word] + gamma + m2)
							/ (CKVsum[z] + gamma * V + count);
					count++;
					p[i] = ModelComFunc.checkDoubleOverflow(p[i], i, overflow);
				}
			}

			for (int m1 = 0; m1 < UniqueTags.size(); m1++) {
				int count = 0;
				for (int m2 = 0; m2 < tCounts.get(m1); m2++) {
					int tag = UniqueTags.get(m1);
					p[i] *= (CKT[z][tag] + eta + m2)
							/ (CKVsum[z] + eta * TagNum + count);
					count++;
					p[i] = ModelComFunc.checkDoubleOverflow(p[i], i, overflow);
				}
			}

			// avoid probsGMM[e] overflow
			probsGMM[e] = ModelComFunc.checkDoubleOverflow(probsGMM[e], i,
					overflow);
			p[i] *= probsGMM[e];
		}

		ModelComFunc.reAssignP(p, overflow);
		int newNo = ComUtil.sample(p, p.length);

		// Add new topic and expert label for w_{u, n, l}
		int newTopic = sets.get(newNo)[0];
		int newExpert = sets.get(newNo)[1];
		// System.out.println("now for (u,n,l) = " + u + "\t" + n + "\t" + l);
		// System.out.println("newTopic and new Expertise are: " + newTopic +
		// "\t" + newExpert);

		// update new mu and lambda
		fgmm.UpdateProbs(GMMData, n_pos, newExpert);

		// increase counts
		Z[u][n] = newTopic;
		E[u][n] = newExpert;
		CUK[u][newTopic]++;// U*K
		CUKsum[u]++;// U
		CKUE[newTopic][u][newExpert]++; // K*U*E
		CKUEsum[newTopic][u]++; // K*U
		
		

		for (int l = 0; l < a.words.size(); l++) {
			int term =a.words.get(l);
			CKV[newTopic][term]++; // K*V
			CKVsum[newTopic]++; // K
		}
		for (int l = 0; l < a.tags.size(); l++) {
			int tag = a.tags.get(l);
			CKT[newTopic][tag]++; // K*T
			CKTsum[newTopic]++; // K
		}
	}

	public void estimateProb() {
		for (int u = 0; u < U; u++) {
			for (int k = 0; k < K; k++) {
				theta[u][k] = (CUK[u][k] + alpha) / (CUKsum[u] + alpha * K);
			}
		}

		for (int k = 0; k < K; k++) {
			for (int u = 0; u < U; u++) {
				for (int e = 0; e < ENum; e++) {
					phi[k][u][e] = (CKUE[k][u][e] + beta)
							/ (CKUEsum[k][u] + beta * ENum);
				}
			}
		}

		for (int k = 0; k < K; k++) {
			for (int tag = 0; tag < TagNum; tag++) {
				psi[k][tag] = (CKT[k][tag] + eta) / (CKTsum[k] + eta * TagNum);
			}
		}

		for (int k = 0; k < K; k++) {
			for (int e = 0; e < 1; e++) {
				for (int term = 0; term < V; term++) {
					varphi[k][e][term] = (CKV[k][term] + gamma)
							/ (CKVsum[k] + gamma * V);
				}
			}
		}

		// for (int e = 0; e < ENum; e++) {
		// for (int vote = 0; vote < S; vote++) {
		// tau[e][vote] = (CES[e][vote] + xi) / (CESsum[e] + xi * S);
		// }
		// }
	}

	/*public void saveIteratedModel(int iteration, Documents docSet,
			String minPostNum) throws IOException {
		// model.params model.theta model.phi model.psi model.varphi model.tau
		String resPath = PathConfig.modelResPath + "USER" + minPostNum
				+ "/model_" + iteration;
		FileUtil.write2DArray(theta, resPath + ".theta");
		FileUtil.write3DArray(phi, resPath + ".phi");
		FileUtil.write2DArray(psi, resPath + ".psi");
		// FileUtil.write3DArray(varphi, resPath + ".varphi"); //Currently don't
		// store varphi which is too big
		//FileUtil.write2DArray(tau, resPath + ".tau");

		// model.zassign

		// model.eassign

		int topNum = 30;
		// model.zterms
		ArrayList<String> zetermsLines = new ArrayList<String>();
		for (int i = 0; i < K; i++) {
			for (int j = 0; j < 1; j++) {
				List<Integer> tWordsIndexArray = new ArrayList<Integer>();
				for (int w = 0; w < V; w++) {
					tWordsIndexArray.add(new Integer(w));
				}
				Collections.sort(tWordsIndexArray,
						new TEMModel.TwordsComparable(varphi[i][j]));
				String line = "topic=" + i + "\t";
				for (int w = 0; w < topNum; w++) {
					line += docSet.indexToTermMap.get(tWordsIndexArray.get(w))
							+ "\t";
				}
				zetermsLines.add(line);
			}
		}
		FileUtil.writeLines(resPath + ".zterms", zetermsLines);

		// // model.evotes
		// ArrayList<String> evotesLines = new ArrayList<String>();
		// for (int i = 0; i < ENum; i++) {
		// List<Integer> tWordsIndexArray = new ArrayList<Integer>();
		// for (int s = 0; s < S; s++) {
		// tWordsIndexArray.add(new Integer(s));
		// }
		// Collections.sort(tWordsIndexArray, new TEMModel.TwordsComparable(
		// tau[i]));
		// String line = "expert=" + i + "\t";
		// for (int s = 0; s < topNum; s++) {
		// line += docSet.indexToVoteMap.get(tWordsIndexArray.get(s))
		// + "\t";
		// }
		// evotesLines.add(line);
		// }
		// FileUtil.writeLines(resPath + ".evotes", evotesLines);

		// model.ztags
		ArrayList<String> ztagsLines = new ArrayList<String>();
		for (int i = 0; i < K; i++) {
			List<Integer> tWordsIndexArray = new ArrayList<Integer>();
			for (int t = 0; t < TagNum; t++) {
				tWordsIndexArray.add(new Integer(t));
			}
			Collections.sort(tWordsIndexArray, new TEMModel.TwordsComparable(
					psi[i]));
			String line = "topic=" + i + "\t";
			for (int t = 0; t < topNum; t++) {
				line += docSet.indexToTagMap.get(tWordsIndexArray.get(t))
						+ "\t";
			}
			ztagsLines.add(line);
		}
		FileUtil.writeLines(resPath + ".ztags", ztagsLines);
	}*/

	public class TwordsComparable implements Comparator<Integer> {
		public float[] sortProb; // Store probability of each word in topic k

		public TwordsComparable(float[] sortProb) {
			this.sortProb = sortProb;
		}

		@Override
		public int compare(Integer o1, Integer o2) {
			// Sort topic word index according to the probability of each word
			// in topic k
			if (sortProb[o1] > sortProb[o2])
				return -1;
			else if (sortProb[o1] < sortProb[o2])
				return 1;
			else
				return 0;
		}
	}
	public ArrayList<ArrayList<String>> getTopWords(){
		//from trainset.
		ArrayList<ArrayList<String>> topicTopWords=new ArrayList<ArrayList<String>>();

		for(int kid=0;kid<this.K;kid++){
			topicTopWords.add(new ArrayList<String>());
			ArrayList<Map.Entry<String, Float>> dp= new ArrayList<Map.Entry<String, Float>>();
			for(int wid=0;wid<this.V;wid++){
				String word=this.trainSet.indexToTermMap.get(wid);
				//AbstractMap.SimpleEntry<String, Integer>("exmpleString", 42);
				Map.Entry<String, Float> pairs =new  AbstractMap.SimpleEntry<String , Float> (word,this.varphi[kid][0][wid]);
				dp.add(pairs);
			}
			Collections.sort(dp, new Comparator<Entry<String,Float>>(){
				public int compare(Entry<String, Float> arg0,Entry<String, Float> arg1) {
					// TODO Auto-generated method stub
					return -1*arg0.getValue().compareTo(arg1.getValue());
				}
			});
			for(int i=0;i<10;i++){
				//only output top 10;
				topicTopWords.get(kid).add(dp.get(i).getKey());
				//writer.write(String.format("%s:%f\t", dp.get(i).getKey(),dp.get(i).getValue()));
			}
			//writer.write("\n");
		}
		return topicTopWords;
		
		
	}
	
	public void computeCoherence( DataWoker dataset){
		ArrayList<ArrayList<String>> topicTopWords= this.getTopWords();
		double total_score=0.0;
		double item=0;
		for(int kid=0;kid<this.K;kid++){
			for(int i=0;i<10;i++){
				String w1=topicTopWords.get(kid).get(i);
				if(!dataset.termCountMap.containsKey(w1)){
					continue;
				}
				int wid1=dataset.termToIndexMap.get(w1);
				int occ1=dataset.singleOccDocument[wid1];//number of document has word1.
				//System.out.println(w1+";"+occ1);
				for(int j=i+1;j<10;j++){
					
					String w2=topicTopWords.get(kid).get(j);
					if(!dataset.termCountMap.containsKey(w2)){
						continue;
					}
	
					int wid2=dataset.termToIndexMap.get(w2);
					int cooc12=dataset.coOccDocument[wid1][wid2]+dataset.coOccDocument[wid2][wid1];
						
					double score= Math.log( ((double)cooc12 + 1.0) / ( (double)(occ1)) );

					total_score+=score;
					item++;
					
					
					//System.out.println(score+";"+total_score);
				}
			}
		}
		System.out.println("item size:"+item);
		System.out.println("average coherence score:"+total_score/item);
		
		
		
	}
	
	
	
	public void computePer(Set<String> filterPostId){
		//p(tag) or p(word) = p(k|u)p(k|v)
		
		//test dataset =
		
		double  total_result=0.0;
		int post_number=0;
		int tag_number=0;
		int word_number=0;
		double final_perplex=0.0;
		int 	UinTrain = 0;
		for (User u : this.testSet.users){
			int uid=0;
			if ( !this.trainSet.useridToIndex.containsKey(u.userId) ){
				continue;
			}
			UinTrain++;
			uid=this.trainSet.useridToIndex.get(u.userId);
			//System.out.println(u.userId);
			//System.out.println(u.answerPosts.size());
			
			for(AnswerPost eachPost: u.answerPosts){
				
				//ArrayList<Integer> faketags= eachPost.tags;
				ArrayList<Integer> fakewords=eachPost.words;
				String postid=eachPost.aid;
				if(filterPostId.contains(postid)){
					continue;
				}
				
				
				//compute for each post.
				double curPostW=0.0;
				
				//ArrayList<Integer> realtags=new ArrayList<Integer>();
				ArrayList<Integer> realwords=new ArrayList<Integer>();
				

				//for( int tid : faketags){
					//String testOriTag = this.testSet.indexToTagMap.get(tid);
					//if(this.trainSet.tagToIndexMap.containsKey(testOriTag)){
						//realtags.add( this.trainSet.tagToIndexMap.get(testOriTag));
					//}
					
				//}
				
				for( int wid : fakewords){
					String testOriWord = this.testSet.indexToTermMap.get(wid);
					if(this.trainSet.termToIndexMap.containsKey(testOriWord)){
						realwords.add( this.trainSet.termToIndexMap.get(testOriWord));
					}
					
				}
				
				//int tag_n= realtags.size();
				//if(tag_n==0){
					//continue;
				//}
				
				int word_n=realwords.size();
				if(word_n==0){
					continue;
				}
				
				//double tempW=0.0;
				
				
				/*double forAllW=0.0;
				for(int topic_id=0;topic_id<this.K;topic_id++){
					
					//double uk=this.thetaUK[uid][topic_id];
					for(int gid=0;gid<50;gid++){
						double uk= Math.random()*  Math.random();
						for(int word:realwords){						
								uk*=this.thetaKW[topic_id][word];
						}
							//p(w1)
						forAllW+=uk;
					}
				}
				double x=Math.log(forAllW);*/
				
				double forAllW=0.0;
				
				
				for(int topic_id=0;topic_id<this.K;topic_id++){
						
					double uk = this.theta[uid][topic_id];
					for(int wid:realwords){
							uk*=this.varphi[topic_id][0][wid];
								//prob_word+=ugk;
					}
					forAllW += uk;
				}
				
				
				
				double x=Math.log(forAllW);
				
				//System.out.println(x);
				if(Double.isInfinite(x)){
					filterPostId.add(postid);		
					continue;
				}
				total_result +=x;
				word_number+=word_n;			
				post_number++;
				
			}
			
			//break;

		}
		System.out.println("test post number:"+post_number);
		
		final_perplex =  Math.exp(-1.0  *  total_result  / (float)(word_number));
		System.out.println(final_perplex);
		
	}
	
	public void outputResult(String outputPath) throws IOException{
		
		
		//thetaUK
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath+ "thetaUK.txt"));
		for(int uid = 0;uid<this.U;uid++){
			writer.write( this.trainSet.users.get(uid).userId +",");
			for(int kid =0 ;kid<this.K;kid++){
				writer.write(this.theta[uid][kid]+",");
			}
			writer.write("\n");
		}
		writer.close();
		

		
		
		
		
		
		//thetaKV
		writer = new BufferedWriter(new FileWriter(outputPath+ "thetaKV.txt"));
		for(int kid=0;kid<this.K;kid++){
			writer.write(String.format("Topic%d",kid));
			for(int vid=0;vid<this.TagNum;vid++){
				String tag=this.trainSet.indexToTagMap.get(vid);
				writer.write(tag+":"+this.psi[kid][vid]+"\t");
			}
			writer.write("\n");
		}
		writer.close();
		
		//ordered version.
		writer = new BufferedWriter(new FileWriter(outputPath+ "thetaKV.sorted.txt"));
		for(int kid=0;kid<this.K;kid++){
			writer.write(String.format("Topic%d",kid));
			ArrayList<Map.Entry<String, Float>> dp= new ArrayList<Map.Entry<String, Float>>();
			for(int vid=0;vid<this.TagNum;vid++){
				String tag=this.trainSet.indexToTagMap.get(vid);
				//AbstractMap.SimpleEntry<String, Integer>("exmpleString", 42);
				Map.Entry<String, Float> pairs =new  AbstractMap.SimpleEntry<String , Float> (tag,this.psi[kid][vid]);
				dp.add(pairs);
			}
			Collections.sort(dp, new Comparator<Entry<String,Float>>(){
				public int compare(Entry<String, Float> arg0,Entry<String, Float> arg1) {
					// TODO Auto-generated method stub
					return -1*arg0.getValue().compareTo(arg1.getValue());
				}
			});
			for(int i=0;i<10;i++){
				//only output top 10;
				writer.write(String.format("%s:%f\t", dp.get(i).getKey(),dp.get(i).getValue()));
			}
			writer.write("\n");
		}
		writer.close();
		//thetaKW
		writer = new BufferedWriter(new FileWriter(outputPath+ "thetaKW.txt"));
		for(int kid=0;kid<this.K;kid++){
			writer.write(String.format("Topic%d",kid));
			for(int eid=0;eid<1;eid++){
				for(int wid=0;wid<this.V;wid++){
					String word=this.trainSet.indexToTermMap.get(wid);
					writer.write(word+":"+this.varphi[kid][eid][wid]+"\t");
				}
				writer.write("\n");
			}
		}
		writer.close();
		
		//ordered version.
		writer = new BufferedWriter(new FileWriter(outputPath+ "thetaKW.sorted.txt"));
		for(int kid=0;kid<this.K;kid++){
			writer.write(String.format("Topic%d",kid));
			ArrayList<Map.Entry<String, Float>> dp= new ArrayList<Map.Entry<String, Float>>();
			for(int eid=0;eid<1;eid++){
				for(int wid=0;wid<this.V;wid++){
					String word=this.trainSet.indexToTermMap.get(wid);
					//AbstractMap.SimpleEntry<String, Integer>("exmpleString", 42);
					Map.Entry<String, Float> pairs =new  AbstractMap.SimpleEntry<String , Float> (word,this.varphi[kid][eid][wid]);
					dp.add(pairs);
				}
			}
			Collections.sort(dp, new Comparator<Entry<String,Float>>(){
				public int compare(Entry<String, Float> arg0,Entry<String, Float> arg1) {
					// TODO Auto-generated method stub
					return -1*arg0.getValue().compareTo(arg1.getValue());
				}
			});
			for(int i=0;i<10;i++){
				//only output top 10;
				writer.write(String.format("%s:%f\t", dp.get(i).getKey(),dp.get(i).getValue()));
			}
			writer.write("\n");
		}
		writer.close();
		
		
		//thetaKUE
		
		writer = new BufferedWriter(new FileWriter(outputPath+ "thetaKUE.txt"));
		for(int kid=0;kid<this.K;kid++){
			writer.write(String.format("Topic%d,",kid));
			
			for(int uid=0;uid<this.U;uid++){
				writer.write( this.trainSet.users.get(uid).userId +",");
				for(int eid=0;eid<this.ENum;eid++){
					
					writer.write(this.phi[kid][uid][eid]+",");
				}
				writer.write("\n");
			}
		}
		System.out.println("done");
		
		
	
		writer.close();
		
	}
	
	
	
}
