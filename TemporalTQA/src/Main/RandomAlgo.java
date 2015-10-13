package Main;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import Main.TTEQAAModel.UserSimiAct;
import Util.CommonUtil;

public class RandomAlgo extends LDABasedModel {


	
	DataWoker trainSet=null;
	DataWoker testSet=null;
	int iterNum;
	
	
	public RandomAlgo(DataWoker trainUsers, DataWoker testUsers,int iternum){
		super.trainSet=trainUsers;
		super.testSet=testUsers;

		this.trainSet=trainUsers;
		this.testSet=testUsers;
		this.iterNum=iternum;
		
	}
	
public void recommendUserForQuestion(QuestionPost q,int numOfAnswer, double[] precision, double[] recall, int [] msc){
		
		
		Set<String> randomUids= this.trainSet.useridToIndex.keySet();
		ArrayList<String> randomList=new ArrayList<String>();
		for(String ruid : randomUids){
			randomList.add(ruid);
		}
		
		
		//check p@5 p@10 p@15 //
		Set<String> ansUids = new HashSet<String>();
		for(AnswerPost a: q.answers){
			ansUids.add(a.user.userId);
		}
		ArrayList<String> randomRec=new ArrayList<String>();
		
		Collections.shuffle(randomList);
		for(int i=0;i<50;i++){
			randomRec.add(randomList.get(i));
		}
		
		
		msc[0]+=CommonUtil.computeMSC(randomRec, ansUids, 5);
		msc[1]+=CommonUtil.computeMSC(randomRec, ansUids, 10);
		msc[2]+=CommonUtil.computeMSC(randomRec, ansUids, 20);
		msc[3]+=CommonUtil.computeMSC(randomRec, ansUids, 30);
		
		precision[0] +=  ( (double) CommonUtil.computePrecision(randomRec, ansUids, 5) / 5.0f   );
		precision[1] +=  ( (double) CommonUtil.computePrecision(randomRec, ansUids, 10) / 10.0f   );
		precision[2] +=  ( (double) CommonUtil.computePrecision(randomRec, ansUids, 20) / 20.0f   );
		precision[3] +=  ( (double) CommonUtil.computePrecision(randomRec, ansUids, 30) / 30.0f   );
		recall[0] +=  ( (double) CommonUtil.computePrecision(randomRec, ansUids, 5) / (double)numOfAnswer   );
		recall[1] +=  ( (double) CommonUtil.computePrecision(randomRec, ansUids, 10) /(double)numOfAnswer   );
		recall[2] +=  ( (double) CommonUtil.computePrecision(randomRec, ansUids, 20) / (double)numOfAnswer   );
		recall[3] +=  ( (double) CommonUtil.computePrecision(randomRec, ansUids, 30) / (double)numOfAnswer   );
		
	}

public void maxVoteHit(QuestionPost q, int [] mvh){
	
	

		
	
		
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
		Set<String> randomUids= this.trainSet.useridToIndex.keySet();
		ArrayList<String> randomList=new ArrayList<String>();
		for(String ruid : randomUids){
			randomList.add(ruid);
		}

		for(int i=0;i<100;i++){
			String recUid= randomList.get(i);
			if(maxuids.contains(recUid)){
				mvh[i/10]+=1;
				return ;
			}
		}
		//mvh[11]+=1;//miss
		return ;
		


		
	}





public void topVoteHit(QuestionPost q,int numOfAnswer, double[] precision, double[] recall, int [] msc){
	
	Set<String> randomUids= this.trainSet.useridToIndex.keySet();
	ArrayList<String> randomList=new ArrayList<String>();
	for(String ruid : randomUids){
		randomList.add(ruid);
	}
	ArrayList<String> topUsers=new ArrayList<String>();
	
	Collections.shuffle(randomList);
	for(int i=0;i<randomList.size();i++){
		topUsers.add(randomList.get(i));
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
	
	
	
	
}
