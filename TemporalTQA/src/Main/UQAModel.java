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
			ArrayList<AnswerPost> anses = u.answerPosts;
			this.wordTopicLabel[i]= new int[anses.size()	][];
			//this.categoryTopicLabel[i]=new int [anses.size()];
			for(int j=0;j<anses.size();j++){
				
				AnswerPost eachPost= anses.get(j);
				
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
				ArrayList<AnswerPost> anses = u.answerPosts;
				for(int j=0;j<anses.size();j++){
					AnswerPost eachPost= anses.get(j);
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
	
	public void computeCoherence(){
		ArrayList<ArrayList<String>> topicTopWords= this.getTopWords();
		double total_score=0.0;
		double item=0;
		for(int kid=0;kid<this.K;kid++){
			for(int i=0;i<10;i++){
				String w1=topicTopWords.get(kid).get(i);
				if(!this.testSet.termCountMap.containsKey(w1)){
					continue;
				}
				int wid1=this.testSet.termToIndexMap.get(w1);
				int occ1= this.testSet.singleOccDocument[wid1];//number of document has word1.
				//System.out.println(w1+";"+occ1);
				for(int j=i+1;j<10;j++){
					
					String w2=topicTopWords.get(kid).get(j);
					if(!this.testSet.termCountMap.containsKey(w1)){
						continue;
					}
	
					int wid2=this.testSet.termToIndexMap.get(w2);
					int cooc12=this.testSet.coOccDocument[wid1][wid2]+this.testSet.coOccDocument[wid2][wid1];
						
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
			
			for(AnswerPost eachPost: u.answerPosts){
				
				//ArrayList<Integer> faketags= eachPost.tags;
				ArrayList<Integer> fakewords=eachPost.words;
				
				//compute for each post.
				double curPostW=0.0;
				String postid=eachPost.aid;
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
						perword+=this.thetaUK[uid][topic_id]*this.thetaKW[topic_id][word]*this.thetaKC[topic_id][cid];
					}
					//System.out.println(perword);
					forAllW+= Math.log(perword);
				}

				total_result +=forAllW;
				word_number+=word_n;
				word_number+=word_n;//category for each word?
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
			
			for(AnswerPost eachPost: u.answerPosts){
				
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
