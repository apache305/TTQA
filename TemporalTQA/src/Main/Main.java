package Main;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Main {
	
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
		String trainFile = "data/ntrain.txt";
		String testFile="data/ntest.txt";
		String resultPath=null;
		//String curWorkingDir=System.getProperty("user.dir");
		//System.out.println(curWorkingDir+trainFile);
		Users users= new Users(trainFile);
		Users testUsers = new Users(testFile);
		//only for debug
		
		String trainsource="/Users/zmeng/GoogleDriver/2015/full_data/temp_dir/alla100.2.train.txt";
		String testsource ="/Users/zmeng/GoogleDriver/2015/full_data/temp_dir/alla100.2.test.txt";
		DataWoker trainset= new DataWoker(trainsource);
		DataWoker testset=new DataWoker(testsource);
		trainset.ProcessOriData();
		testset.ProcessOriData();
		
		trainset.computeCoOccur();
		testset.computeCoOccur();

		
		
		
		
		//System.out.println(users.tagCountMap.keySet().size());
		//System.out.println(users.tagCountMap.size());
		
		//i think a better way to do is a simple version, then enrich it.
		
		Set<String> filter=new HashSet<String>();
		
		int iternum=100;
		int topNum=100;
		resultPath= "out/outGROST/";
		System.out.println("Grostt Model");
		GrosToT tot = new GrosToT(trainset,testset,iternum);
		tot.K=topNum;
		runModel(tot,"out/outGROST/",filter);
		
		resultPath="out/outLDA/";
		System.out.println("LDA Model");
		LDA lda= new LDA(trainset,testset,iternum);
		lda.K=topNum;
		runModel(lda,"out/outLDA/",filter);
		
		
		resultPath= "out/outUQA/";
		System.out.println("UQA Model");
		UQAModel uqa = new UQAModel(trainset,testset,iternum);
		uqa.K=topNum;
		runModel(uqa,"out/outUQA/",filter);
		
		resultPath= "out/outTTEQAA/";
		System.out.println("TTEQAA Model");
		TTEQAAModel tteqaa = new TTEQAAModel(trainset,testset,iternum);
		tteqaa.K=topNum;
		runModel(tteqaa,"out/outTTEQAA/",filter);
		

		
		tot.computePer(filter);
		lda.computePer(filter);
		uqa.computePer(filter);
		tteqaa.computePer(filter);
		System.exit(1);
		
		
		
		
		
		
		
		

	}
}
