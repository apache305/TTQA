package Main;

import java.io.IOException;
import java.util.Set;

public  class LDABasedModel {
	public DataWoker trainSet;
	public DataWoker testSet;
	
	public LDABasedModel(){
		
	}
	
	public LDABasedModel(DataWoker Train,DataWoker Test,int iternum){
		
	}
	public void initModel(){
		//throw new Exception("un implement error");
		
	}
	public void trainModel(){
		
	}
	public void estimateProb(){
		
	}
	public void outputResult(String outputPath) throws IOException{
		
	}
	public void computePer(Set<String> filterPostId){
		
	}
	public void computeCoherence(DataWoker dataset){

	}
	
	public void recommendUserForQuestion(QuestionPost q,int [] precision){
		System.out.println("un implement method");
	}
	
}
