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
	
	
	
	
}
