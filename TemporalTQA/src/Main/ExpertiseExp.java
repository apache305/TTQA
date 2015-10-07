package Main;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;


	

public class ExpertiseExp {

	public static void testNDCG(LDABasedModel xx){
		
		
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
			
			
			//break;
			//System.out.println(precision[0]+precision[1]+precision[2])	;
		}
		System.out.println("test question:"+qnum);
		
		
		

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
		String trainsource="/Users/zmeng/GoogleDriver/2015/full_data/temp_dir/qa3m3.train.50.txt";
		String testQAsource="/Users/zmeng/GoogleDriver/2015/full_data/temp_dir/qa3m3.qr.a2.txt";
		System.out.println("dataset:"+trainsource);
		System.out.println("dataset:"+testQAsource);
		DataWoker trainset= new DataWoker(trainsource);
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
		
		int iternum=100;
		int topNum=30;
		
		System.out.println("current topic num:"+topNum);
		
		resultPath="out/TEM/";
		System.out.println("TEM model");
		TEMModel tem=new TEMModel(trainset,testQA,iternum);
		tem.K=topNum;
		double t1 = System.currentTimeMillis();
		runModel(tem,"out/TEM/",filter);
		double t2 = System.currentTimeMillis();
		System.out.println("time="+(t2-t1)  );
		testNDCG(tem);
		
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
			testNDCG(uqa);
		
		resultPath="out/outLDA/";
		System.out.println("LDA Model");
		LDA lda= new LDA(trainset,testQA,iternum);
		lda.K=topNum;
		t1 = System.currentTimeMillis();
		runModel(lda,"out/outLDA/",filter);
		 t2 = System.currentTimeMillis();
			System.out.println("time="+(t2-t1)  );
			testNDCG(lda);
		
		resultPath= "out/outTTEQAA/";
		System.out.println("TTEQAA Model");
		TTEQAAModel tteqaa = new TTEQAAModel(trainset,testQA,iternum);
		tteqaa.K=topNum;
		t1 = System.currentTimeMillis();
		runModel(tteqaa,"out/outTTEQAA/",filter);
		t2 = System.currentTimeMillis();
		System.out.println("time="+(t2-t1)  );
		//tteqaa.oneThingINeedToMakeSure();
		testNDCG(tteqaa);
		
		resultPath= "out/outRandom/";
		System.out.println("random Model");
		RandomAlgo ram = new RandomAlgo(trainset,testQA,iternum);
		System.out.println("time="+(t2-t1)  );
		testNDCG(ram);
		

		resultPath= "out/outGROST/";
		System.out.println("Grostt Model");
		GrosToT tot = new GrosToT(trainset,testQA,iternum);
		tot.K=topNum;
		t1 = System.currentTimeMillis();
		runModel(tot,"out/outGROST/",filter);
		t2 = System.currentTimeMillis();
		System.out.println("time="+(t2-t1)  );
		testNDCG(tot);
		
		//System.exit(1);
		

	}
}
