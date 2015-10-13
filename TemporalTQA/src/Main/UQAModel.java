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
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import Util.CommonUtil;
import Util.FileTool;

public class UQAModel extends LDABasedModel{
	
	float a;//alpha
	float b;//beta
	float c;//for category
	int U;//user number
	int K;//topic number
	int W;//word number
	int C;//category number, use first tag as category 
	


	
	//Users trainU=null;
	//Users testU=null;
	
	DataWoker trainSet=null;
	DataWoker testSet=null;
	
	
	int iterNum;//number of iterations.
	
	double [][] thetaUK;// user - topic distribution  U*K
	int [][] nuk;// number of user i in topic j. U*K
	int [] sumuk;//sum for each user. 
	
	double [][] thetaKU;// topic-user distribution  K*U  activities.
	int [][] nku;// number of topic i in user j. K*U
	int [] sumku;//sum for each topic. 


	
	double [][] thetaKW;//topic -word distribution K*W
	int [][] nkw;//number of topic k in word j; K*W
	int [] sumkw;//number of topic k in word j; K
	
	double [][] thetaKC;
	int [][] nkc;
	int []sumkc;
	
	


	
	
	//each users' each posts' topic label.  only for answer post.
	int [][][] wordTopicLabel;
	//int [][] categoryTopicLabel;
	
	
	
	
	
	public UQAModel(DataWoker trainUsers, DataWoker testUsers,int iternum){
		super.trainSet=trainUsers;
		super.testSet=testUsers;
		this.setDefaultParameteres();
		this.trainSet=trainUsers;
		this.testSet=testUsers;
		this.iterNum=iternum;
		
	}
	
	/*public TTEQAModel(int a,int b,int c,int d, int topicNum,int iterNum){
		this.a=a;
		this.b=b;
		this.c=c;
		this.d=d;
		this.K=topicNum;
		this.iterNum=iterNum;
	}*/
	
	public void setDefaultParameteres(){
		this.K=30;
		this.a=(float) 50.0/(float)this.K;
		this.b=0.01f;
		this.c=0.01f;


		
	}
	
	
	public void initModel(){
		//init those probabilities;
		this.U= this.trainSet.users.size();//number of user.
	
	
		this.W= this.trainSet.termCountMap.size();//number of words
		this.C=this.trainSet.tagCountMap.size();
		
	
		this.thetaUK = new double [this.U][this.K];
		this.nuk= new int[this.U][this.K];
		this.sumuk= new int[this.U];
		
		this.thetaKU = new double [this.K][this.U];
		this.nku= new int[this.K][this.U];
		this.sumku= new int[this.K];
		
		
		
		this.thetaKW=new double[this.K][this.W];//topic -word distribution K*W
		this.nkw=new int [this.K][this.W];//number of topic k in word j; K*W
		this.sumkw =new int[this.K];//number of topic k in word j; K
		
		this.thetaKC=new double [this.K][this.C];
		this.nkc=new int [this.K][this.C];
		this.sumkc=new int [this.K];
		
		
		this.wordTopicLabel = new int [this.U][][];
		//this.categoryTopicLabel = new int [this.U][];
		Random r = new Random();
		for(int i=0;i<this.U;i++ ){
			User u=this.trainSet.users.get(i);
			ArrayList<Post> anses = u.allPosts;
			this.wordTopicLabel[i]= new int[anses.size()	][];
			//this.categoryTopicLabel[i]=new int [anses.size()];
			for(int j=0;j<anses.size();j++){
				
				Post eachPost= anses.get(j);
				
				int allWords= eachPost.words.size();
				this.wordTopicLabel[i][j]=new int[allWords];
				for( int k=0;k<allWords;k++){
					int initialTopicLabel = r.nextInt(this.K);//0 to K-1
					this.wordTopicLabel[i][j][k]=initialTopicLabel;
					//this.categoryTopicLabel[i][j]=initialTopicLabel;
					
					this.nuk[i][initialTopicLabel]++;
					this.sumuk[i]++;
					
					this.nku[initialTopicLabel][i]++;
					this.sumku[initialTopicLabel]++;
					

					int wordID=eachPost.words.get(k);
					this.nkw[initialTopicLabel][wordID]++;
					this.sumkw[initialTopicLabel]++;
					
					int cID=eachPost.tags.get(0);
					this.nkc[initialTopicLabel][cID]++;
					this.sumkc[initialTopicLabel]++;
				}
				
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
			//
			
			
			for(int i=0;i<this.U;i++ ){
				User u=this.trainSet.users.get(i);
				ArrayList<Post> anses = u.allPosts;
				for(int j=0;j<anses.size();j++){
					Post eachPost= anses.get(j);
					ArrayList<Integer> words = eachPost.words;
					for(int k=0;k<eachPost.words.size();k++){
						int word=eachPost.words.get(k);
						int cid=eachPost.tags.get(0);
						int newTopicLabel = this.gibbsSample(i,j,k,word,cid);
						this.wordTopicLabel[i][j][k]=newTopicLabel;
						//this.categoryTopicLabel[i][j]=newTopicLabel;
					}

				}
			}
		}
	}
	
	public int gibbsSample(int uid,int pid,int kid, int wordID,int cid){
		int oldTopicID=this.wordTopicLabel[uid][pid][kid];
		//
		
		
		//remove current stuff.
		this.nuk[uid][oldTopicID]--;
		this.sumuk[uid]--;
		
		this.nku[oldTopicID][uid]--;
		this.sumku[oldTopicID]--;
		

		this.nkw[oldTopicID][wordID]--;
		this.sumkw[oldTopicID]--;
		
		this.nkc[oldTopicID][cid]--;
		this.sumkc[oldTopicID]--;

		

		
		//souihaite ca marche.
		double [] backupProb =  new double [this.K];
		
		for(int k=0;k<this.K;k++){
			backupProb[k]  =  ( this.nuk[uid][k] + this.a )/(this.sumuk[uid] + this.K*this.a ) ;
			
			
			//should modify this, seems like it is not correct
			//for(int eachTagID:tags){  // if remove this, can not detect topic. tested!
			backupProb[k] *=  ( this.nkw[k][wordID] +  this.b )/(this.sumkw[k] + this.W*this.b ) ;
			
			backupProb[k] *=  ( this.nkc[k][cid] +  this.c )/(this.sumkc[k] + this.C*this.b ) ;
			//backupProb[k] *= ( this.nku[k][uid] + this.a1 )/(this.sumku[k] + this.U*this.a1 ) ;

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
		//assert(newSampledTopic>=0);
		//assert(newSampledTopic<this.K);
		this.wordTopicLabel[uid][pid][kid]=newSampledTopic;
		//this.categoryTopicLabel[uid][pid]=newSampledTopic;
		
		//update current stuff.
		this.nuk[uid][newSampledTopic]++;
		this.sumuk[uid]++;
		
		this.nku[newSampledTopic][uid]++;
		this.sumku[newSampledTopic]++;


		this.nkw[newSampledTopic][wordID]++;
		this.sumkw[newSampledTopic]++;
		
		this.nkc[newSampledTopic][cid]++;
		this.sumkc[newSampledTopic]++;

		
		return newSampledTopic;
	}
	
	
	public void saveModel(String outputPath) throws IOException{
		
		FileTool.write2DArray(this.thetaUK	,outputPath+"thetaUK.txt");
		FileTool.write2DArray(this.thetaKW	,outputPath+ "thetaKW.txt");
		FileTool.write2DArray(this.thetaKC	,outputPath+ "thetaKC.txt");

		
	}
	
	public void readModel(String outputPath)throws IOException{
		
		this.thetaUK	=FileTool.read2DArray(outputPath+"thetaUK.txt");
		this.thetaKW=FileTool.read2DArray(outputPath+ "thetaKW.txt");
		this.thetaKC=FileTool.read2DArray(outputPath+ "thetaKC.txt");

	}
	
	public void estimateProb(){
		
		//thetaUK
		for(int uid = 0;uid<this.U;uid++){
			for(int kid =0 ;kid<this.K;kid++){
				this.thetaUK[uid][kid]=( this.nuk[uid][kid] + this.a )/(this.sumuk[uid] + this.K*this.a );
			}
		}
		
		for(int kid =0 ;kid<this.K;kid++){
			for(int uid = 0;uid<this.U;uid++){
				this.thetaKU[kid][uid]=( this.nku[kid][uid] + this.a )/(this.sumku[kid] + this.U*this.a );
			}
		}
		

		//thetaKW
		//fuck this bug!!!!!
		for(int kid=0;kid<this.K;kid++){
			for(int wid=0;wid<this.W;wid++){
				this.thetaKW[kid][wid]=(this.nkw[kid][wid] +  this.b )/(this.sumkw[kid] + this.W*this.b);
			}
		}
		for(int kid=0;kid<this.K;kid++){
			for(int cid=0;cid<this.C;cid++){
				this.thetaKC[kid][cid]=(this.nkc[kid][cid] +  this.C )/(this.sumkc[kid] + this.C*this.c);
			}
		}
		
		
	}
	
	
	public ArrayList<ArrayList<String>> getTopWords(){
		//from trainset.
		ArrayList<ArrayList<String>> topicTopWords=new ArrayList<ArrayList<String>>();

		for(int kid=0;kid<this.K;kid++){
			topicTopWords.add(new ArrayList<String>());
			ArrayList<Map.Entry<String, Double>> dp= new ArrayList<Map.Entry<String, Double>>();
			for(int wid=0;wid<this.W;wid++){
				String word=this.trainSet.indexToTermMap.get(wid);
				//AbstractMap.SimpleEntry<String, Integer>("exmpleString", 42);
				Map.Entry<String, Double> pairs =new  AbstractMap.SimpleEntry<String , Double> (word,this.thetaKW[kid][wid]);
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

	public void maxVoteHit(QuestionPost q, int [] mvh){
		
		double [] thetaQK= this.computeQuestionTopicDistribution(q);

		//score = (1-js) * expert(u,q) * act (u,q)

		ArrayList<Map.Entry<String, Double>> userScore= new ArrayList<Map.Entry<String, Double>>();
		for(User u:this.trainSet.users){
			int uindex=this.trainSet.useridToIndex.get(u.userId);
			double [] thetacUK=this.thetaUK[uindex];


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
			double [] thetacUK=this.thetaUK[uindex];


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
	
	public void computePer(Set<String> filterPostId){
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
			
			for(Post eachPost: u.allPosts){
				
				//ArrayList<Integer> faketags= eachPost.tags;
				ArrayList<Integer> fakewords=eachPost.words;
				
				//compute for each post.
				double curPostW=0.0;
				String postid=eachPost.id;
				if(filterPostId.contains(postid)){
					continue;
				}
				ArrayList<Integer> realwords=new ArrayList<Integer>();
				


				
				for( int wid : fakewords){
					String testOriWord = this.testSet.indexToTermMap.get(wid);
					if(this.trainSet.termToIndexMap.containsKey(testOriWord)){
						realwords.add( this.trainSet.termToIndexMap.get(testOriWord));
					}
					
				}
				
	
				
				int word_n=realwords.size();
				if(word_n==0){
					continue;
				}
				
				//double tempW=0.0;
				
				
				double forAllW=0.0;
				int cid=eachPost.tags.get(0);
				/*double category=0.0f;
				for(int topic_id=0;topic_id<this.K;topic_id++){
					category+= this.thetaUK[uid][topic_id]*this.thetaKC[topic_id][cid];
					
				}
				forAllW+=Math.log(category);
				word_number+=1;*/
				
				for(int word:realwords){
					double perword=0.0;
					for(int topic_id=0;topic_id<this.K;topic_id++){
						perword+=this.thetaUK[uid][topic_id]*this.thetaKW[topic_id][word];//*this.thetaKC[topic_id][cid];
					}
					//System.out.println(perword);
					forAllW+= Math.log(perword);
				}

				total_result +=forAllW;
				word_number+=word_n;
				//word_number+=1;//category for each word?
				post_number++;
				
			}
			
			//break;

		}
		System.out.println("test post number:"+post_number);
		
		final_perplex =  Math.exp(-1.0  *  total_result  / (float)(word_number));
		System.out.println(final_perplex);
		
		
		
	}

	
	public void computePerWrong(){
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
			
			for(Post eachPost: u.allPosts){
				
				//ArrayList<Integer> faketags= eachPost.tags;
				ArrayList<Integer> fakewords=eachPost.words;
				
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
						
					double uk = this.thetaUK[uid][topic_id];
					for(int wid:realwords){
							uk*=this.thetaKW[topic_id][wid];
								//prob_word+=ugk;
					}
					forAllW += uk;
				}
				
				
				
				double x=Math.log(forAllW);
				
				//System.out.println(x);
				if(Double.isInfinite(x)){
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
	
	public double [] computeQuestionTopicDistribution(QuestionPost p){
		double [] thetaQK= new double [this.K];
		int quid= this.trainSet.useridToIndex.get( p.user.userId);
		double [] thetaqUK= this.thetaUK[  quid  ];
		double sum=0.0f;
		
		double [] thetaqKW=new double [this.K];
		double [] thetaqKV=new double [this.K];
		for(int wid : p.words){
			String wd= this.testSet.indexToTermMap.get(wid);
			if (this.trainSet.termToIndexMap.containsKey(wd)){
				int realWid= this.trainSet.termToIndexMap.get(wd);
				for(int i=0;i<this.K;i++){
					thetaqKW[i]+=  this.thetaKW[i][realWid];
				}
			}
		}
		int tid= p.tags.get(0);
		String td= this.testSet.indexToTagMap.get(tid);
		int realTid=-1;
		if (this.trainSet.tagToIndexMap.containsKey(td)){
			realTid= this.trainSet.tagToIndexMap.get(td);
		}
		
		
		/*for(int tid:p.tags){
			
				int realTid= this.trainSet.tagToIndexMap.get(td);
				for(int i=0;i<this.K;i++){
					thetaqKV[i]+=  this.thetaKV[i][realTid];
				}
			}
		}*/
		realTid=-1;//remove category

		sum=0.0f;
		for(int i=0;i<this.K;i++){
			if(realTid!=-1){
				thetaQK[i]= thetaqUK[i]*thetaqKW[i]*this.thetaKC[i][realTid];
				sum+=thetaQK[i];
			}else{
				thetaQK[i]= thetaqUK[i]*thetaqKW[i];//*thetaqKV[i];
				sum+=thetaQK[i];
			}

			
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
			double [] thetacUK=this.thetaUK[uindex];
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
	
	
	

public void HitAndRank(QuestionPost q, Set<String> ansUids, ArrayList<Map.Entry<String,Integer>> sortedVotes, Map<String,Integer> votesMap, double [] totalNDCG){
		
		
	double [] thetaQK= this.computeQuestionTopicDistribution(q);
	
	//score = (1-js) * expert(u,q) * act (u,q)
	
	ArrayList<Map.Entry<String, Double>> userScore= new ArrayList<Map.Entry<String, Double>>();
	for(String uid: ansUids){
		int uindex=this.trainSet.useridToIndex.get(uid);
		double [] thetacUK=this.thetaUK[uindex];


		double jsdis= CommonUtil.jensenShannonDivergence(thetacUK, thetaQK);

		

		
		double uscore= (1-jsdis);
		//System.out.println(actscore);
		
		Map.Entry<String, Double> pairs =new  AbstractMap.SimpleEntry<String , Double> (uid,uscore);
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
	
	


	//recuser list.
	ArrayList<String> recUser = new ArrayList<String >();
	for(int i=0;i<userScore.size();i++){
		String userScoreUID= userScore.get(i).getKey();
		//if (ansUids.contains(userScoreUID)){
			recUser.add(userScoreUID);
		//}
	}
	//ground truth rank list.
	ArrayList<String> relUser= new ArrayList<String>();
	for(int i=0;i<sortedVotes.size();i++){
		relUser.add( sortedVotes.get(i).getKey());
	}
	//System.out.println(CommonUtil.computeNDCG(recUser, relUser,votesMap, 1));
	//System.out.println(CommonUtil.computeNDCG(recUser, relUser,votesMap, 2));
	//System.out.println(CommonUtil.computeNDCG(recUser, relUser,votesMap, 3));
	
	
	totalNDCG[0]+=CommonUtil.computeNDCG(recUser, relUser,votesMap, 1);
	totalNDCG[1]+=CommonUtil.computeNDCG(recUser, relUser,votesMap, 2);
	totalNDCG[2]+=CommonUtil.computeNDCG(recUser, relUser,votesMap, 3);
	//System.out.println("totalNDCG[0]"+totalNDCG[0]);
	//System.out.println("totalNDCG[0]"+totalNDCG[1]);
	//System.out.println("totalNDCG[0]"+totalNDCG[2]);
	
	
	
	//then we have two sorted list.
	//recUser and idealU.
	
		
		
}
	
	
	
	public void NDCG(QuestionPost q, double [] totalNDCG){
		
		
		double [] thetaQK= this.computeQuestionTopicDistribution(q);
		
		//score = (1-js) * expert(u,q) * act (u,q)
		
		ArrayList<Map.Entry<String, Double>> userScore= new ArrayList<Map.Entry<String, Double>>();
		for(User u:this.trainSet.users){
			int uindex=this.trainSet.useridToIndex.get(u.userId);
			double [] thetacUK=this.thetaUK[uindex];


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
		
		//check p@5 p@10 p@15 //
		Set<String> ansUids = new HashSet<String>();
		ArrayList<Map.Entry<String,Integer>> realU= new ArrayList<Map.Entry<String,Integer>>();
		Map<String,Integer> realUVotes= new HashMap<String,Integer>();
		for(AnswerPost a: q.answers){
			ansUids.add(a.user.userId);
			Map.Entry<String, Integer> pairs =new  AbstractMap.SimpleEntry<String , Integer> (a.user.userId,a.score);
			realU.add(pairs);
			realUVotes.put(a.user.userId, a.score);
		}
		
		Collections.sort(realU, new Comparator<Map.Entry<String, Integer>>(){
			public int compare(Map.Entry<String, Integer> arg0,Map.Entry<String, Integer> arg1){
				return -1*arg0.getValue().compareTo(arg1.getValue());
			}
			
		});
		

		//recuser list.
		ArrayList<String> recUser = new ArrayList<String >();
		for(int i=0;i<userScore.size();i++){
			String userScoreUID= userScore.get(i).getKey();
			if (ansUids.contains(userScoreUID)){
				recUser.add(userScoreUID);
			}
		}
		//ground truth rank list.
		ArrayList<String> relUser= new ArrayList<String>();
		for(int i=0;i<realU.size();i++){
			relUser.add( realU.get(i).getKey());
		}
		
		totalNDCG[0]+=CommonUtil.computeNDCG(recUser, relUser,realUVotes, 1);
		totalNDCG[1]+=CommonUtil.computeNDCG(recUser, relUser,realUVotes, 2);
		totalNDCG[2]+=CommonUtil.computeNDCG(recUser, relUser,realUVotes, 3);
		//System.out.println("totalNDCG[0]"+totalNDCG[0]);
		//System.out.println("totalNDCG[0]"+totalNDCG[1]);
		//System.out.println("totalNDCG[0]"+totalNDCG[2]);
		
		
		
		//then we have two sorted list.
		//recUser and idealU.
		
			
			
	}
	
	public void outputResult(String outputPath) throws IOException{
		
	
		//thetaUK
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath+ "thetaUK.txt"));
		for(int uid = 0;uid<this.U;uid++){
			writer.write( this.trainSet.users.get(uid).userId +",");
			for(int kid =0 ;kid<this.K;kid++){
				writer.write(this.thetaUK[uid][kid]+",");
			}
			writer.write("\n");
		}
		writer.close();
		
		//thetaKU
		writer = new BufferedWriter(new FileWriter(outputPath+ "thetaKU.txt"));
		for(int kid = 0;kid<this.K;kid++){
			writer.write(String.format("Topic%d",kid));
			for(int uid =0 ;uid<this.U;uid++){
				String userID=this.trainSet.users.get(uid).userId;
				writer.write(userID+":"+this.thetaUK[uid][kid]+",");
			}
			writer.write("\n");
		}
		writer.close();
		
		//orderedVersion
		writer = new BufferedWriter(new FileWriter(outputPath+ "thetaKU.sorted.txt"));
		for(int kid=0;kid<this.K;kid++){
			writer.write(String.format("Topic%d",kid));
			ArrayList<Map.Entry<String, Double>> dp= new ArrayList<Map.Entry<String, Double>>();
			for(int uid=0;uid<this.U;uid++){
				String userid=this.trainSet.users.get(uid).userId;
				//AbstractMap.SimpleEntry<String, Integer>("exmpleString", 42);
				Map.Entry<String, Double> pairs =new  AbstractMap.SimpleEntry<String , Double> (userid,this.thetaKU[kid][uid]);
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
		
		
		
		
		writer = new BufferedWriter(new FileWriter(outputPath+ "thetaKW.txt"));
		for(int kid=0;kid<this.K;kid++){
			writer.write(String.format("Topic%d",kid));
			for(int wid=0;wid<this.W;wid++){
				String word=this.trainSet.indexToTermMap.get(wid);
				writer.write(word+":"+this.thetaKW[kid][wid]+"\t");
			}
			writer.write("\n");
		}
		writer.close();
		
		//ordered version.
		writer = new BufferedWriter(new FileWriter(outputPath+ "thetaKW.sorted.txt"));
		for(int kid=0;kid<this.K;kid++){
			writer.write(String.format("Topic%d",kid));
			ArrayList<Map.Entry<String, Double>> dp= new ArrayList<Map.Entry<String, Double>>();
			for(int wid=0;wid<this.W;wid++){
				String word=this.trainSet.indexToTermMap.get(wid);
				//AbstractMap.SimpleEntry<String, Integer>("exmpleString", 42);
				Map.Entry<String, Double> pairs =new  AbstractMap.SimpleEntry<String , Double> (word,this.thetaKW[kid][wid]);
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
		
		
		writer = new BufferedWriter(new FileWriter(outputPath+ "thetaKC.txt"));
		for(int kid=0;kid<this.K;kid++){
			writer.write(String.format("Topic%d",kid));
			for(int cid=0;cid<this.C;cid++){
				String category=this.trainSet.indexToTagMap.get(cid);
				writer.write(category+":"+this.thetaKC[kid][cid]+"\t");
			}
			writer.write("\n");
		}
		writer.close();
		
		//ordered version.
		writer = new BufferedWriter(new FileWriter(outputPath+ "thetaKC.sorted.txt"));
		for(int kid=0;kid<this.K;kid++){
			writer.write(String.format("Topic%d",kid));
			ArrayList<Map.Entry<String, Double>> dp= new ArrayList<Map.Entry<String, Double>>();
			for(int cid=0;cid<this.C;cid++){
				String category=this.trainSet.indexToTagMap.get(cid);
				//AbstractMap.SimpleEntry<String, Integer>("exmpleString", 42);
				Map.Entry<String, Double> pairs =new  AbstractMap.SimpleEntry<String , Double> (category,this.thetaKC[kid][cid]);
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
		
		
		
	
		System.out.println("done");
		writer.close();
		
	}
}
