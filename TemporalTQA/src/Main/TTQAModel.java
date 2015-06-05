package Main;

import java.util.ArrayList;
import java.util.Random;

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
	
	double [][] thetaUK;// user - topic distribution  U*K
	int [][] nuk;// number of user i in topic j. U*K
	int [] sumuk;//sum for each user. U

	
	
	double [][] thetaKV;// topic - tag distribution  K*V
	int [][] nkv;//number of topic k in tag j; K*V
	int [] sumkv;//sum for each topic. K
	
	
	double [][] thetaKT;//  topic - time distribution K*T 
	int [][] nkt;//number of topic k in time j; K*T
	int [] sumkt;//sum for each topic. K
	
	
	double [][][] thetaUKT;// topic -time per user. distribution  U*K*T;
	int [][][] nukt;//number of user i 's topic j in time k; U*K*T
	int [][] sumukt;//sum of user i's each topic. U*K;
	
	
	//each users' each posts' topic label.
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
		this.thetaUK = new double [this.U][this.K];
		this.nuk= new int[this.U][this.K];
		this.sumuk= new int[this.U];
		
		this.thetaKV= new double[this.K][this.V];
		this.nkv=new int [this.K][this.V];
		this.sumkv= new int[this.K];
		
		this.thetaKT=new double[this.K][this.T];
		this.nkt= new int[this.K][this.T];
		this.sumkt=new int[this.K];
		
		this.thetaUKT=new double[this.U][this.K][this.T];
		this.nukt= new int[this.U][this.K][this.T];
		this.sumukt=new int[this.U][this.K];
		

		
		this.topicLabel = new int [this.U][];
		Random r = new Random();
		for(int i=0;i<this.U;i++ ){
			User u=users.users.get(i);
			ArrayList<AnswerPost> anses = u.answerPosts;
			this.topicLabel[i]= new int[anses.size()	];
			for(int j=0;j<anses.size();j++){
				int initialTopicLabel = r.nextInt(this.K);//0 to K-1
				this.topicLabel[i][j]=initialTopicLabel;
				
				AnswerPost eachPost= anses.get(j);
				//update those counts.
				this.nuk[i][initialTopicLabel]++;
				this.sumuk[i]++;
				
				int timePos=eachPost.Atime;
				this.nkt[initialTopicLabel][timePos]++;
				this.sumkt[initialTopicLabel]++;
				
				this.nukt[i][initialTopicLabel][timePos]++;
				this.sumukt[i][initialTopicLabel]++;
				
				//for each tag
				for(int t:eachPost.Qtags){
					this.nkv[initialTopicLabel][t]++;
					this.sumkv[initialTopicLabel]++;
				}
				
			}
		}
		
		
		
		
		// init latent variable z for each post. why?
		for(int u=0;u<this.U;u++){

			
			
			
			
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
	}
	
	
	
	
	

}
