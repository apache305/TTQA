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

	
	
	
	
	public GrosToT(DataWoker trainUsers, DataWoker testUsers){
		this.setDefaultParameteres();
		this.trainSet=trainUsers;
		this.testSet=testUsers;
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
		this.iterNum=100;
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
			ArrayList<AnswerPost> anses = u.answerPosts;
			this.topicLabel[i]= new int[anses.size()	];
			this.groupLabel[i]= new int [anses.size()];
			
			for(int j=0;j<anses.size();j++){
				int initialTopicLabel = r.nextInt(this.K);//0 to K-1
				this.topicLabel[i][j]=initialTopicLabel;
				int initialGroupLabel = r.nextInt(this.G);// 0 to G-1
				this.groupLabel[i][j]=initialGroupLabel;
					
						
				
				AnswerPost eachPost= anses.get(j);
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
			System.out.println(String.format("Round:%d", it));
			
			//first sample gi
			
			
			
			for(int i=0;i<this.U;i++ ){
				User u=this.trainSet.users.get(i);
				ArrayList<AnswerPost> anses = u.answerPosts;
				for(int j=0;j<anses.size();j++){
					AnswerPost eachPost= anses.get(j);
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
			backupProb[g]  =  ( this.ngk[g][oldTopicID] + this.a )/(this.sumgk[g] + this.K*this.a);
			backupProb[g]  =  ( this.nkgt[oldTopicID][g][timeID]+this.d)/(this.sumkgt[oldTopicID][g] +this.T*this.d);
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
	
	public void computePer(){
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
			uid=this.trainSet.useridToIndex.get(u.userId);
			//System.out.println(u.userId);
			//System.out.println(u.answerPosts.size());
			
			for(AnswerPost eachPost: u.answerPosts){
				//int [] tids = eachPost.Qtags;
				
				//compute for each post.
				double curPostW=0.0;
				

				ArrayList<Integer> fakewords=eachPost.words;
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

				
				//for each post
				double forAllW=0.0;
				
				
				for(int wid:realwords){
					double prob_word=0;
					for(int group_id=0;group_id<this.G;group_id++){
						for(int topic_id=0;topic_id<this.K;topic_id++){
							double ugk=this.thetaUG[uid][group_id] * this.thetaGK[group_id][topic_id] ;
							ugk*= this.thetaKV[topic_id][wid];
							prob_word+=ugk;
						}
					}
					forAllW+=Math.log(prob_word);
				}
				total_result +=forAllW;
				word_number+=word_n;
				
				
				
			}
			
			//break;

		}
		
		final_perplex =  Math.exp(-1.0  *  total_result  / (double)word_number);
		System.out.println(final_perplex);
		
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
		writer = new BufferedWriter(new FileWriter(outputPath+ "thetaKW.txt"));
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
		writer = new BufferedWriter(new FileWriter(outputPath+ "thetaKW.sorted.txt"));
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
