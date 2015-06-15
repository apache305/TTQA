package Others;

//import Main.Users;
import Main.*;


//paper on AAAI 2014.
public class GrosToT {
	float a;//alpha
	float b;//beta
	float s;//sigema
	float l;//lamida
	
	
	int U;//user number
	int G;//group number
	int K;//topic number
	int T;//time label?
	int V;//this tag.
	
	
	double [][] thetaGK;//group over topic distribution
	int [][] ngk;
	int []  sumngk;
	
	double [][] thetaUG;//user over group distribution
	int [][] nug;
	int [] sumnug;
	
	double [][] thetaKV;//topic over words distribution
	int [][] nkv;
	int [] sumnkv;
	
	double [][][] thetaGKT;//given group, topic over time distribution.
	int [][][] ngkt;
	int [][] sumngkt;
	
	
	
	
	int topicLabel [][];//user i, j post's topic label
	int groupLabel []; //user i's group label.
	
	
	public GrosToT(){
		this.K=50;// topic number
		this.G=50;//group number
		this.a=50/this.K;
		this.s=50/this.G;
		this.b=0.01f;
		this.l=0.01f;
	}
	
	
	public void initModel(Users users){
		this.U = users.users.size();
		this.T= users.timeCountMap.size();//number of time label
		this.V= users.tagCountMap.size();//number of tag
		
		this.thetaGK = new double[this.G][this.K];
		this.ngk=new int[this.G][this.K];
		this.sumngk=new int[this.G];
		
		this.thetaUG = new double [this.U][this.G];
		this.nug = new int [this.U][this.G];
		this.sumnug= new int[this.U];
		
		this.thetaKV = new double [this.K][this.V];
		this.nkv = new int[this.K][this.V];
		this.sumnkv= new int[this.K];
		
		this.thetaGKT = new double [this.G][this.K][this.T];
		this.ngkt = new int [this.G][this.K][this.T];
		this.sumngkt = new int[this.G][this.K];
		
		
	
	}
	
	
	
	
	
	
	

}
