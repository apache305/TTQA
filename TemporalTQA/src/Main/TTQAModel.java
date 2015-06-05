package Main;

import java.util.ArrayList;

public class TTQAModel {
	
	float a;
	float b;
	float c;
	float d;
	int U;//user number
	int K;//topic number
	int V;//tag number
	int T;//time number
	
	
	int iterNum;//number of iterations.
	
	double [][] thetaU;// user - topic distribution  U*K
	int [][] nuk;// number of user i in topic j. U*K
	int [] sumuk;//sum for each user. U

	
	
	double [][] thetaK;// topic - tag distribution  K*V
	int [][] nkt;//number of topic k in tag j; K*V
	int [] sumkt;//sum for each topic. K
	
	
	double [][] thetaGT;//  topic - time distribution K*T 
	int [][] ngt;//number of topic k in time j; K*T
	int [] sumgt;//sum for each topic. K
	
	
	double [][][] thetaUT;// topic -time per user. distribution  U*K*T;
	int [][][] nut;//number of user i 's topic j in time k; U*K*T
	int [][] sumut;//sum of user i's each topic. U*K;
	
	
	//topic label for each user's each post
	//ArrayList<ArrayList<Integer>> topicLabel; 
	int [][] topicLabel;
	
	
	
	public TTQAModel(){
		this.setDefaultParameteres();
		
	}
	
	public TTQAModel(int a,int b,int c,int d, int topicNum,int iterNum){
		this.a=a;
		this.b=b;
		this.c=c;
		this.d=d;
		this.K=topicNum;
		this.iterNum=iterNum;
	}
	
	public void setDefaultParameteres(){
		this.K=10;
		this.a=(float) 50.0/(float)this.K;
		this.b=0.01f;
		this.c=0.01f;
		this.d=0.01f;
		this.iterNum=100;
	}
	
	public void initModel(Users users){
		//init those probabilities;
		this.U= users.users.size();//number of user.
		this.T= users.timeCountMap.size();//number of time label
		this.V= users.tagCountMap.size();//number of tag
		this.thetaU = new double [this.U][this.K];
		this.nuk= new int[this.U][this.K];
		this.sumuk= new int[this.U];
		
		this.thetaK= new double[this.K][this.V];
		this.nkt=new int [this.K][this.V];
		this.sumkt= new int[this.K];
		
		this.thetaGT=new double[this.K][this.T];
		this.ngt= new int[this.K][this.T];
		this.sumgt=new int[this.K];
		
		this.thetaUT=new double[this.U][this.K][this.T];
		this.nut= new int[this.U][this.K][this.T];
		this.sumut=new int[this.U][this.K];
		

		
		this.topicLabel = new int [this.U][];
		for(int i=0;i<this.U;i++ ){
			//users.
			
			
		}
		
		
		
		
		// init latent variable z for each post. why?
		for(int u=0;u<this.U;u++){

			
			
			
			
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
	}
	
	
	
	
	

}
