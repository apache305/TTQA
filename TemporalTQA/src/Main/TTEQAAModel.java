package Main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

public class TTEQAAModel extends LDABasedModel{
	
	float a1;//alpha
	float a2;//alpha
	float b1;//beta
	float b2;//beta
	float delta;//delta
	float lambda;//lambda
	float eta;//
	float gamma;
	int U;//user number
	int K;//topic number
	int V;//tag number
	int W;//word number
	int T;//time number
	int E; //vote level number;

	
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

	double [][] thetaKV;// topic - tag distribution  K*V
	int [][] nkv;//number of topic k in tag j; K*V
	int [] sumkv;//sum for each topic. K
	
	double [][] thetaKW;//topic -word distribution K*W
	int [][] nkw;//number of topic k in word j; K*W
	int [] sumkw;//number of topic k in word j; K
	
	
	double [][] thetaKT;//  topic - time distribution K*T 
	int [][] nkt;//number of topic k in time j; K*T
	int [] sumkt;//sum for each topic. K
	
	double [][] thetaTK;
	int [][] ntk;
	int [] sumtk;
	
	
	double [][][] thetaUKT;// topic -time per user. distribution  U*K*T;
	int [][][] nukt;//number of user i 's topic j in time k; U*K*T
	int [][] sumukt;//sum of user i's each topic. U*K;
	
	double [][][] thetaKTU;
	int [][][] nktu;
	int [][] sumktu;
	
	double [][][] thetaKEU;
	int [][][] nkeu;
	int [][] sumkeu;
	
	
	double [][][] thetaUKE;// topic -expertise per user. distribution U*K*E;
	int [][][] nuke;
	int [][] sumuke;
	
	
	//each users' each posts' topic label.  only for answer post.
	int [][] topicLabel;
	
	
	
	
	
	public TTEQAAModel(DataWoker trainUsers, DataWoker testUsers,int iternum){
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
		this.a1=(float) 50.0/(float)this.K;
		this.a2=this.a1;
		this.b1=0.01f;
		this.b2=0.01f;
		this.lambda=0.01f;
		this.delta=0.01f;
		this.gamma=0.01f;
		this.eta=0.01f;

		this.iterNum=100;
	}
	
	
	public void initModel(){
		//init those probabilities;
		this.U= this.trainSet.users.size();//number of user.
		this.T= this.trainSet.timeCountMap.size();//number of time label
		this.V= this.trainSet.tagCountMap.size();//number of tag
		this.W= this.trainSet.termCountMap.size();//number of words
		this.E= this.trainSet.voteStep;//number of expertise.//
		this.thetaUK = new double [this.U][this.K];
		this.nuk= new int[this.U][this.K];
		this.sumuk= new int[this.U];
		
		this.thetaKU = new double [this.K][this.U];
		this.nku= new int[this.K][this.U];
		this.sumku= new int[this.K];
		
		this.thetaKV= new double[this.K][this.V];
		this.nkv=new int [this.K][this.V];
		this.sumkv= new int[this.K];
		
		this.thetaKW=new double[this.K][this.W];//topic -word distribution K*W
		this.nkw=new int [this.K][this.W];//number of topic k in word j; K*W
		this.sumkw =new int[this.K];//number of topic k in word j; K
		
		this.thetaKT=new double[this.K][this.T];
		this.nkt= new int[this.K][this.T];
		this.sumkt=new int[this.K];
		
		this.thetaTK = new double[this.T][this.K];
		this.ntk = new int[this.T][this.K];
		this.sumtk = new int[this.T];
		
		this.thetaUKT=new double[this.U][this.K][this.T];
		this.nukt= new int[this.U][this.K][this.T];
		this.sumukt=new int[this.U][this.K];
		
		this.thetaKTU=new double[this.K][this.T][this.U];
		this.nktu=new int[this.K][this.T][this.U];
		this.sumktu=new int[this.K][this.T];
		
		this.thetaKEU=new double[this.K][this.E][this.U];
		this.nkeu=new int[this.K][this.E][this.U];
		this.sumkeu=new int[this.K][this.E];
		
		
		this.thetaUKE = new double [this.U][this.K][this.E];
		this.nuke= new int [this.U][this.K][this.E];
		this.sumuke=new int [this.U][this.K];

		
		this.topicLabel = new int [this.U][];
		Random r = new Random();
		for(int i=0;i<this.U;i++ ){
			User u=this.trainSet.users.get(i);
			ArrayList<AnswerPost> anses = u.answerPosts;
			this.topicLabel[i]= new int[anses.size()	];
			for(int j=0;j<anses.size();j++){
				int initialTopicLabel = r.nextInt(this.K);//0 to K-1
				this.topicLabel[i][j]=initialTopicLabel;
				
				AnswerPost eachPost= anses.get(j);
				//update those counts.
				this.nuk[i][initialTopicLabel]++;
				this.sumuk[i]++;
				
				this.nku[initialTopicLabel][i]++;
				this.sumku[initialTopicLabel]++;
				
				
				int timeID=eachPost.dateid;
				this.nkt[initialTopicLabel][timeID]++;
				this.sumkt[initialTopicLabel]++;
				
				this.ntk[timeID][initialTopicLabel]++;
				this.sumtk[timeID]++;
				
				this.nukt[i][initialTopicLabel][timeID]++;
				this.sumukt[i][initialTopicLabel]++;
				
				this.nktu[initialTopicLabel][timeID][i]++;
				this.sumktu[initialTopicLabel][timeID]++;
				
			
				
				int expertiseLevel = eachPost.vote_level;
				this.nuke[i][initialTopicLabel][expertiseLevel]++;
				this.sumuke[i][initialTopicLabel]++;
				
				this.nkeu[initialTopicLabel][expertiseLevel][i]++;
				this.sumkeu[initialTopicLabel][expertiseLevel]++;
				
				
				//for each tag
				for(int tagID:eachPost.tags){
					this.nkv[initialTopicLabel][tagID]++;
					this.sumkv[initialTopicLabel]++;
				}
				
				//for each word
				for(int wordID:eachPost.words){
					this.nkw[initialTopicLabel][wordID]++;
					this.sumkw[initialTopicLabel]++;
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
				ArrayList<AnswerPost> anses = u.answerPosts;
				for(int j=0;j<anses.size();j++){
					AnswerPost eachPost= anses.get(j);
					int timeID=eachPost.dateid;
					ArrayList<Integer> tags= eachPost.tags ;
					ArrayList<Integer> words = eachPost.words;
					int expLevel = eachPost.vote_level;
					int newTopicLabel = this.gibbsSample(i,j,tags,words, timeID,expLevel);
					this.topicLabel[i][j]=newTopicLabel;
					
				}
			}
		}
	}
	
	public int gibbsSample(int uid,int pid,ArrayList<Integer>tags,ArrayList<Integer> words, int timeID,int expLevel){
		int oldTopicID=this.topicLabel[uid][pid];
		
		//remove current stuff.
		this.nuk[uid][oldTopicID]--;
		this.sumuk[uid]--;
		
		this.nku[oldTopicID][uid]--;
		this.sumku[oldTopicID]--;
		
		
		
		
		for(int eachTagID: tags){
			this.nkv[oldTopicID][eachTagID]--;
			this.sumkv[oldTopicID]--;
		}
		
		//need counter for this words.
		Map<Integer,Integer> wordFreq = new HashMap<Integer,Integer>();
		
		for(int eachWord:words){
			this.nkw[oldTopicID][eachWord]--;
			this.sumkw[oldTopicID]--;
			if(!wordFreq.containsKey(eachWord)){
				wordFreq.put(eachWord,0);
			}
			int oldc=wordFreq.get(eachWord);
			wordFreq.put(eachWord, oldc+1);
		}
		
		
		
		
		this.nkt[oldTopicID][timeID]--;
		this.sumkt[oldTopicID]--;
		
		this.ntk[timeID][oldTopicID]--;
		this.sumtk[timeID]--;
		
		this.nukt[uid][oldTopicID][timeID]--;
		this.sumukt[uid][oldTopicID]--;
		this.nktu[oldTopicID][timeID][uid]--;
		this.sumktu[oldTopicID][timeID]--;
		
		this.nuke[uid][oldTopicID][expLevel]--;
		this.sumuke[uid][oldTopicID]--;
		this.nkeu[oldTopicID][expLevel][uid]--;
		this.sumkeu[oldTopicID][expLevel]--;
		
		//souihaite ca marche.
		double [] backupProb =  new double [this.K];
		
		int tagL=tags.size();
		for(int k=0;k<this.K;k++){
			backupProb[k]  =  ( this.nuk[uid][k] + this.a1 )/(this.sumuk[uid] + this.K*this.a1 ) ;
			
			
			//should modify this, seems like it is not correct
			//for(int eachTagID:tags){  // if remove this, can not detect topic. tested!
				//backupProb[k] *=  ( this.nkv[k][eachTagID] + tagL+ this.b )/(this.sumkv[k] + tagL+ this.V*this.b ) ;
			//}
			int count=0;
			for(int tag:tags){
				backupProb[k]*=(this.nkv[k][tag]+this.gamma)/(this.sumkv[k]+this.gamma*this.V+count );
				count++;
			}
			count=0;
			for(int word: wordFreq.keySet()){
				//
				int freq=wordFreq.get(word);
				for(int eachFreq=0;eachFreq<freq;eachFreq++){
					backupProb[k]*=(this.nkw[k][word]+this.delta+eachFreq)
							/(this.sumkw[k]+this.delta*this.W + count);
					count++;
				}
				
			}


			//if only keep this, good result.
			//backupProb[k] *= ( this.nkt[k][timeID] + this.b2 )/(this.sumkt[k] + this.T*this.b2 ) ;
			
			backupProb[k] *= ( this.nku[k][uid] + this.a1 )/(this.sumku[k] + this.U*this.a1 ) ;
		
			//indeed, if add this, perplex will increase. 
			//backupProb[k] *= ( this.nukt[uid][k][timeID] + this.b2 )/(this.sumukt[uid][k] + this.T*this.b2 ) ;
			//backupProb[k] *= ( this.nuke[uid][k][expLevel] + this.eta )/(this.sumuke[uid][k] + this.E*this.eta ) ;

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
		this.topicLabel[uid][pid]=newSampledTopic;
		
		//update current stuff.
		this.nuk[uid][newSampledTopic]++;
		this.sumuk[uid]++;
		
		this.nku[newSampledTopic][uid]++;
		this.sumku[newSampledTopic]++;
		
		for(int eachTagID: tags){
			this.nkv[newSampledTopic][eachTagID]++;
			this.sumkv[newSampledTopic]++;
		}
		for(int word: words){
			this.nkw[newSampledTopic][word]++;
			this.sumkw[newSampledTopic]++;
		}
		
		this.nkt[newSampledTopic][timeID]++;
		this.sumkt[newSampledTopic]++;
		
		this.ntk[timeID][newSampledTopic]++;
		this.sumtk[timeID]++;
		
		this.nukt[uid][newSampledTopic][timeID]++;
		this.sumukt[uid][newSampledTopic]++;
		this.nktu[newSampledTopic][timeID][uid]++;
		this.sumktu[newSampledTopic][timeID]++;
		
		this.nuke[uid][newSampledTopic][expLevel]++;
		this.sumuke[uid][newSampledTopic]++;
		
		this.nkeu[newSampledTopic][expLevel][uid]++;
		this.sumkeu[newSampledTopic][expLevel]++;
		
		return newSampledTopic;
	}
	
	public void estimateProb(){
		
		//thetaUK
		for(int uid = 0;uid<this.U;uid++){
			for(int kid =0 ;kid<this.K;kid++){
				this.thetaUK[uid][kid]=( this.nuk[uid][kid] + this.a1 )/(this.sumuk[uid] + this.K*this.a1 );
			}
		}
		
		for(int kid =0 ;kid<this.K;kid++){
			for(int uid = 0;uid<this.U;uid++){
				this.thetaKU[kid][uid]=( this.nku[kid][uid] + this.a2 )/(this.sumku[kid] + this.U*this.a2 );
			}
		}
		
		//thetaKV
		for(int kid=0;kid<this.K;kid++){
			for(int vid=0;vid<this.V;vid++){
				this.thetaKV[kid][vid]=(this.nkv[kid][vid] +  this.gamma )/(this.sumkv[kid] + this.V*this.gamma);
			}
		}
		
		//thetaKW
		//fuck this bug!!!!!
		for(int kid=0;kid<this.K;kid++){
			for(int wid=0;wid<this.W;wid++){
				this.thetaKW[kid][wid]=(this.nkw[kid][wid] +  this.delta )/(this.sumkw[kid] + this.W*this.delta);
			}
		}
		
		//thetaKT
		for(int kid=0;kid<this.K;kid++){
			for(int tid=0;tid<this.T;tid++){
				this.thetaKT[kid][tid]=(this.nkt[kid][tid] + this.b2 )/(this.sumkt[kid] + this.T*this.b2);
			}
		}
		
		//thetaTK
		for(int tid=0;tid<this.T;tid++){
			for(int kid=0;kid<this.K;kid++){
				this.thetaTK[tid][kid]=(this.ntk[tid][kid] + this.b1 )/(this.sumtk[tid] + this.K*this.b1);
			}
		}
		
		//thetaKTU
		for(int kid=0;kid<this.K;kid++){
			for(int tid=0;tid<this.T;tid++){
				for(int uid=0;uid<this.U;uid++){
					this.thetaKTU[kid][tid][uid]=( this.nktu[kid][tid][uid] + this.lambda )/(this.sumktu[kid][tid] + this.U*this.lambda ) ;
				}
			}
		}
		
		//thetaKEU
		for(int kid=0;kid<this.K;kid++){
			for(int eid=0;eid<this.E;eid++){
				for(int uid=0;uid<this.U;uid++){
					this.thetaKEU[kid][eid][uid]=( this.nkeu[kid][eid][uid] + this.eta )/(this.sumkeu[kid][eid] + this.U*this.eta ) ;
				}
			}
		}
		
		
		
		
		//thetaUKT
		for(int uid=0;uid<this.U;uid++){
			for(int kid=0;kid<this.K;kid++){
				for(int tid=0;tid<this.T;tid++){
					this.thetaUKT[uid][kid][tid]=( this.nukt[uid][kid][tid] + this.lambda )/(this.sumukt[uid][kid] + this.T*this.lambda ) ;
				}
			}
		}
		
		//thetaUKE
		for(int uid=0;uid<this.U;uid++){
			for(int kid=0;kid<this.K;kid++){
				for(int eid=0;eid<this.E;eid++){
					this.thetaUKE[uid][kid][eid]=( this.nuke[uid][kid][eid] + this.lambda )/(this.sumuke[uid][kid] + this.E*this.lambda ) ;
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
	
	public void computeCoherence(){
		ArrayList<ArrayList<String>> topicTopWords= this.getTopWords();
		double total_score=0.0;
		double item=0;
		for(int kid=0;kid<this.K;kid++){
			for(int i=0;i<10;i++){
				String w1=topicTopWords.get(kid).get(i);
				int wid1=this.testSet.termToIndexMap.get(w1);
				int occ1= this.testSet.singleOccDocument[wid1];//number of document has word1.
				//System.out.println(w1+";"+occ1);
				for(int j=i+1;j<10;j++){
					
					String w2=topicTopWords.get(kid).get(j);
					int wid2=this.testSet.termToIndexMap.get(w2);
					//int occ2= this.testSet.singleOccDocument[wid2];//number of document has words2
					int cooc12=this.testSet.coOccDocument[wid1][wid2]+this.testSet.coOccDocument[wid2][wid1];
					//System.out.println(w1+";"+w2+";"+cooc12);
					
					double score= Math.log( ((double)cooc12 + 1.0) / ( (double)(occ1)) );
					total_score+=score;
					item++;
					//System.out.println(score+";"+total_score);
				}
			}
		}
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
		
		
		
		//thetaKV
		writer = new BufferedWriter(new FileWriter(outputPath+ "thetaKV.txt"));
		for(int kid=0;kid<this.K;kid++){
			writer.write(String.format("Topic%d",kid));
			for(int vid=0;vid<this.V;vid++){
				String tag=this.trainSet.indexToTagMap.get(vid);
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
				String tag=this.trainSet.indexToTagMap.get(vid);
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
		//thetaKW
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
		
		
		//thetaKT
		writer = new BufferedWriter(new FileWriter(outputPath+ "thetaKT.txt"));
		for(int kid=0;kid<this.K;kid++){
			writer.write(String.format("Topic%d,",kid));
			for(int tid=0;tid<this.T;tid++){
				
				String timeLabel = this.trainSet.indexToTimeMap.get(tid);
				//System.out.println(timeLabel);
				//writer.write(timeLabel+":"+this.thetaKT[kid][tid]+"\t");
				writer.write(this.thetaKT[kid][tid]+",");
			}
			writer.write("\n");
		}
		writer.close();
		
		//thetaTK
		writer = new BufferedWriter(new FileWriter(outputPath+ "thetaTK.txt"));
		for(int tid=0;tid<this.T;tid++){
			String timeLabel = this.trainSet.indexToTimeMap.get(tid);
			writer.write(String.format("TimeID%s,",timeLabel));
			//writer.write(String.format("TimeID%s,",kid));
			for(int kid=0;kid<this.K;kid++){
				writer.write(this.thetaTK[tid][kid]+",");
			}
			writer.write("\n");
		}
		writer.close();
		
		//thetaUKT
		writer = new BufferedWriter(new FileWriter(outputPath+ "UserthetaKT.txt"));
		for(int uid=0;uid<this.U;uid++){
			writer.write( this.trainSet.users.get(uid).userId +",");
			for(int kid=0;kid<this.K;kid++){
				writer.write(String.format("Topic%d,",kid));
				for(int tid=0;tid<this.T;tid++){
					
					writer.write(this.thetaUKT[uid][kid][tid]+",");
				}
				writer.write("\n");
			}
		}
		writer.close();
		
		//thetaUKE
		writer = new BufferedWriter(new FileWriter(outputPath+ "UserthetaKE.txt"));
		for(int uid=0;uid<this.U;uid++){
			writer.write( this.trainSet.users.get(uid).userId +",");
			for(int kid=0;kid<this.K;kid++){
				writer.write(String.format("Topic%d,",kid));
				for(int eid=0;eid<this.E;eid++){
					
					writer.write(this.thetaUKE[uid][kid][eid]+",");
				}
				writer.write("\n");
			}
		}
		System.out.println("done");
		writer.close();
		
	}
}
