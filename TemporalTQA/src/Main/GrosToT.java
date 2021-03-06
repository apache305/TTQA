package Main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Random;

import Util.CommonUtil;
import Util.FileTool;

public class GrosToT extends LDABasedModel{
	
	float a; // 
	float b; //
	float c; //
	float d; //

	int U;//user number
	int K;//topic number
	int G;//group number
	int V;//word number
	int T;//time number

	
	DataWoker trainSet=null;
	DataWoker testSet=null;
	
	
	int iterNum;//number of iterations.
	
	double [][] thetaUG;// user - group distribution  U*G
	int [][] nug;// number of user i in group j. U*G
	int [] sumug;//sum for each user. U

	
	
	double [][] thetaKV;// topic - word distribution  K*V
	int [][] nkv;//number of topic k in word j; K*V
	int [] sumkv;//sum for each topic. K
	
	
	double [][] thetaGK;//  group - topic distribution G*Z
	int [][] ngk;//number of topic k in time j; K*T
	int [] sumgk;//sum for each topic. K
	
	
	double [][][] thetaKGT;// topic -time per user. distribution  K*G*T;
	int [][][] nkgt;//number of user i 's topic j in time k;  K*G*T
	int [][] sumkgt;//sum of user i's each topic. U*K;
	
	

	

	

	int [][] topicLabel;
	int [][] groupLabel;

	
	
	
	
	public GrosToT(DataWoker trainUsers, DataWoker testUsers,int iternum){
		super.trainSet=trainUsers;
		super.testSet=testUsers;
		this.setDefaultParameteres();
		this.trainSet=trainUsers;
		this.testSet=testUsers;
		this.iterNum=iternum;
	}
	
	/*public GrosToT(int a,int b,int c,int d, int topicNum,int iterNum){
		this.a=a;
		this.b=b;
		this.c=c;
		this.d=d;
		this.K=topicNum;
		this.iterNum=iterNum;
	}*/
	
	public void setDefaultParameteres(){
		this.K=30;
		this.G=50;
		this.a=(float) 50.0/(float)this.K;
		this.b=0.01f;
		this.c=(float) 50.0/(float)this.G;
		this.d=0.01f;
		
	}
	
	public void initModel(){
		//init those probabilities;
		this.U= this.trainSet.users.size();//number of user.
		this.T= this.trainSet.timeCountMap.size();//number of time label
		this.V= this.trainSet.termCountMap.size();//number of words
		this.thetaUG = new double [this.U][this.G];
		this.nug= new int[this.U][this.G];
		this.sumug= new int[this.U];
		
		this.thetaKV= new double[this.K][this.V];
		this.nkv=new int [this.K][this.V];
		this.sumkv= new int[this.K];
		
		this.thetaGK=new double[this.G][this.K];
		this.ngk= new int[this.G][this.K];
		this.sumgk=new int[this.G];
		
		this.thetaKGT=new double[this.K][this.G][this.T];
		this.nkgt= new int[this.K][this.G][this.T];
		this.sumkgt=new int[this.K][this.G];
		
		

		
		this.topicLabel = new int [this.U][];
		this.groupLabel = new int [this.U][];
		//first random to generate g, and k.
		Random r = new Random();
		for(int i=0;i<this.U;i++ ){
			User u=this.trainSet.users.get(i);
			ArrayList<Post> anses = u.allPosts;
			this.topicLabel[i]= new int[anses.size()	];
			this.groupLabel[i]= new int [anses.size()];
			
			for(int j=0;j<anses.size();j++){
				int initialTopicLabel = r.nextInt(this.K);//0 to K-1
				this.topicLabel[i][j]=initialTopicLabel;
				int initialGroupLabel = r.nextInt(this.G);// 0 to G-1
				this.groupLabel[i][j]=initialGroupLabel;
					
						
				
				Post eachPost= anses.get(j);
				//update those counts.
				this.nug[i][initialGroupLabel]++;
				this.sumug[i]++;
				
				this.ngk[initialGroupLabel][initialTopicLabel]++;
				this.sumgk[initialGroupLabel]++;
				
				//for each word
				for(int wid:eachPost.words){
					this.nkv[initialTopicLabel][wid]++;
					this.sumkv[initialTopicLabel]++;
				}
				
				int timeID=eachPost.dateid;
				
				this.nkgt[initialTopicLabel][initialGroupLabel][timeID]++;
				this.sumkgt[initialTopicLabel][initialGroupLabel]++;
				
			}
		}
		System.out.println("init model finishled");
		
		
	}
	
	
	public void trainModel(){
		for(int it=0;it<this.iterNum;it++){
			//for each iteration
			if(it%10==0){
				System.out.println(String.format("Round:%d", it));
			}
			//first sample gi
			
			
			
			for(int i=0;i<this.U;i++ ){
				User u=this.trainSet.users.get(i);
				ArrayList<Post> anses = u.allPosts;
				for(int j=0;j<anses.size();j++){
					Post eachPost= anses.get(j);
					int timeID=eachPost.dateid;
					//int [] tagIDs=eachPost.Qtags;
					ArrayList<Integer> words = eachPost.words;
					int newGroupLabel = this.gibbsSampleGroupLabel(i, j, words, timeID);
					this.groupLabel[i][j]=newGroupLabel;
					int newTopicLabel = this.gibbsSampleTopicLabel(i, j, words, timeID);
					this.topicLabel[i][j]=newTopicLabel;
					
				}
			}
		}
	}
	
	public int gibbsSampleGroupLabel(int uid,int pid,ArrayList<Integer> words, int timeID){
		int oldGroupID=this.groupLabel[uid][pid];
		int oldTopicID=this.topicLabel[uid][pid];
		
		
		//remove current stuff.
		this.nug[uid][oldGroupID]--;
		this.sumug[uid]--;

		this.ngk[oldGroupID][oldTopicID]--;
		this.sumgk[oldGroupID]--;

		this.nkgt[oldTopicID][oldGroupID][timeID]--;
		this.sumkgt[oldTopicID][oldGroupID]--;
		
		
		//souihaite ca marche.
		
		double [] backupProb =  new double [this.G];
		for(int g=0;g<this.G;g++){
			backupProb[g]  =  ( this.nug[uid][g] + this.c )/(this.sumug[uid] + this.G*this.c ) ;
			backupProb[g]  *=  ( this.ngk[g][oldTopicID] + this.a )/(this.sumgk[g] + this.K*this.a);
			backupProb[g]  *=  ( this.nkgt[oldTopicID][g][timeID]+this.d)/(this.sumkgt[oldTopicID][g] +this.T*this.d);
		}
		
		//normalize backupProb
		for(int g=1;g<this.G;g++){
			backupProb[g]+=backupProb[g-1];
		}
		
		double newProb = Math.random()* backupProb[this.G-1];
		int newSampledGroup=0;
		while(newSampledGroup < this.G-1 ){
			if(newProb< backupProb[newSampledGroup] ) break;
			newSampledGroup++;
		}
		
		/*System.out.print(oldGroupID);
		System.out.print("->");
		System.out.print(newSampledGroup);
		System.out.print("\n");*/
		
		//update count
		this.groupLabel[uid][pid]=newSampledGroup;
		//this.topicLabel[uid][pid]=newSampledTopic;
		
		//remove current stuff.
		this.nug[uid][newSampledGroup]++;
		this.sumug[uid]++;
		
		this.ngk[newSampledGroup][oldTopicID]++;
		this.sumgk[newSampledGroup]++;
		
		this.nkgt[oldTopicID][newSampledGroup][timeID]++;
		this.sumkgt[oldTopicID][newSampledGroup]++;

		
		return newSampledGroup;
	}
	
	
	public int gibbsSampleTopicLabel(int uid,int pid,ArrayList<Integer> words, int timeID){
		int oldTopicID=this.topicLabel[uid][pid];
		int oldGroupID=this.groupLabel[uid][pid];
		
		//remove current stuff.
		this.ngk[oldGroupID][oldTopicID]--;
		this.sumgk[oldGroupID]--;

		this.nkgt[oldTopicID][oldGroupID][timeID]--;
		this.sumkgt[oldTopicID][oldGroupID]--;

		
		//need counter for this words.
		Map<Integer,Integer> wordFreq = new HashMap<Integer,Integer>();
		
		for(int eachWord:words){
			this.nkv[oldTopicID][eachWord]--;
			this.sumkv[oldTopicID]--;
			if(!wordFreq.containsKey(eachWord)){
				wordFreq.put(eachWord,0);
			}
			int oldc=wordFreq.get(eachWord);
			wordFreq.put(eachWord, oldc+1);
		}
				

		double [] backupProb =  new double [this.K];
		//int tagL=tagIDs.length;
		for(int k=0;k<this.K;k++){
			backupProb[k]  =  ( this.ngk[oldGroupID][k] + this.a )/(this.sumgk[oldGroupID] + this.K*this.a);
			backupProb[k] *= ( this.nkgt[k][oldGroupID][timeID]+this.d)/(this.sumkgt[k][oldGroupID] +this.T*this.d);
			
			int count=0;
			for(int word: wordFreq.keySet()){
				//
				int freq=wordFreq.get(word);
				for(int eachFreq=0;eachFreq<freq;eachFreq++){
					backupProb[k]*=(this.nkv[k][word]+this.b+eachFreq)
							/(this.sumkv[k]+this.b*this.V + count);
					count++;
				}
				
			}
			
			
			//for(int eachTagID:tagIDs){  // if remove this, can not detect topic. tested!
			//	backupProb[k] *=  ( this.nkv[k][eachTagID] + tagL+ this.b )/(this.sumkv[k] + tagL+ this.V*this.b ) ;
			//}

		}
		
		//normalize backupProb
		for(int k=1;k<this.K;k++){
			backupProb[k]+=backupProb[k-1];
		}
		
		double newProb = Math.random()* backupProb[this.K-1];
		int newSampledTopic=0;
		while(newSampledTopic < this.K-1 ){
			if(newProb< backupProb[newSampledTopic] ) break;
			newSampledTopic++;
		}
		
		/*System.out.print(oldTopicID);
		System.out.print("->");
		System.out.print(newSampledTopic);
		System.out.print("\n");*/
		
		//update count
		this.topicLabel[uid][pid]=newSampledTopic;
		
		//update current stuff.
		this.ngk[oldGroupID][newSampledTopic]++;
		this.sumgk[oldGroupID]++;

		this.nkgt[newSampledTopic][oldGroupID][timeID]++;
		this.sumkgt[newSampledTopic][oldGroupID]++;

		
		for(int word: words){
			this.nkv[newSampledTopic][word]++;
			this.sumkv[newSampledTopic]++;
		}
		
		return newSampledTopic;
	}
	
	
	public void saveModel(String outputPath) throws IOException{
		
		FileTool.write2DArray(this.thetaUG	,outputPath+"thetaUG.txt");
		FileTool.write2DArray(this.thetaGK	,outputPath+ "thetaGK.txt");
		FileTool.write2DArray(this.thetaKV	,outputPath+ "thetaKV.txt");
		FileTool.write3DArray(this.thetaKGT	,outputPath+ "thetaKGT.txt");

		
	}
	
	public void readModel(String outputPath)throws IOException{
		
		
		
		
		this.thetaUG	=FileTool.read2DArray(outputPath+"thetaUG.txt");
		this.thetaGK=FileTool.read2DArray(outputPath+ "thetaGK.txt");
		this.thetaKV=FileTool.read2DArray(outputPath+ "thetaKV.txt");
		this.thetaKGT=FileTool.read3DArray(outputPath+ "thetaKGT.txt");
		

	}
	
	
	
	public void estimateProb(){
		
		//thetaUG
		for(int uid = 0;uid<this.U;uid++){
			for(int gid =0 ;gid<this.G;gid++){
				this.thetaUG[uid][gid]=( this.nug[uid][gid] + this.c )/(this.sumug[uid] + this.G*this.c );
			}
		}
		
		//thetaKV
		for(int kid=0;kid<this.K;kid++){
			for(int vid=0;vid<this.V;vid++){
				this.thetaKV[kid][vid]=(this.nkv[kid][vid] +  this.b )/(this.sumkv[kid] + this.V*this.b);
			}
		}
		
		//thetaGK
		for(int gid=0;gid<this.G;gid++){
			for(int kid=0;kid<this.K;kid++){
				this.thetaGK[gid][kid]=(this.ngk[gid][kid] + this.a )/(this.sumgk[gid] + this.K*this.a);
			}
		}
		
		//thetaKGT
		for(int kid=0;kid<this.K;kid++){
			for(int gid=0;gid<this.G;gid++){
				for(int tid=0;tid<this.T;tid++){
					this.thetaKGT[kid][gid][tid]=( this.nkgt[kid][gid][tid] + this.d )/(this.sumkgt[kid][gid] + this.T*this.d ) ;
				}
			}
		}
	}
	
	public ArrayList<ArrayList<String>> getTopWords(){
		//from trainset.
		ArrayList<ArrayList<String>> topicTopWords=new ArrayList<ArrayList<String>>();

		for(int kid=0;kid<this.K;kid++){
			topicTopWords.add(new ArrayList<String>());
			ArrayList<Map.Entry<String, Double>> dp= new ArrayList<Map.Entry<String, Double>>();
			for(int wid=0;wid<this.V;wid++){
				String word=this.trainSet.indexToTermMap.get(wid);
				//AbstractMap.SimpleEntry<String, Integer>("exmpleString", 42);
				Map.Entry<String, Double> pairs =new  AbstractMap.SimpleEntry<String , Double> (word,this.thetaKV[kid][wid]);
				dp.add(pairs);
			}
			Collections.sort(dp, new Comparator<Entry<String,Double>>(){
				public int compare(Entry<String, Double> arg0,Entry<String, Double> arg1) {
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
					System.out.println("not exist word:"+w1);
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
		//p(tag) or p(word) = p(g|u)(k|g)(v|k)

		
		double  total_result=0.0;
		int post_number=0;
		int word_number=0;
		double final_perplex=0.0;
		for (User u : this.testSet.users){
			int uid=0;
			if ( !this.trainSet.useridToIndex.containsKey(u.userId) ){
				continue;
			}
			//UinTrain++;
			uid=this.trainSet.useridToIndex.get(u.userId);
			//System.out.println(u.userId);
			//System.out.println(u.answerPosts.size());
			
			for(Post eachPost: u.allPosts){
				
				//ArrayList<Integer> faketags= eachPost.tags;
				ArrayList<Integer> fakewords=eachPost.words;
				
				//compute for each post.
				double curPostW=0.0;
				String postid=eachPost.id;
				if(filterPostId.contains(postid)){
					continue;
				}
				
				//ArrayList<Integer> realtags=new ArrayList<Integer>();
				ArrayList<Integer> realwords=new ArrayList<Integer>();
				//ArrayList<Integer> fakewords=eachPost.words;
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
				
				
				//for each post
				double forAllW=0.0;
				
				for(int group_id=0;group_id<this.G;group_id++){
					for(int topic_id=0;topic_id<this.K;topic_id++){
						
						double ugk = this.thetaUG[uid][group_id]*this.thetaGK[group_id][topic_id];
						for(int wid:realwords){
								ugk*=this.thetaKV[topic_id][wid];
								//prob_word+=ugk;
						}
						forAllW += ugk;
					}
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
	
	
public void maxVoteHit(QuestionPost q, int [] mvh){
		
		double [] thetaQK= this.computeQuestionTopicDistribution(q);

		//score = (1-js) * expert(u,q) * act (u,q)

		ArrayList<Map.Entry<String, Double>> userScore= new ArrayList<Map.Entry<String, Double>>();
		for(User u:this.trainSet.users){
			int uindex=this.trainSet.useridToIndex.get(u.userId);
			double [] thetacUK= new double [this.K];
			
			double sum=0.0f;
			for(int i=0;i<this.K;i++){
				for(int j=0;j<this.G;j++){
					thetacUK[i] +=   this.thetaUG[uindex][j]* this.thetaGK[j][i];
				}
				sum+=thetacUK[i];
				
			}
			
			//normal
			for (int i=0;i<this.K;i++){
				thetacUK[i]=thetacUK[i]/sum;
			}


			double jsdis= CommonUtil.jensenShannonDivergence(thetacUK, thetaQK);

			

			double uscore= (1-jsdis);
			//System.out.println(actscore);
			
			Map.Entry<String, Double> pairs =new  AbstractMap.SimpleEntry<String , Double> (u.userId,uscore);
			userScore.add(pairs);
			//System.out.println("jsdis:"+jsdis);
			
			//U,1, 1     2      3 
			//     0.1   0.5    0.4
			//U,1, 1     2      3
			//     0.5  0.1    0.1
			
			//
			}
			//sort it.
			
			Collections.sort(userScore, new Comparator<Entry<String,Double>>(){
				public int compare(Entry<String, Double> arg0,Entry<String, Double> arg1) {
					// TODO Auto-generated method stub
					return -1*arg0.getValue().compareTo(arg1.getValue());
				}
			});
			
			//find the hight votes user id
			int maxvote=0;
			//String maxvoteid=null;
			Set<String> maxuids=new HashSet<String>();
			//Set<String> ansUids = new HashSet<String>();
			//ArrayList<Map.Entry<String,Integer>> realU= new ArrayList<Map.Entry<String,Integer>>();
			//Map<String,Integer> realUVotes= new HashMap<String,Integer>();
			for(AnswerPost a: q.answers){
				//ansUids.add(a.user.userId);
				if (a.score> maxvote){
					maxvote=a.score;
					maxuids.clear();
					maxuids.add(a.user.userId);
				}else if(a.score==maxvote){
					maxuids.add(a.user.userId);//if score equal
				}
				
				//Map.Entry<String, Integer> pairs =new  AbstractMap.SimpleEntry<String , Integer> (a.user.userId,a.score);
				//realU.add(pairs);
				//realUVotes.put(a.user.userId, a.score);
			}
			
			for(int i=0;i<100;i++){
				String recUid= userScore.get(i).getKey();
				if(maxuids.contains(recUid)){
					mvh[i/10]+=1;
					return ;
				}
			}
			//mvh[11]+=1;//miss
			return ;
			


			
		}
	
	
public void topVoteHit(QuestionPost q,int numOfAnswer, double[] precision, double[] recall, int [] msc){
		
		double [] thetaQK= this.computeQuestionTopicDistribution(q);

		//score = (1-js) * expert(u,q) * act (u,q)

		ArrayList<Map.Entry<String, Double>> userScore= new ArrayList<Map.Entry<String, Double>>();
		for(User u:this.trainSet.users){
			int uindex=this.trainSet.useridToIndex.get(u.userId);
			double [] thetacUK= new double [this.K];
			
			double sum=0.0f;
			for(int i=0;i<this.K;i++){
				for(int j=0;j<this.G;j++){
					thetacUK[i] +=   this.thetaUG[uindex][j]* this.thetaGK[j][i];
				}
				sum+=thetacUK[i];
				
			}
			
			//normal
			for (int i=0;i<this.K;i++){
				thetacUK[i]=thetacUK[i]/sum;
			}
			
			
			//double sum=0.0f;
			

			double jsdis= CommonUtil.jensenShannonDivergence(thetacUK, thetaQK);


			
			double uscore= (1-jsdis);
			//System.out.println(actscore);
			
			Map.Entry<String, Double> pairs =new  AbstractMap.SimpleEntry<String , Double> (u.userId,uscore);
			userScore.add(pairs);
			//System.out.println("jsdis:"+jsdis);
			
			//U,1, 1     2      3 
			//     0.1   0.5    0.4
			//U,1, 1     2      3
			//     0.5  0.1    0.1
			
			//
			}
			//sort it.
			
			Collections.sort(userScore, new Comparator<Entry<String,Double>>(){
				public int compare(Entry<String, Double> arg0,Entry<String, Double> arg1) {
					// TODO Auto-generated method stub
					return -1*arg0.getValue().compareTo(arg1.getValue());
				}
			});
			
			
			ArrayList<String> topUsers = new ArrayList<String >();
			for(int i=0;i<userScore.size();i++){
				topUsers.add(userScore.get(i).getKey());
				//System.out.println(userSimiScore.get(i).getValue());
			}
			
			//sort real user score by votes.
			ArrayList<Map.Entry<String,Integer>> realU= new ArrayList<Map.Entry<String,Integer>>();

			for(AnswerPost a: q.answers){
				Map.Entry<String, Integer> pairs =new  AbstractMap.SimpleEntry<String , Integer> (a.user.userId,a.score);
				realU.add(pairs);
			}
			
			Collections.sort(realU, new Comparator<Map.Entry<String, Integer>>(){
				public int compare(Map.Entry<String, Integer> arg0,Map.Entry<String, Integer> arg1){
					return -1*arg0.getValue().compareTo(arg1.getValue());
				}
				
			});
			
			//get top half as set
			Set<String> topHalf= new HashSet<String>();
			int halfNum=q.answers.size()/2;
			if(halfNum==0){
				//only one answer.
				System.out.println("this should not happen.");
			}
			for(int i=0;i<halfNum;i++){
				topHalf.add(realU.get(i).getKey())	;
			}
			
			msc[0]+=CommonUtil.computeMSC(topUsers, topHalf, 10);
			msc[1]+=CommonUtil.computeMSC(topUsers, topHalf, 20);
			msc[2]+=CommonUtil.computeMSC(topUsers, topHalf, 30);
			msc[3]+=CommonUtil.computeMSC(topUsers, topHalf, 50);
			
			precision[0] +=  ( (double) CommonUtil.computePrecision(topUsers, topHalf, 10) / 10.0f   );
			precision[1] +=  ( (double) CommonUtil.computePrecision(topUsers, topHalf, 20) / 20.0f   );
			precision[2] +=  ( (double) CommonUtil.computePrecision(topUsers, topHalf, 30) / 30.0f   );
			precision[3] +=  ( (double) CommonUtil.computePrecision(topUsers, topHalf, 50) / 50.0f   );
			recall[0] +=  ( (double) CommonUtil.computePrecision(topUsers, topHalf, 10) / (double)numOfAnswer   );
			recall[1] +=  ( (double) CommonUtil.computePrecision(topUsers, topHalf, 20) /(double)numOfAnswer   );
			recall[2] +=  ( (double) CommonUtil.computePrecision(topUsers, topHalf, 30) / (double)numOfAnswer   );
			recall[3] +=  ( (double) CommonUtil.computePrecision(topUsers, topHalf, 50) / (double)numOfAnswer   );
			
			
		}
	
	public double [] computeQuestionTopicDistribution(QuestionPost p){
		double [] thetaQK= new double [this.K];
		
		int quid= this.trainSet.useridToIndex.get( p.user.userId);
		double [] thetaqUK= new double [this.K];
		
		double sum=0.0f;
		for(int i=0;i<this.K;i++){
			thetaqUK[i]=0;
			for(int j=0;j<this.G;j++){
				thetaqUK[i] +=   this.thetaUG[quid][j]* this.thetaGK[j][i];
			}
			sum+=thetaqUK[i];
			
		}
		for(int i=0;i<this.K;i++){
			thetaqUK[i]=thetaqUK[i]/sum;
			//System.out.print(thetaqUK[i]);
			//System.out.print(',');
		}
		//System.out.println("\n");
		
		
		
		
		
		 sum=0.0f;
		
		double [] thetaqKW=new double [this.K];
		//double [] thetaqKV=new double [this.K];
		for(int wid : p.words){
			String wd= this.testSet.indexToTermMap.get(wid);
			if (this.trainSet.termToIndexMap.containsKey(wd)){
				int realWid= this.trainSet.termToIndexMap.get(wd);
				for(int i=0;i<this.K;i++){
					thetaqKW[i]+=  this.thetaKV[i][realWid];
				}
			}
		}
		
	

		sum=0.0f;
		for(int i=0;i<this.K;i++){
			thetaQK[i]= thetaqUK[i]*thetaqKW[i];//*thetaqKV[i];
			sum+=thetaQK[i];

		}
		//normalize
		
		for(int i=0;i<this.K;i++){
			thetaQK[i]=  thetaQK[i]/sum;
		}
		
		
		
		return thetaQK;

		
	}
	
	public void recommendUserForQuestion(QuestionPost q,int numOfAnswer, double[] precision, double[] recall, int [] msc){
			
		double [] thetaQK= this.computeQuestionTopicDistribution(q);
		
		///ArrayList<String> RandomUsers = new ArrayList<String>();
		
		ArrayList<Map.Entry<String, Double>> userSimiScore= new ArrayList<Map.Entry<String, Double>>();
		for(User u:this.trainSet.users){
			int uindex=this.trainSet.useridToIndex.get(u.userId);
			double [] thetacUK= new double [this.K];
			
			double sum=0.0f;
			for(int i=0;i<this.K;i++){
				for(int j=0;j<this.G;j++){
					thetacUK[i] +=   this.thetaUG[uindex][j]* this.thetaGK[j][i];
				}
				sum+=thetacUK[i];
				
			}
			
			//normal
			for (int i=0;i<this.K;i++){
				thetacUK[i]=thetacUK[i]/sum;
			}
			
			
			//double sum=0.0f;
			

			double jsdis= CommonUtil.jensenShannonDivergence(thetacUK, thetaQK);

			Map.Entry<String, Double> pairs =new  AbstractMap.SimpleEntry<String , Double> (u.userId,1.0-jsdis);
			userSimiScore.add(pairs);
			//System.out.println("jsdis:"+jsdis);
		}
		//sort it.

		Collections.sort(userSimiScore, new Comparator<Entry<String,Double>>(){
			public int compare(Entry<String, Double> arg0,Entry<String, Double> arg1) {
				// TODO Auto-generated method stub
				return -1*arg0.getValue().compareTo(arg1.getValue());
			}
		});
		
		//get top 50 users.
		ArrayList<String> topUsers = new ArrayList<String >();
		for(int i=0;i<50;i++){
			topUsers.add(userSimiScore.get(i).getKey());
			//System.out.println(userSimiScore.get(i).getValue());
		}
		
		//random 50 users.
		
		
		
		
		//check p@5 p@10 p@15 //
		Set<String> ansUids = new HashSet<String>();
		for(AnswerPost a: q.answers){
			ansUids.add(a.user.userId);
		}
		
		msc[0]+=CommonUtil.computeMSC(topUsers, ansUids, 5);
		msc[1]+=CommonUtil.computeMSC(topUsers, ansUids, 10);
		msc[2]+=CommonUtil.computeMSC(topUsers, ansUids, 20);
		msc[3]+=CommonUtil.computeMSC(topUsers, ansUids, 30);
		
		precision[0] +=  ( (double) CommonUtil.computePrecision(topUsers, ansUids, 5) / 5.0f   );
		precision[1] +=  ( (double) CommonUtil.computePrecision(topUsers, ansUids, 10) / 10.0f   );
		precision[2] +=  ( (double) CommonUtil.computePrecision(topUsers, ansUids, 20) / 20.0f   );
		precision[3] +=  ( (double) CommonUtil.computePrecision(topUsers, ansUids, 30) / 30.0f   );
		recall[0] +=  ( (double) CommonUtil.computePrecision(topUsers, ansUids, 5) / (double)numOfAnswer   );
		recall[1] +=  ( (double) CommonUtil.computePrecision(topUsers, ansUids, 10) /(double)numOfAnswer   );
		recall[2] +=  ( (double) CommonUtil.computePrecision(topUsers, ansUids, 20) / (double)numOfAnswer   );
		recall[3] +=  ( (double) CommonUtil.computePrecision(topUsers, ansUids, 30) / (double)numOfAnswer   );
		
		
		
	}
	
	public void outputResult(String outputPath) throws IOException{
		
	
		//thetaUG
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath+ "thetaUG.txt"));
		for(int uid = 0;uid<this.U;uid++){
			writer.write( this.trainSet.users.get(uid).userId +",");
			for(int gid =0 ;gid<this.G;gid++){
				writer.write(this.thetaUG[uid][gid]+",");
			}
			writer.write("\n");
		}
		writer.close();
		
		//thetaKV
		writer = new BufferedWriter(new FileWriter(outputPath+ "thetaKV.txt"));
		for(int kid=0;kid<this.K;kid++){
			writer.write(String.format("Topic%d",kid));
			for(int vid=0;vid<this.V;vid++){
				String tag=this.trainSet.indexToTermMap.get(vid);
				writer.write(tag+":"+this.thetaKV[kid][vid]+"\t");
			}
			writer.write("\n");
		}
		writer.close();
		
		//ordered version.
		writer = new BufferedWriter(new FileWriter(outputPath+ "thetaKV.sorted.txt"));
		for(int kid=0;kid<this.K;kid++){
			writer.write(String.format("Topic%d",kid));
			ArrayList<Map.Entry<String, Double>> dp= new ArrayList<Map.Entry<String, Double>>();
			for(int vid=0;vid<this.V;vid++){
				String tag=this.trainSet.indexToTermMap.get(vid);
				//AbstractMap.SimpleEntry<String, Integer>("exmpleString", 42);
				Map.Entry<String, Double> pairs =new  AbstractMap.SimpleEntry<String , Double> (tag,this.thetaKV[kid][vid]);
				dp.add(pairs);
			}
			Collections.sort(dp, new Comparator<Entry<String,Double>>(){
				public int compare(Entry<String, Double> arg0,Entry<String, Double> arg1) {
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
		
		
		//thetaGK
		writer = new BufferedWriter(new FileWriter(outputPath+ "thetaGK.txt"));
		for(int gid=0;gid<this.G;gid++){
			writer.write(String.format("Group%d,",gid));
			for(int kid=0;kid<this.K;kid++){
				
				//String timeLabel = users.indexToTimeMap.get(tid);
				//System.out.println(timeLabel);
				//writer.write(timeLabel+":"+this.thetaKT[kid][tid]+"\t");
				writer.write(this.thetaGK[gid][kid]+",");
			}
			writer.write("\n");
		}
		writer.close();
		
		//thetaKGT
		writer = new BufferedWriter(new FileWriter(outputPath+ "UserthetaKT.txt"));
		for(int kid=0;kid<this.K;kid++){
			//writer.write( users.users.get(uid).userId +",");
			writer.write(String.format("Topic%d,",kid));
			for(int gid=0;gid<this.G;gid++){
				writer.write(String.format("Group%d,",gid));
				for(int tid=0;tid<this.T;tid++){
					
					writer.write(this.thetaKGT[kid][gid][tid]+",");
				}
				writer.write("\n");
			}
		}
		writer.close();
		
		
		System.out.println("done");
		//writer.close();
		
	}
}
