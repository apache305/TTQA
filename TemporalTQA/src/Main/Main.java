package Main;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Main {
	

	
	public static void testQR(LDABasedModel xx, DataWoker testQAset){
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
		//5102015
		
		for(Entry<String, QuestionPost> post:testQAset.quesitonMap.entrySet()){
			String qid=post.getKey();
			QuestionPost q= post.getValue();
			//number of answer.
			int numOfAnswer= q.answers.size();
			qnum+=1;
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
		
		
		xx.computePer(filter);
		xx.computeCoherence(xx.trainSet);
		xx.computeCoherence(xx.testSet);
		
		
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

		String resultPath=null;
		//String curWorkingDir=System.getProperty("user.dir");
		//System.out.println(curWorkingDir+trainFile);

		//only for debug
		
		//String trainsource="/Users/zmeng/GoogleDriver/2015/full_data/temp_dir/alla100.2.train.txt";
		//String testsource ="/Users/zmeng/GoogleDriver/2015/full_data/temp_dir/alla100.2.test.txt";
		String trainsource="/Users/zmeng/GoogleDriver/2015/full_data/temp_dir/aq300.train.80.txt";
		String testsource ="/Users/zmeng/GoogleDriver/2015/full_data/temp_dir/aq300.test.80.txt";
		String testQAsource="/Users/zmeng/GoogleDriver/2015/full_data/temp_dir/qa300.80.5.test.txt";
		DataWoker trainset= new DataWoker(trainsource);
		DataWoker testset=new DataWoker(testsource);
		DataWoker testQA=new DataWoker(testQAsource);
		trainset.ProcessOriData();
		testset.ProcessOriData();
		testQA.ProcessQuestions();
		trainset.printStat();
		testset.printStat();
		testQA.printStat();
		
		//System.exit(1);
		
		trainset.computeCoOccur();
		testset.computeCoOccur();
		
		/*for(int i=0;i<trainset.useridToIndex.size();i++){
			System.out.println( trainset.users.get(i).allPosts.size() )	;
		}
		System.exit(1);*/

		
		
		
		
		//System.out.println(users.tagCountMap.keySet().size());
		//System.out.println(users.tagCountMap.size());
		
		//i think a better way to do is a simple version, then enrich it.
		
		Set<String> filter=new HashSet<String>();
		
		int iternum=10;
		int topNum=30;
		
		resultPath="out/TEM/";
		System.out.println("TEM model");
		TEMModel tem=new TEMModel(trainset,testset,iternum);
		tem.K=topNum;
		//runModel(tem,"out/TEM/",filter);
		//testQR(tem);
		resultPath="out/TTEMA/";
		System.out.println("TTEMA model");
		TTEMA ttema=new TTEMA(trainset,testset,iternum);
		ttema.K=topNum;
		//runModel(ttema,"out/TTEMA/",filter);
		//testQR(ttema);
		
		
		resultPath= "out/outUQA/";
		System.out.println("UQA Model");
		UQAModel uqa = new UQAModel(trainset,testset,iternum);
		uqa.K=topNum;
		//runModel(uqa,"out/outUQA/",filter);
		//testQR(uqa);
		
		resultPath="out/outLDA/";
		System.out.println("LDA Model");
		LDA lda= new LDA(trainset,testset,iternum);
		lda.K=topNum;
		//runModel(lda,"out/outLDA/",filter);
		//testQR(lda);
		
		resultPath= "out/outTTEQAA/";
		System.out.println("TTEQAA Model");
		TTEQAAModel tteqaa = new TTEQAAModel(trainset,testset,iternum);
		tteqaa.K=topNum;
		runModel(tteqaa,"out/outTTEQAA/",filter);
		//tteqaa.oneThingINeedToMakeSure();
		testQR(tteqaa, testQA);
		

		resultPath= "out/outGROST/";
		System.out.println("Grostt Model");
		GrosToT tot = new GrosToT(trainset,testset,iternum);
		tot.K=topNum;
		//runModel(tot,"out/outGROST/",filter);
		//testQR(tot);
		
		System.exit(1);
		
		
		
		
		
		
		
		
		

		
		tot.computePer(filter);
		lda.computePer(filter);
		uqa.computePer(filter);
		tteqaa.computePer(filter);
		System.exit(1);
		
		
		
		
		
		
		
		

	}
}
