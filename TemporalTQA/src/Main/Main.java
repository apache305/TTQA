package Main;

import java.io.File;
import java.io.IOException;

public class Main {
	
	public static void runModel(LDABasedModel xx, String outputPathDir){
		

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
		xx.computePer();
		
		
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

		
		
		
		
		//System.out.println(users.tagCountMap.keySet().size());
		//System.out.println(users.tagCountMap.size());
		
		//i think a better way to do is a simple version, then enrich it.
		
		resultPath= "out/outGROST/";
		System.out.println("Grostt Model");
		//GrosToT tot = new GrosToT(users,testUsers);
		//runModel(tot,"out/outGROST/");
		
		resultPath= "out/outTTEQAA/";
		System.out.println("TTEQAA Model");
		TTEQAAModel tteqaa = new TTEQAAModel(trainset,testset);
		runModel(tteqaa,"out/outTTEQAA/");
		//System.exit(1);
		resultPath= "out/outTTEQA/";
		//TTEQAModel tteqa = new TTEQAModel(users,testUsers);
		//runModel(tteqa,"out/outTTEQA/");
		
		
		
		
		
		
		

	}
}
