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
		this.a1=(float) 50.0/(float)this.K;
		this.a2=this.a1;
		this.b1=0.01f;
		this.b2=0.01f;
		this.lambda=0.01f;
		this.delta=0.01f;
		this.gamma=0.001f;
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
			ArrayList<Post> anses = u.allPosts;
			this.topicLabel[i]= new int[anses.size()	];
			for(int j=0;j<anses.size();j++){
				int initialTopicLabel = r.nextInt(this.K);//0 to K-1
				this.topicLabel[i][j]=initialTopicLabel;
				
				Post eachPost= anses.get(j);
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
				ArrayList<Post> anses = u.allPosts;
				for(int j=0;j<anses.size();j++){
					Post eachPost= anses.get(j);
					
					int timeID=eachPost.dateid;
					//System.out.println(timeID+":"+eachPost.date);
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
		if(tags==null){
			System.out.println("why");
		}
		
		
		
		
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
				
				int freq=wordFreq.get(word);
				for(int eachFreq=0;eachFreq<freq;eachFreq++){
					backupProb[k]*=(this.nkw[k][word]+this.delta+eachFreq)
							/(this.sumkw[k]+this.delta*this.W + count);
					count++;
				}
				
			}


			//if only keep this, good result.
			backupProb[k] *= ( this.nkt[k][timeID] + this.b2 )/(this.sumkt[k] + this.T*this.b2 ) ;
			
			//backupProb[k] *= ( this.nku[k][uid] + this.a1 )/(this.sumku[k] + this.U*this.a1 ) ;
		
			//indeed, if add this, perplex will increase. 
			//backupProb[k] *= ( this.nukt[uid][k][timeID] + this.b2 )/(this.sumukt[uid][k] + this.T*this.b2 ) ;
			backupProb[k] *= ( this.nuke[uid][k][expLevel] + this.eta )/(this.sumuke[uid][k] + this.E*this.eta ) ;

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
	
	public void saveModel(String outputPath) throws IOException{
		
		FileTool.write2DArray(this.thetaUK	,outputPath+"thetaUK.txt");
		FileTool.write2DArray(this.thetaKU	,outputPath+ "thetaKU.txt");
		FileTool.write2DArray(this.thetaKW	,outputPath+ "thetaKW.txt");
		FileTool.write2DArray(this.thetaKV	,outputPath+ "thetaKV.txt");
		FileTool.write3DArray(this.thetaUKE	,outputPath+ "thetaUKE.txt");

		
	}
	
	public void readModel(String outputPath)throws IOException{
		
		
		
		
		this.thetaUK	=FileTool.read2DArray(outputPath+"thetaUK.txt");
		this.thetaKU=FileTool.read2DArray(outputPath+ "thetaKU.txt");
		this.thetaKW=FileTool.read2DArray(outputPath+ "thetaKW.txt");
		this.thetaKV=FileTool.read2DArray(outputPath+ "thetaKV.txt");
		this.thetaUKE=FileTool.read3DArray(outputPath+ "thetaUKE.txt");
		

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
				System.out.println("no user?");
				continue;
			}
			UinTrain++;
			uid=this.trainSet.useridToIndex.get(u.userId);
			//System.out.println(u.userId);
			//System.out.println(u.answerPosts.size());
			
			for(Post eachPost: u.allPosts){
				
				//ArrayList<Integer> faketags= eachPost.tags;
				ArrayList<Integer> fakewords=eachPost.words;
				String postid=eachPost.id;
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
					//System.out.println(postid);
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
		
		for(int tid:p.tags){
			String td= this.testSet.indexToTagMap.get(tid);
			if (this.trainSet.tagToIndexMap.containsKey(td)){
				int realTid= this.trainSet.tagToIndexMap.get(td);
				for(int i=0;i<this.K;i++){
					thetaqKV[i]+=  this.thetaKV[i][realTid];
				}
			}
		}

		sum=0.0f;
		for(int i=0;i<this.K;i++){
			if(thetaqKV[i]!=0.0){
				thetaQK[i]=thetaqUK[i]* thetaqKW[i]*thetaqKV[i];
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
	
	public double [] computeQuestionTopicDistributionWithoutUser(QuestionPost p){
		double [] thetaQK= new double [this.K];
		
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
		
		for(int tid:p.tags){
			String td= this.testSet.indexToTagMap.get(tid);
			if (this.trainSet.tagToIndexMap.containsKey(td)){
				int realTid= this.trainSet.tagToIndexMap.get(td);
				for(int i=0;i<this.K;i++){
					thetaqKV[i]+=  this.thetaKV[i][realTid];
				}
			}
		}

		sum=0.0f;
		for(int i=0;i<this.K;i++){
			if(thetaqKV[i]!=0.0){
				thetaQK[i]= thetaqKW[i]*thetaqKV[i];
				sum+=thetaQK[i];
			}else{
				thetaQK[i]= thetaqKW[i];//*thetaqKV[i];
				sum+=thetaQK[i];
			}
			
		}
		//normalize
		
		for(int i=0;i<this.K;i++){
			thetaQK[i]=  thetaQK[i]/sum;
		}
		
		
		
		return thetaQK;

		
	}
	
	class UserSimiAct{
		public Double simiscore;
		public Double actscore;
		public String userid;
		UserSimiAct(String userid, Double simiscore, Double actscore){
			this.userid=userid;
			this.simiscore=simiscore;
			this.actscore=actscore;
			
		}
	}
	
public void recommendUserForQuestion(QuestionPost q,int numOfAnswer, double[] precision, double[] recall, int [] msc){
		
		
		/*Set<String> randomUids= this.trainSet.useridToIndex.keySet();
		ArrayList<String> randomList=new ArrayList<String>();
		for(String ruid : randomUids){
			randomList.add(ruid);
		}*/
		
		double [] thetaQK= this.computeQuestionTopicDistribution(q);
		
		//score = (1-js) * expert(u,q) * act (u,q)
		
		ArrayList<Map.Entry<String, Double>> userScore= new ArrayList<Map.Entry<String, Double>>();
		for(User u:this.trainSet.users){
			int uindex=this.trainSet.useridToIndex.get(u.userId);
			double [] thetacUK=this.thetaUK[uindex];

			

			double jsdis= CommonUtil.jensenShannonDivergence(thetacUK, thetaQK);
			double actscore=0.0f;
			for(int j=0;j<this.K;j++){
				actscore=actscore+(thetaQK[j]* this.thetaKU[j][uindex]);
			}
			
			double uscore= (1-jsdis);//*actscore;
			//System.out.println(actscore);
			
			Map.Entry<String, Double> pairs =new  AbstractMap.SimpleEntry<String , Double> (u.userId,uscore);
			userScore.add(pairs);
			//System.out.println("jsdis:"+jsdis);
		}
		//sort it.

		Collections.sort(userScore, new Comparator<Entry<String,Double>>(){
			public int compare(Entry<String, Double> arg0,Entry<String, Double> arg1) {
				// TODO Auto-generated method stub
				return -1*arg0.getValue().compareTo(arg1.getValue());
			}
		});
		
		
		//get top 50 users.
		ArrayList<String> topUsers = new ArrayList<String >();
		for(int i=0;i<50;i++){
			topUsers.add(userScore.get(i).getKey());
			//System.out.println(userSimiScore.get(i).getValue());
		}


		//check p@5 p@10 p@15 //
		Set<String> ansUids = new HashSet<String>();
		for(AnswerPost a: q.answers){
			ansUids.add(a.user.userId);
		}
		/*ArrayList<String> randomRec=new ArrayList<String>();
		
		Collections.shuffle(randomList);
		for(int i=0;i<50;i++){
			randomRec.add(randomList.get(i));
		}
		
		
		topUsers=randomRec;*/
		
		
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
	
	public void recommendUserForQuestionOLDACT(QuestionPost q,int numOfAnswer, double[] precision, double[] recall, int [] msc){
		
		
		/*Set<String> randomUids= this.trainSet.useridToIndex.keySet();
		ArrayList<String> randomList=new ArrayList<String>();
		for(String ruid : randomUids){
			randomList.add(ruid);
		}*/
		
		double [] thetaQK= this.computeQuestionTopicDistribution(q);
		//get the big k
		ArrayList<Map.Entry<Integer, Double>> idqk= new ArrayList<Map.Entry<Integer, Double>>();
		for(int i=0;i<this.K;i++){
			idqk.add (new  AbstractMap.SimpleEntry<Integer , Double> (i,thetaQK[i]));
		}
		Collections.sort(idqk, new Comparator<Entry<Integer,Double>>(){
			public int compare(Entry<Integer, Double> arg0,Entry<Integer, Double> arg1) {
				// TODO Auto-generated method stub
				return -1*arg0.getValue().compareTo(arg1.getValue());
			}
		});
		///ArrayList<String> RandomUsers = new ArrayList<String>();
		ArrayList<UserSimiAct>  userSimiActs = new ArrayList<UserSimiAct>();
	
		for(User u:this.trainSet.users){
			int uindex=this.trainSet.useridToIndex.get(u.userId);
			double [] thetacUK=this.thetaUK[uindex];

			

			double jsdis= CommonUtil.jensenShannonDivergence(thetacUK, thetaQK);
			double actscore=0.0f;
			for(int topI=0;topI<2;topI++){
				int maxt= idqk.get(topI).getKey();
				actscore=  actscore + ( thetaQK[maxt]*this.thetaKU[maxt][uindex]	  );
			}
			/*for(int j=0;j<K;j++){
				actscore=actscore+(thetaQK[j]* this.thetaKU[j][uindex]);
			}*/
			//System.out.println(actscore);
			
			UserSimiAct newUserSimiAct= new UserSimiAct(u.userId,1.0-jsdis,actscore);
			//UserSimiAct newUserSimiAct= new UserSimiAct(u.userId,1.0-jsdis,this.thetaKU[maxTopicId][uindex]);
			userSimiActs.add(newUserSimiAct);

			
			//System.out.println("jsdis:"+jsdis);
		}
		//sort it.

		Collections.sort(userSimiActs, new Comparator<UserSimiAct>(){
			public int compare(UserSimiAct arg0,UserSimiAct arg1) {
				// TODO Auto-generated method stub
				return -1* arg0.simiscore.compareTo(arg1.simiscore);
			}
		});
		
		//get top 50 users.
		ArrayList<UserSimiAct>  top50UserSimiActs=new ArrayList<UserSimiAct>();

		for(int i=0;i<50;i++){
			top50UserSimiActs.add(userSimiActs.get(i));
		}
		Collections.sort(top50UserSimiActs, new Comparator<UserSimiAct>(){
			public int compare(UserSimiAct arg0,UserSimiAct arg1) {
				// TODO Auto-generated method stub
				return -1* arg0.actscore.compareTo(arg1.actscore);
			}
		});
		
		//get top 50 users.
		ArrayList<String> topUsers =new ArrayList<String>();
		for(int i=0;i<50;i++){
			topUsers.add( top50UserSimiActs.get(i).userid  );
		}

		//check p@5 p@10 p@15 //
		Set<String> ansUids = new HashSet<String>();
		for(AnswerPost a: q.answers){
			ansUids.add(a.user.userId);
		}
		/*ArrayList<String> randomRec=new ArrayList<String>();
		
		Collections.shuffle(randomList);
		for(int i=0;i<50;i++){
			randomRec.add(randomList.get(i));
		}
		
		
		topUsers=randomRec;*/
		
		
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
	
	
	
	

	
	public void recommendUserForQuestionNOACT(QuestionPost q,int numOfAnswer, double[] precision, double[] recall, int [] msc){
			
		double [] thetaQK= this.computeQuestionTopicDistribution(q);
		
		///ArrayList<String> RandomUsers = new ArrayList<String>();
		
		ArrayList<Map.Entry<String, Double>> userSimiScore= new ArrayList<Map.Entry<String, Double>>();
		for(User u:this.trainSet.users){
			int uindex=this.trainSet.useridToIndex.get(u.userId);
			double [] thetacUK=this.thetaUK[uindex];
			//double sum=0.0f;
			
			//if (r.nextBoolean()){
			//	RandomUsers.add(u.userId);
			//}
			
			/*double jsdis1= CommonUtil.jensenShannonDivergence(thetacUK, thetaQK);
			
			double sum=0.0f;
			//this part is activiy part.
			for(int i=0;i<this.K;i++){
				thetacUK[i]*=this.thetaKU[i][uindex];
				sum+=thetacUK[i];
			}
			
			//normalize again...should
			for(int i=0;i<this.K;i++){
				thetacUK[i]=thetacUK[i]/sum;
			}*/
			
			
			

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
	//public 
	class WhySoComplicate{
		int uid;
		Double distance;
		WhySoComplicate(int uid, double distance){
			this.uid=uid;
			this.distance=distance;
			
		}
	}
	
	public void oneThingINeedToMakeSure(){
		double [] ku = this.thetaKU[0];//topic 0

			ArrayList<Map.Entry<Integer, Double>> dp= new ArrayList<Map.Entry<Integer, Double>>();
			for(int uid=0;uid<this.U;uid++){
				//String userid=this.trainSet.users.get(uid).userId;
				
				//AbstractMap.SimpleEntry<String, Integer>("exmpleString", 42);
				Map.Entry<Integer, Double> pairs =new  AbstractMap.SimpleEntry<Integer , Double> (uid,ku[uid]);
				dp.add(pairs);
			}
			Collections.sort(dp, new Comparator<Entry<Integer,Double>>(){
				public int compare(Entry<Integer, Double> arg0,Entry<Integer, Double> arg1) {
					// TODO Auto-generated method stub
					return -1*arg0.getValue().compareTo(arg1.getValue());
				}
			});
			for(int i=0;i<10;i++){
				//only output top 10;
				int uid= dp.get(i).getKey();
				double pob=dp.get(i).getValue();
				for(int j=0;j<this.K;j++){
					System.out.print(this.nuk[uid][j]);
					System.out.print(",");
					
				}
				System.out.print(pob);
				System.out.print("\n");

			}
			int l=dp.size();
			for(int i=0;i<10;i++){
				//only output top 10;
				int uid= dp.get(l-i-1).getKey();
				double pob=dp.get(l-i-1).getValue();
				for(int j=0;j<this.K;j++){
					System.out.print(this.nuk[uid][j]);
					System.out.print(",");
					
				}
				System.out.print(pob);
				System.out.print("\n");

			}

		

		
	}
	
	
	
	
	public void computeTimeLikelihood(){
		 
		
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
		
		writer = new BufferedWriter(new FileWriter(outputPath+ "thetaKTU.txt"));
		for(int kid=0;kid<this.K;kid++){
			writer.write(String.format("Topic%d,",kid));
			for(int tid=0;tid<this.T;tid++){
				String timeLabel = this.trainSet.indexToTimeMap.get(tid);
				writer.write(String.format("TimeID%s,",timeLabel));
				for(int uid=0;uid<this.U;uid++){
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
//top 10% votes.
	public void topVoteHit(QuestionPost q,int numOfAnswer, double[] precision, double[] recall, int [] msc){
		
		double [] thetaQK= this.computeQuestionTopicDistribution(q);

		//score = (1-js) * expert(u,q) * act (u,q)

		ArrayList<Map.Entry<String, Double>> userScore= new ArrayList<Map.Entry<String, Double>>();
		for(User u:this.trainSet.users){
			int uindex=this.trainSet.useridToIndex.get(u.userId);
			double [] thetacUK=this.thetaUK[uindex];


			double jsdis= CommonUtil.jensenShannonDivergence(thetacUK, thetaQK);
			double actscore=0.0f;
			for(int j=0;j<this.K;j++){
				actscore=actscore+(thetaQK[j]* this.thetaKU[j][uindex]);
			}
			
			
			//avg level for user.
			double []klevel= new double[this.K];
			for(int j=0;j<this.K;j++){
				for(int el=0;el<this.E;el++){
					klevel[j]=this.thetaUKE[uindex][j][el] * el;
					//    1 0.1   10 0.9       u,j  5.3
				}
			}//
			//[3,5,1,......9]
			
			
			
			double expscore=0.0f;
			for(int j=0;j<this.K;j++){
				expscore += (thetaQK[j]* klevel[j]	 );

			}
			System.out.println("exp"+expscore);
			
			double uscore= (1-jsdis)*actscore*expscore;
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

	
public void maxVoteHit1step(QuestionPost q, int [] mvh){
	
double [] thetaQK= this.computeQuestionTopicDistribution(q);

//score = (1-js) * expert(u,q) * act (u,q)

ArrayList<Map.Entry<String, Double>> userScore= new ArrayList<Map.Entry<String, Double>>();
for(User u:this.trainSet.users){
	int uindex=this.trainSet.useridToIndex.get(u.userId);
	double [] thetacUK=this.thetaUK[uindex];


	double jsdis= CommonUtil.jensenShannonDivergence(thetacUK, thetaQK);
	double actscore=0.0f;
	for(int j=0;j<this.K;j++){
		actscore=actscore+(thetaQK[j]* this.thetaKU[j][uindex]);
	}
	
	
	//avg level for user.
	double []klevel= new double[this.K];
	for(int j=0;j<this.K;j++){
		for(int el=0;el<this.E;el++){
			klevel[j]=this.thetaUKE[uindex][j][el] * el;
			//    1 0.1   10 0.9       u,j  5.3
		}
	}//
	//[3,5,1,......9]
	
	
	
	double expscore=0.0f;
	for(int j=0;j<this.K;j++){
		expscore += (thetaQK[j]* klevel[j]	 );

	}
	
	double uscore= (1-jsdis)*actscore*expscore;
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
	
	return;
	


	
}



public void maxVoteHit(QuestionPost q, int [] mvh){
	
double [] thetaQK= this.computeQuestionTopicDistribution(q);

//score = (1-js) * expert(u,q) * act (u,q)

ArrayList<Map.Entry<String, Double>> userScore= new ArrayList<Map.Entry<String, Double>>();
for(User u:this.trainSet.users){
	int uindex=this.trainSet.useridToIndex.get(u.userId);
	double [] thetacUK=this.thetaUK[uindex];


	double jsdis= CommonUtil.jensenShannonDivergence(thetacUK, thetaQK);
	double actscore=0.0f;
	for(int j=0;j<this.K;j++){
		actscore=actscore+(thetaQK[j]* this.thetaKU[j][uindex]);
	}
	
	
	
	double uscore= (1-jsdis)*actscore;//*expscore;
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
	
	
	
	
	
	
	// second step, ranking them by expertise.
	ArrayList<String> firstStepTopUsers= new ArrayList<String>();
	for(int i=0;i<100;i++){
		firstStepTopUsers.add(userScore.get(i).getKey());
	}
	//now we have 100 top active users. re-rank them.
	
	//ArrayList<Double> topUsersExpScores= new ArrayList<Double>();
	//only for sort.
	//
	ArrayList<Map.Entry<String, Double>> topUsersExpScores =
			new ArrayList<Map.Entry<String,Double>> ();
	
	
	for(String topuid : firstStepTopUsers){
		//compute exp
		int uindex=this.trainSet.useridToIndex.get(topuid);
		double [] thetacUK=this.thetaUK[uindex];

		//avg level for user.
			double []klevel= new double[this.K];
			for(int j=0;j<this.K;j++){
				for(int el=0;el<this.E;el++){
					klevel[j]=this.thetaUKE[uindex][j][el] * el;
					//    1 0.1   10 0.9       u,j  5.3
				}
			}//
			//[3,5,1,......9]
			
			
			
			double expscore=0.0f;
			for(int j=0;j<this.K;j++){
				expscore += (thetaQK[j]* klevel[j]	 );

			}
			
			Map.Entry<String, Double> pairs =new  AbstractMap.SimpleEntry<String , Double> (topuid,expscore);
			topUsersExpScores.add(pairs);
			
			//topUsersExpScores
			//coresExpScores.add(expscore);

		}
	Collections.sort(topUsersExpScores, new Comparator<Entry<String,Double>>(){
		public int compare(Entry<String, Double> arg0,Entry<String, Double> arg1) {
			// TODO Auto-generated method stub
			return -1*arg0.getValue().compareTo(arg1.getValue());
		}
	});
	
	
	
	// now we have a reranked user list.
	

	
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
		String recUid= topUsersExpScores.get(i).getKey();
		if(maxuids.contains(recUid)){
			mvh[i/10]+=1;
			return ;
		}
	}
	
	return;
	


	
}




public void HitAndRank(QuestionPost q, Set<String> ansUids, ArrayList<Map.Entry<String,Integer>> sortedVotes, Map<String,Integer> votesMap, double [] totalNDCG){
		
		
	double [] thetaQK= this.computeQuestionTopicDistribution(q);
	
	//score = (1-js) * expert(u,q) * act (u,q)
	
	ArrayList<Map.Entry<String, Double>> userScore= new ArrayList<Map.Entry<String, Double>>();
	for(String uid: ansUids){
		int uindex=this.trainSet.useridToIndex.get(uid);
		double [] thetacUK=this.thetaUK[uindex];


		double jsdis= CommonUtil.jensenShannonDivergence(thetacUK, thetaQK);

		
		//avg level for user.
		double []klevel= new double[this.K];
		for(int j=0;j<this.K;j++){
			for(int el=0;el<this.E;el++){
				klevel[j]=this.thetaUKE[uindex][j][el] * el;
				//    1 0.1   10 0.9       u,j  5.3
			}
		}//
		//[3,5,1,......9]
		

		
		double expscore=0.0f;
		for(int j=0;j<this.K;j++){
			expscore += (thetaQK[j]* klevel[j]	 );

		}
		
		double uscore= (1-jsdis)*expscore;
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
		double actscore=0.0f;
		for(int j=0;j<this.K;j++){
			actscore=actscore+(thetaQK[j]* this.thetaKU[j][uindex]);
		}
		
		
		//avg level for user.
		double []klevel= new double[this.K];
		for(int j=0;j<this.K;j++){
			for(int el=0;el<this.E;el++){
				klevel[j]=this.thetaUKE[uindex][j][el] * el;
				//    1 0.1   10 0.9       u,j  5.3
			}
		}//
		//[3,5,1,......9]
		
		
		
		double expscore=0.0f;
		for(int j=0;j<this.K;j++){
			expscore += (thetaQK[j]* klevel[j]	 );

		}
		
		double uscore= (1-jsdis)*expscore;
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
	
	
	
}
