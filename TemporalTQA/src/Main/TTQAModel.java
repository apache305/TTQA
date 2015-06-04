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
	

}
