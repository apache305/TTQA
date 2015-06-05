package Main;

public class Main {
	public static void main(String [] args){
		//this is the entry
		//first try to animate LDA.
		if(args.length <2 ){
			System.out.println("not enough args");
			System.out.println("trainfile,outputpath");
			System.exit(-1);			
		}
		
		String trainFile = args[0];
		String resultPath= args[1];
		//String curWorkingDir=System.getProperty("user.dir");
		//System.out.println(curWorkingDir+trainFile);
		Users users= new Users(trainFile);
		//only for debug
		//System.out.println(users.tagCountMap.keySet().size());
		//System.out.println(users.tagCountMap.size());
		
		//i think a better way to do is a simple version, then enrich it.
		
		TTQAModel tt= new TTQAModel();
		tt.initModel(users);
		tt.trainModel(users);
		tt.estimateProb();
		
		
		

	}
}
