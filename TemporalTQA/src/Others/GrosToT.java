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
	
	
	double [][] thetaGK;//group over topic distribution
	double [][] thetaUG;//user over group distribution
	double [][] thetaKV;//topic over words distribution
	double [][][] thetaGKT;//given group, topic over time distribution.
	
	
	
	
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
		
		
	
	}
	
	
	
	
	
	
	

}
