package Others;


//paper on AAAI 2014.
public class GrosToT {
	float a;//alpha
	float b;//beta
	float s;//sigema
	float r;//lamida
	
	
	int U;//user number
	int G;//group number
	int K;//topic number
	int T;//time label?
	
	
	double [][] thetaGK;//group over topic distribution
	double [][] thetaUG;//user over group distribution
	double [][] thetaKV;//topic over work distribution
	double [][][] thetaGKT;//given group, topic over time distribution.
	
	
	
	
	int topicLabel [][];//user i, j post's topic label
	int groupLabel []; //user i's group label.
	

}
