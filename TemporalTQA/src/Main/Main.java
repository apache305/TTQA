package Main;

import java.io.IOException;

public class Main {
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
		String resultPath= "out/";
		//String curWorkingDir=System.getProperty("user.dir");
		//System.out.println(curWorkingDir+trainFile);
		Users users= new Users(trainFile);
		Users testUsers = new Users(testFile);
		//only for debug
		//System.out.println(users.tagCountMap.keySet().size());
		//System.out.println(users.tagCountMap.size());
		
		//i think a better way to do is a simple version, then enrich it.
		
		TTEQAModel tt= new TTEQAModel();
		tt.initModel(users);
		tt.trainModel(users);
		tt.estimateProb();
		try {
			tt.outputResult(resultPath, users);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tt.computePer(testUsers);
		
		
		

	}
}
