package Main;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

public class QuestionRouting {
	
	
	
	
	public static void testQR(LDABasedModel xx){
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
			xx.recommendUserForQuestion(q,numOfAnswer,precision,recall,msc);
			//break;
			//System.out.println(precision[0]+precision[1]+precision[2])	;
		}
		System.out.println("test question:"+qnum);
		
		System.out.println( "p@5:"+  precision[0]/(double)qnum );
		System.out.println( "p@10:"+  precision[1]/(double)qnum );
		System.out.println( "p@20:"+  precision[2]/(double)qnum );
		System.out.println( "p@30:"+  precision[3]/(double)qnum );
		System.out.println( "r@5:"+  recall[0]/(double)qnum );
		System.out.println( "r@10:"+  recall[1]/(double)qnum );
		System.out.println( "r@20:"+  recall[2]/(double)qnum );
		System.out.println( "r@30:"+  recall[3]/(double)qnum );
		System.out.println( "m@5:"+  (double)msc[0]/(double)qnum );
		System.out.println( "m@10:"+  (double)msc[1]/(double)qnum );
		System.out.println( "m@20:"+  (double)msc[2]/(double)qnum );
		System.out.println( "m@30:"+  (double)msc[3]/(double)qnum );
		

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
		DataWoker testset=new DataWoker();
		DataWoker testQA=new DataWoker(testQAsource);
		trainset.ProcessOriData();
		testQA.ProcessQuestions();
		trainset.printStat();
		testQA.printStat();
		
		

		
		//System.exit(1);
		System.out.println("this is question routing only for grostot runing");
		
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
		//runModel(tem,"out/TEM/",filter);
		double t2 = System.currentTimeMillis();
		System.out.println("time="+(t2-t1)  );
		//testQR(tem);
		
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
		//runModel(uqa,"out/outUQA/",filter);
		 t2 = System.currentTimeMillis();
			System.out.println("time="+(t2-t1)  );
		//testQR(uqa);
		
		resultPath="out/outLDA/";
		System.out.println("LDA Model");
		LDA lda= new LDA(trainset,testQA,iternum);
		lda.K=topNum;
		t1 = System.currentTimeMillis();
		//runModel(lda,"out/outLDA/",filter);
		 t2 = System.currentTimeMillis();
			System.out.println("time="+(t2-t1)  );
		//testQR(lda);
		
		resultPath= "out/outTTEQAA/";
		System.out.println("TTEQAA Model");
		TTEQAAModel tteqaa = new TTEQAAModel(trainset,testQA,iternum);
		tteqaa.K=topNum;
		t1 = System.currentTimeMillis();
		//runModel(tteqaa,"out/outTTEQAA/",filter);
		t2 = System.currentTimeMillis();
		System.out.println("time="+(t2-t1)  );
		//tteqaa.oneThingINeedToMakeSure();
		//testQR(tteqaa);
		
		resultPath= "out/outRandom/";
		System.out.println("random Model");
		RandomAlgo ram = new RandomAlgo(trainset,testQA,iternum);
		System.out.println("time="+(t2-t1)  );
		//testQR(ram);
		

		resultPath= "out/outGROST/";
		System.out.println("Grostt Model");
		GrosToT tot = new GrosToT(trainset,testQA,iternum);
		tot.K=topNum;
		t1 = System.currentTimeMillis();
		runModel(tot,"out/outGROST/",filter);
		t2 = System.currentTimeMillis();
		System.out.println("time="+(t2-t1)  );
		testQR(tot);
		
		//System.exit(1);
		
		
		
		
		
		
		
		
		

		
		
		
		
		
		
		
		
		
		

	}
}
