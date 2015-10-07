package Main;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;


	

public class TimeLikelihood {
	
	
	public static void testTIME(LDABasedModel xx){
		
		
		
		
		
		
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
		String trainsource="/Users/zmeng/GoogleDriver/2015/full_data/temp_dir/plex2m.train.80.txt";
		String testsource="/Users/zmeng/GoogleDriver/2015/full_data/temp_dir/plex2m.test.80.txt";
		System.out.println("dataset:"+trainsource);
		System.out.println("dataset:"+testsource);
		DataWoker trainset= new DataWoker(trainsource);
		DataWoker testset=new DataWoker(testsource);
		
		
		trainset.ProcessOriData();
		testset.ProcessOriData();

		trainset.printStat();
		testset.printStat();

		
		

		
		//System.exit(1);
		System.out.println("this is time likelihood");
		
		//System.out.println(users.tagCountMap.keySet().size());
		//System.out.println(users.tagCountMap.size());
		
		//i think a better way to do is a simple version, then enrich it.
		
		
		Set<String> filter=new HashSet<String>();
		
		int iternum=100;
		int topNum=30;
		
		System.out.println("current topic num:"+topNum);
		


		double t1, t2;
		resultPath= "out/outTTEQAA/";
		System.out.println("TTEQAA Model");
		TTEQAAModel tteqaa = new TTEQAAModel(trainset,testset,iternum);
		tteqaa.K=topNum;
		t1 = System.currentTimeMillis();
		runModel(tteqaa,"out/outTTEQAA/",filter);
		t2 = System.currentTimeMillis();
		System.out.println("time="+(t2-t1)  );
		//tteqaa.oneThingINeedToMakeSure();
		testTIME(tteqaa);
		


		resultPath= "out/outGROST/";
		System.out.println("Grostt Model");
		GrosToT tot = new GrosToT(trainset,testset,iternum);
		tot.K=topNum;
		t1 = System.currentTimeMillis();
		runModel(tot,"out/outGROST/",filter);
		t2 = System.currentTimeMillis();
		System.out.println("time="+(t2-t1)  );
		testTIME(tot);
		
		//System.exit(1);
		

	}
}
