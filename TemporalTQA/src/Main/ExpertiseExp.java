package Main;

import java.io.File;
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


	

public class ExpertiseExp {
	
	
	
	public static void testHitAndRank(LDABasedModel xx){
		
		int qnum=0;
		double [] totalNDCG= new double [3];
		totalNDCG[0]=0.0f;//ndcg@1
		totalNDCG[1]=0.0f;//ndcg@5
		totalNDCG[2]=0.0f;//ndcg
		//int unum=0;
		//5102015
		//System.out.println("test question size")
		for(Entry<String, QuestionPost> post:xx.testSet.quesitonMap.entrySet()){
			
			String qid=post.getKey();
			QuestionPost q= post.getValue();
			int numOfAnswer= q.answers.size();
			Set<String> ansUids = new HashSet<String>();
			ArrayList<Map.Entry<String,Integer>> realU= new ArrayList<Map.Entry<String,Integer>>();
			Map<String,Integer> realUVotes= new HashMap<String,Integer>();
			for(AnswerPost a: q.answers){
				if(xx.trainSet.useridToIndex.containsKey(a.user.userId) ){
					ansUids.add(a.user.userId);
					Map.Entry<String, Integer> pairs =new  AbstractMap.SimpleEntry<String , Integer> (a.user.userId,a.score);
					realU.add(pairs);
					realUVotes.put(a.user.userId, a.score);
				}
			}
			//System.out.println(ansUids.size());
			Collections.sort(realU, new Comparator<Map.Entry<String, Integer>>(){
				public int compare(Map.Entry<String, Integer> arg0,Map.Entry<String, Integer> arg1){
					return -1*arg0.getValue().compareTo(arg1.getValue());
				}
				
			});
			if(ansUids.size()>1){
			
				xx.HitAndRank(q, ansUids, realU, realUVotes,totalNDCG    );
				qnum+=1;
			}
			
			
			
		}
		System.out.println("test question:"+qnum);
		System.out.println("ndcg@1"+ totalNDCG[0]/(double)qnum   );
		System.out.println("ndcg@2"+ totalNDCG[1]/(double)qnum   );
		System.out.println("ndcg@3"+ totalNDCG[2]/(double)qnum   );
		
	}
	
	
	
	public static void testTOPVOTE(LDABasedModel xx){
		int [] msc = new int[4];//5 10 20 30
		msc[0]=0;
		msc[1]=0;
		msc[2]=0;
		msc[3]=0;
		double [] precision = new double [4];
		precision[0]=0.0f;
		precision[1]=0.0f;
		precision[2]=0.0f;
		precision[3]=0.0f;
		double[] recall = new double [4];
		recall[0]=0.0f;
		recall[1]=0.0f;
		recall[2]=0.0f;
		recall[3]=0.0f;
		
		
		
		int qnum=0;
		//int unum=0;
		//5102015
		//System.out.println("test question size")
		for(Entry<String, QuestionPost> post:xx.testSet.quesitonMap.entrySet()){
			
			String qid=post.getKey();
			QuestionPost q= post.getValue();
			int numOfAnswer= q.answers.size();
			/*if (numOfAnswer<=2){
				continue;
			}
			int flag=0;
			for(AnswerPost ans :q.answers){
				if (xx.trainSet.useridToIndex.containsKey(ans.user.userId)){
					flag=1;
					break;
				}
			}
			if (0==flag){
				//no train user answer this question.
				continue;
			}*/
			
			//this.trainSet.useridToIndex.get( p.user.userId); 
			/*if( !xx.trainSet.useridToIndex.containsKey(q.user.userId)   ){
				//System.out.println("??");
				continue;
			}*/

			
			qnum+=1;
			//num+=1;
			xx.topVoteHit(q,numOfAnswer,precision,recall,msc);
			//break;
			//System.out.println(precision[0]+precision[1]+precision[2])	;
		}
		System.out.println("test question:"+qnum);
		
		System.out.println( "p@10:"+  precision[0]/(double)qnum );
		System.out.println( "p@20:"+  precision[1]/(double)qnum );
		System.out.println( "p@30:"+  precision[2]/(double)qnum );
		System.out.println( "p@50:"+  precision[3]/(double)qnum );
		System.out.println( "r@10:"+  recall[0]/(double)qnum );
		System.out.println( "r@20:"+  recall[1]/(double)qnum );
		System.out.println( "r@30:"+  recall[2]/(double)qnum );
		System.out.println( "r@50:"+  recall[3]/(double)qnum );
		System.out.println( "m@10:"+  (double)msc[0]/(double)qnum );
		System.out.println( "m@20:"+  (double)msc[1]/(double)qnum );
		System.out.println( "m@30:"+  (double)msc[2]/(double)qnum );
		System.out.println( "m@50:"+  (double)msc[3]/(double)qnum );
		

	}
	
	public static void testNDCG(LDABasedModel xx){
			
		
		int qnum=0;
		double [] totalNDCG= new double [3];
		totalNDCG[0]=0.0f;//ndcg@1
		totalNDCG[1]=0.0f;//ndcg@5
		totalNDCG[2]=0.0f;//ndcg
		//int unum=0;
		//5102015
		//System.out.println("test question size")
		for(Entry<String, QuestionPost> post:xx.testSet.quesitonMap.entrySet()){
			
			String qid=post.getKey();
			QuestionPost q= post.getValue();
			int numOfAnswer= q.answers.size();
	
			xx.NDCG(q,totalNDCG);
			qnum+=1;
		}
		System.out.println("test question:"+qnum);
		System.out.println("ndcg@1"+ totalNDCG[0]/(double)qnum   );
		System.out.println("ndcg@2"+ totalNDCG[1]/(double)qnum   );
		System.out.println("ndcg@3"+ totalNDCG[2]/(double)qnum   );
			
	}

	public static void testMaxVoteHit(LDABasedModel xx){
		
		
		int qnum=0;
		int [] maxVoteHit= new int [12];

		for(Entry<String, QuestionPost> post:xx.testSet.quesitonMap.entrySet()){
			
			String qid=post.getKey();
			QuestionPost q= post.getValue();
			int numOfAnswer= q.answers.size();
			
			xx.maxVoteHit(q,maxVoteHit);
			qnum+=1;

		}
		System.out.println("test question:"+qnum);
		for(int i=0;i<12;i++){
			System.out.print( maxVoteHit[i]/(double)qnum +","   );
		}
		System.out.println("\n");
		
		
		
		
		
		
		

	}
	public static void saveModel(LDABasedModel xx,String outputPathDir){
		//xx.initModel();
		try {
			File f = new File(outputPathDir);
			if(!f.exists()){
				new File(outputPathDir).mkdirs();
			}{
				//FileUtils.cleanDirectory(outputPathDir);
			}
			xx.saveModel(outputPathDir);

			//xx.outputResult(outputPathDir );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void readModel(LDABasedModel xx,String outputPathDir){
		xx.initModel();
		try {
			File f = new File(outputPathDir);
			if(!f.exists()){
				new File(outputPathDir).mkdirs();
			}{
				//FileUtils.cleanDirectory(outputPathDir);
			}
			xx.readModel(outputPathDir);

			//xx.outputResult(outputPathDir );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void runModel(LDABasedModel xx, String outputPathDir,Set<String> filter){
		//this is new branch
		

		xx.initModel();
		xx.trainModel();
		xx.estimateProb();
		try {
			File f = new File(outputPathDir);
			if(!f.exists()){
				new File(outputPathDir).mkdirs();
			}{
				//FileUtils.cleanDirectory(outputPathDir);
			}

			xx.outputResult(outputPathDir );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//xx.computePer(filter);

		
		
	}
	public static void main(String [] args){
		//this is the entry
		//first try to animate LDA.
		/*if(args.length <2 ){
			System.out.println("not enough args");
			System.out.println("trainfile,outputpath");
			System.exit(-1);			
		}
		
		String trainFile = args[0];
		String resultPath= args[1];*/
		int  batchTopicNum =30;

		String resultPath=null;
		//String curWorkingDir=System.getProperty("user.dir");
		//System.out.println(curWorkingDir+trainFile);

		//only for debug

		//String trainsource="/Users/zmeng/GoogleDriver/2015/full_data/temp_dir/QRtrain2m1.train.txt";
		//String testQAsource="/Users/zmeng/GoogleDriver/2015/full_data/temp_dir/plex2m1.80.5.test.txt";
		String trainExpSource="/Users/zmeng/GoogleDriver/2015/full_data/temp_dir/qa3m3.train.50.txt";
		//String trainExpSource="/Users/zmeng/GoogleDriver/2015/full_data/temp_dir/EXPtrain3m3.train.txt";
		String testQAsource="/Users/zmeng/GoogleDriver/2015/full_data/temp_dir/qa3m3.qr.a2.txt";
		System.out.println("dataset:"+trainExpSource);
		System.out.println("dataset:"+testQAsource);
		DataWoker trainset= new DataWoker(trainExpSource);
		DataWoker testQA=new DataWoker(testQAsource);
		trainset.ProcessOriData();
		testQA.ProcessQuestions();
		trainset.printStat();
		testQA.printStat();
		
		

		
		//System.exit(1);
		System.out.println("this is expertise exp runing");
		
		//System.out.println(users.tagCountMap.keySet().size());
		//System.out.println(users.tagCountMap.size());
		
		//i think a better way to do is a simple version, then enrich it.
		
		
		Set<String> filter=new HashSet<String>();
		
		int iternum=50;
		int topNum=30;
		String savedir="save";//becareful, this sould be corresponding to the dataset.
		int read=0;
		
		System.out.println("current topic num:"+topNum);
		System.out.println("current it num:"+iternum);
		
		resultPath="out/TEM/";
		System.out.println("TEM model");
		TEMModel tem=new TEMModel(trainset,testQA,iternum);
		//testHitAndRank(tem);
		tem.K=topNum;
		double t1 = System.currentTimeMillis();
		if(read==0){
			 runModel(tem,"out/TEM/",filter);
			//saveModel(tem,savedir+"saveout/TEM/");
		}else{
			//readModel(tem,savedir+"saveout/TEM/");
		}
		double t2 = System.currentTimeMillis();
		System.out.println("time="+(t2-t1)  );
		testMaxVoteHit(tem);
		//testNDCG(tem);
		//testTOPVOTE(tem);
		//testHitAndRank(tem);
		resultPath="out/TTEMA/";
		System.out.println("TTEMA model");
		TTEMA ttema=new TTEMA(trainset,testQA,iternum);
		ttema.K=topNum;
		//runModel(ttema,"out/TTEMA/",filter);
		//testQR(ttema);
		
		
		resultPath= "out/outUQA/";
		System.out.println("UQA Model");
		UQAModel uqa = new UQAModel(trainset,testQA,iternum);
		uqa.K=topNum;
		t1 = System.currentTimeMillis();
		runModel(uqa,"out/outUQA/",filter);
		 t2 = System.currentTimeMillis();
			System.out.println("time="+(t2-t1)  );
			//testNDCG(uqa);
			//testHitAndRank(uqa);
			//testTOPVOTE(uqa);
			testMaxVoteHit(uqa);
		
		resultPath="out/outLDA/";
		System.out.println("LDA Model");
		LDA lda= new LDA(trainset,testQA,iternum);
		lda.K=topNum;
		t1 = System.currentTimeMillis();
		//runModel(lda,"out/outLDA/",filter);
		 t2 = System.currentTimeMillis();
			System.out.println("time="+(t2-t1)  );
			//testNDCG(lda);
		
		
		resultPath= "out/outTTEQAA/";
		System.out.println("TTEQAA Model");
		TTEQAAModel tteqaa = new TTEQAAModel(trainset,testQA,iternum);
		tteqaa.K=topNum;
		t1 = System.currentTimeMillis();
		if(read==0){
			runModel(tteqaa,"out/TTEA/",filter);
			//saveModel(tteqaa,savedir+"saveout/TTEA/");
		}else{
			readModel(tteqaa,savedir+"saveout/TTEA/");
		}
		t2 = System.currentTimeMillis();
		System.out.println("time="+(t2-t1)  );
		//tteqaa.oneThingINeedToMakeSure();
		//testTOPVOTE(tteqaa);
		//testNDCG(tteqaa);
		testMaxVoteHit(tteqaa);
		//testHitAndRank(tteqaa);
		
		resultPath= "out/outRandom/";
		System.out.println("random Model");
		RandomAlgo ram = new RandomAlgo(trainset,testQA,iternum);
		System.out.println("time="+(t2-t1)  );
		//testTOPVOTE(ram);
		//testNDCG(ram);
		testMaxVoteHit(ram);
		

		resultPath= "out/outGROST/";
		System.out.println("Grostt Model");
		GrosToT tot = new GrosToT(trainset,testQA,iternum);
		tot.K=topNum;
		t1 = System.currentTimeMillis();
		runModel(tot,"out/outGROST/",filter);
		t2 = System.currentTimeMillis();
		System.out.println("time="+(t2-t1)  );
		//testNDCG(tot);
		//testTOPVOTE(tot);
		testMaxVoteHit(tot);
		//System.exit(1);
		

	}
}
