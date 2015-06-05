package Main;

public class TTQAModel {
	
	float a;
	float b;
	float c;
	float d;
	int U;//user number
	int K;//topic number
	int V;//tag number
	int T;//time number
	
	
	double [][] thetaU;// user - topic distribution  U*K
	double [][] thetaK;// topic - tag distribution  K*V
	double [][] thetaGT;//  topic - time distribution K*T 
	double [][][] thetaUT;// topic -time per user. distribution  U*K*T;
	
	public TTQAModel(){
		this.setDefaultParameteres();
		
	}
	
	public TTQAModel(int a,int b,int c,int d, int topicNum){
		this.a=a;
		this.b=b;
		this.c=c;
		this.d=d;
		this.K=topicNum;
	}
	
	public void setDefaultParameteres(){
		this.K=10;
		this.a=(float) 50.0/(float)this.K;
		this.b=0.01f;
		this.c=0.01f;
		this.d=0.01f;
	}
	
	
	
	
	

}
