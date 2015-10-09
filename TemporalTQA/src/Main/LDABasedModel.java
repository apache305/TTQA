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
	
	public void recommendUserForQuestion(QuestionPost q,int numOfAnswer, double[] precision, double[] recall, int [] msc){
		System.out.println("un implement method");
	}

	public void computeTimeLikelihood() {
		// TODO Auto-generated method stub
		
	}
	public void NDCG(QuestionPost q, double [] totalNDCG){
		System.out.println("un implement method");
		
	}

	public void maxVoteHit(QuestionPost q, int[] maxVoteHit) {
		// TODO Auto-generated method stub
		System.out.println("un implement method");
		
	}
	
}
