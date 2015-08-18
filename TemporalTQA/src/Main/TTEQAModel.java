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

public class TTEQAModel extends LDABasedModel{
	
	float a;
	float b;
	float c;
	float d;
	float e;
	int U;//user number
	int K;//topic number
	int V;//tag number
	int T;//time number
	int E; //vote level number;
	
	Users trainU=null;
	Users testU=null;
	
	
	int iterNum;//number of iterations.
	
	double [][] thetaUK;// user - topic distribution  U*K
	int [][] nuk;// number of user i in topic j. U*K
	int [] sumuk;//sum for each user. U

	
	
	double [][] thetaKV;// topic - tag distribution  K*V
	int [][] nkv;//number of topic k in tag j; K*V
	int [] sumkv;//sum for each topic. K
	
	
	double [][] thetaKT;//  topic - time distribution K*T 
	int [][] nkt;//number of topic k in time j; K*T
	int [] sumkt;//sum for each topic. K
	
	
	double [][][] thetaUKT;// topic -time per user. distribution  U*K*T;
	int [][][] nukt;//number of user i 's topic j in time k; U*K*T
	int [][] sumukt;//sum of user i's each topic. U*K;
	
	
	double [][][] thetaUKE;// topic -expertise per user. distrbution U*K*E;
	int [][][] nuke;
	int [][] sumuke;
	
	
	//each users' each posts' topic label.
	int [][] topicLabel;
	
	
	
	
	
	public TTEQAModel(Users trainUsers, Users testUsers){
		this.setDefaultParameteres();
		this.trainU=trainUsers;
		this.testU=testUsers;
		
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
		this.b=0.001f;
		this.c=0.01f;
		this.d=0.01f;
		this.e=0.01f;
		this.iterNum=100;
	}
	
	public void initModel(){
		//init those probabilities;

		this.U= this.trainU.users.size();//number of user.
		this.T= this.trainU.timeCountMap.size();//number of time label
		this.V= this.trainU.tagCountMap.size();//number of tag
		this.E= this.trainU.voteStep;//number of expertise.
		this.thetaUK = new double [this.U][this.K];
		this.nuk= new int[this.U][this.K];
		this.sumuk= new int[this.U];
		
		this.thetaKV= new double[this.K][this.V];
		this.nkv=new int [this.K][this.V];
		this.sumkv= new int[this.K];
		
		this.thetaKT=new double[this.K][this.T];
		this.nkt= new int[this.K][this.T];
		this.sumkt=new int[this.K];
		
		this.thetaUKT=new double[this.U][this.K][this.T];
		this.nukt= new int[this.U][this.K][this.T];
		this.sumukt=new int[this.U][this.K];
		
		this.thetaUKE = new double [this.U][this.K][this.E];
		this.nuke= new int [this.U][this.K][this.E];
		this.sumuke=new int [this.U][this.K];

		
		this.topicLabel = new int [this.U][];
		Random r = new Random();
		for(int i=0;i<this.U;i++ ){
			User u=this.trainU.users.get(i);
			ArrayList<AnswerPost> anses = u.answerPosts;
			this.topicLabel[i]= new int[anses.size()	];
			for(int j=0;j<anses.size();j++){
				int initialTopicLabel = r.nextInt(this.K);//0 to K-1
				this.topicLabel[i][j]=initialTopicLabel;
				
				AnswerPost eachPost= anses.get(j);
				//update those counts.
				this.nuk[i][initialTopicLabel]++;
				this.sumuk[i]++;
				
				int timeID=eachPost.Atime;
				this.nkt[initialTopicLabel][timeID]++;
				this.sumkt[initialTopicLabel]++;
				
				this.nukt[i][initialTopicLabel][timeID]++;
				this.sumukt[i][initialTopicLabel]++;
				
				int expertiseLevel = eachPost.vote_level;
				this.nuke[i][initialTopicLabel][expertiseLevel]++;
				this.sumuke[i][initialTopicLabel]++;
				
				
				//for each tag
				for(int tagID:eachPost.Qtags){
					this.nkv[initialTopicLabel][tagID]++;
					this.sumkv[initialTopicLabel]++;
				}
				
			}
		}
		System.out.println("init model finishled");
		
		

		
	}
	
	public void trainModel(){
		for(int it=0;it<this.iterNum;it++){
			//for each iteration
			System.out.println(String.format("Round:%d", it));
			
			
			for(int i=0;i<this.U;i++ ){
				User u=this.trainU.users.get(i);
				ArrayList<AnswerPost> anses = u.answerPosts;
				for(int j=0;j<anses.size();j++){
					AnswerPost eachPost= anses.get(j);
					int timeID=eachPost.Atime;
					int [] tagIDs=eachPost.Qtags;
					int expLevel = eachPost.vote_level;
					int newTopicLabel = this.gibbsSample(i,j,tagIDs,timeID,expLevel);
					this.topicLabel[i][j]=newTopicLabel;
					
				}
			}
		}
	}
	
	public int gibbsSample(int uid,int pid,int [] tagIDs, int timeID,int expLevel){
		int oldTopicID=this.topicLabel[uid][pid];
		
		//remove current stuff.
		this.nuk[uid][oldTopicID]--;
		this.sumuk[uid]--;
		
		for(int eachTagID: tagIDs){
			this.nkv[oldTopicID][eachTagID]--;
			this.sumkv[oldTopicID]--;
		}
		
		this.nkt[oldTopicID][timeID]--;
		this.sumkt[oldTopicID]--;
		
		this.nukt[uid][oldTopicID][timeID]--;
		this.sumukt[uid][oldTopicID]--;
		
		this.nuke[uid][oldTopicID][expLevel]--;
		this.sumuke[uid][oldTopicID]--;
		
		//souihaite ca marche.
		double [] backupProb =  new double [this.K];
		int tagL=tagIDs.length;
		for(int k=0;k<this.K;k++){
			backupProb[k]  =  ( this.nuk[uid][k] + this.a )/(this.sumuk[uid] + this.K*this.a ) ;
			
			for(int eachTagID:tagIDs){  // if remove this, can not detect topic. tested!
				backupProb[k] *=  ( this.nkv[k][eachTagID] + tagL+ this.b )/(this.sumkv[k] + tagL+ this.V*this.b ) ;
			}
			//but if remove this , it still works.
			//if only keep this, good result.
			backupProb[k] *= ( this.nkt[k][timeID] + this.c )/(this.sumkt[k] + this.T*this.c ) ;
		
			//indeed, if add this, perplex will increase. fuck!
			//backupProb[k] *= ( this.nukt[uid][k][timeID] + this.d )/(this.sumukt[uid][k] + this.T*this.d ) ;
			//backupProb[k] *= ( this.nukt[uid][k][expLevel] + this.e )/(this.sumukt[uid][k] + this.E*this.e ) ;
			
			
		}
		
		//normalize backupProb
		for(int k=1;k<this.K;k++){
			backupProb[k]+=backupProb[k-1];
		}
		
		double newProb = Math.random()* backupProb[this.K-1];
		int newSampledTopic=0;
		while(newSampledTopic < this.K ){
			if(newProb< backupProb[newSampledTopic] ) break;
			newSampledTopic++;
		}
		
		/*System.out.print(oldTopicID);
		System.out.print("->");
		System.out.print(newSampledTopic);
		System.out.print("\n");*/
		
		//update count
		this.topicLabel[uid][pid]=newSampledTopic;
		
		//remove current stuff.
		this.nuk[uid][newSampledTopic]++;
		this.sumuk[uid]++;
		
		for(int eachTagID: tagIDs){
			this.nkv[newSampledTopic][eachTagID]++;
			this.sumkv[newSampledTopic]++;
		}
		
		this.nkt[newSampledTopic][timeID]++;
		this.sumkt[newSampledTopic]++;
		
		this.nukt[uid][newSampledTopic][timeID]++;
		this.sumukt[uid][newSampledTopic]++;
		
		this.nuke[uid][newSampledTopic][expLevel]++;
		this.sumuke[uid][newSampledTopic]++;
		
		return newSampledTopic;
	}
	
	public void estimateProb(){
		
		//thetaUK
		for(int uid = 0;uid<this.U;uid++){
			for(int kid =0 ;kid<this.K;kid++){
				this.thetaUK[uid][kid]=( this.nuk[uid][kid] + this.a )/(this.sumuk[uid] + this.K*this.a );
			}
		}
		
		//thetaKV
		for(int kid=0;kid<this.K;kid++){
			for(int vid=0;vid<this.V;vid++){
				this.thetaKV[kid][vid]=(this.nkv[kid][vid] +  this.b )/(this.sumkv[kid] + this.V*this.b);
			}
		}
		
		//thetaKT
		for(int kid=0;kid<this.K;kid++){
			for(int tid=0;tid<this.T;tid++){
				this.thetaKT[kid][tid]=(this.nkt[kid][tid] + this.c )/(this.sumkt[kid] + this.T*this.c);
			}
		}
		
		//thetaUKT
		for(int uid=0;uid<this.U;uid++){
			for(int kid=0;kid<this.K;kid++){
				for(int tid=0;tid<this.T;tid++){
					this.thetaUKT[uid][kid][tid]=( this.nukt[uid][kid][tid] + this.d )/(this.sumukt[uid][kid] + this.T*this.d ) ;
				}
			}
		}
		
		//thetaUKE
		for(int uid=0;uid<this.U;uid++){
			for(int kid=0;kid<this.K;kid++){
				for(int eid=0;eid<this.E;eid++){
					this.thetaUKE[uid][kid][eid]=( this.nuke[uid][kid][eid] + this.e )/(this.sumuke[uid][kid] + this.E*this.e ) ;
				}
			}
		}
		
		
		
	}
	
	public void computePer(){
		//p(tag) or p(word) = p(k|u)p(k|v)
		
		//test dataset =
		
		double  total_result=0.0;
		int post_number=0;
		int tag_number=0;
		double final_perplex=0.0;
		for (User u : this.testU.users){
			int uid=0;
			if ( !this.trainU.userToIndexMap.containsKey(u.userId) ){
				continue;
			}
			uid=this.trainU.userToIndexMap.get(u.userId);
			//System.out.println(u.userId);
			//System.out.println(u.answerPosts.size());
			
			for(AnswerPost eachPost: u.answerPosts){
				int [] tids = eachPost.Qtags;
				
				//compute for each post.
				double curPostW=0.0;
				
				ArrayList<String> tags = new ArrayList<String>();
				for( int tid : tids){
					String testOriTag = this.testU.indexToTagMap.get(tid);
					if(this.trainU.tagToIndexMap.containsKey(testOriTag)){
						tags.add( testOriTag);
					}
					
				}
				
				int tag_n= tags.size();
				if(tag_n==0){
					continue;
				}
				
				//double tempW=0.0;
				
				
				double forAllW=0.0;
				for(int topic_id=0;topic_id < this.K; topic_id++){
					double tempW=1.0;
					for(String tag: tags){
						//System.out.println(tag);
						int cur_tid=this.trainU.tagToIndexMap.get(tag);
						//p(topic|u) * p(tag|topic);
						
						tempW *= this.thetaKV[topic_id][cur_tid];
						assert (tempW!=0.0 );
					}
					assert(tempW!=1.0);
					tempW *= this.thetaUK[uid][topic_id] ;
					forAllW+=tempW;//accumulate for each topic.
				}
				
				post_number+=1;
				total_result += Math.log(forAllW);
				tag_number+=tag_n;
				
			}
			
			//break;

		}
		
		final_perplex =  Math.exp(-1.0  *  total_result  / tag_number);
		System.out.println(final_perplex);
		
	}
	
	public void outputResult(String outputPath) throws IOException{
		
	
		//thetaUK
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath+ "thetaUK.txt"));
		for(int uid = 0;uid<this.U;uid++){
			writer.write( this.trainU.users.get(uid).userId +",");
			for(int kid =0 ;kid<this.K;kid++){
				writer.write(this.thetaUK[uid][kid]+",");
			}
			writer.write("\n");
		}
		writer.close();
		
		//thetaKV
		writer = new BufferedWriter(new FileWriter(outputPath+ "thetaKV.txt"));
		for(int kid=0;kid<this.K;kid++){
			writer.write(String.format("Topic%d",kid));
			for(int vid=0;vid<this.V;vid++){
				String tag=this.trainU.indexToTagMap.get(vid);
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
				String tag=this.trainU.indexToTagMap.get(vid);
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
		
		
		//thetaKT
		writer = new BufferedWriter(new FileWriter(outputPath+ "thetaKT.txt"));
		for(int kid=0;kid<this.K;kid++){
			writer.write(String.format("Topic%d,",kid));
			for(int tid=7;tid<this.T;tid++){
				
				String timeLabel = this.trainU.indexToTimeMap.get(tid);
				System.out.println(timeLabel);
				//writer.write(timeLabel+":"+this.thetaKT[kid][tid]+"\t");
				writer.write(this.thetaKT[kid][tid]+",");
			}
			writer.write("\n");
		}
		writer.close();
		
		//thetaUKT
		writer = new BufferedWriter(new FileWriter(outputPath+ "UserthetaKT.txt"));
		for(int uid=0;uid<this.U;uid++){
			writer.write( this.trainU.users.get(uid).userId +",");
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
			writer.write( this.trainU.users.get(uid).userId +",");
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
