package Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CommonUtil {
	
	public static double jensenShannonDivergence(double[] p1, double[] p2) {
	      assert(p1.length == p2.length);
	      double[] average = new double[p1.length];
	      for (int i = 0; i < p1.length; ++i) {
	        average[i] += (p1[i] + p2[i])/2;
	      }
	      return (klDivergence(p1, average) + klDivergence(p2, average))/2;
	    }

	    
	   public static final double log2 = Math.log(2);
	    /**
	     * Returns the KL divergence, K(p1 || p2).
	     *
	     * The log is w.r.t. base 2. <p>
	     *
	     * *Note*: If any value in <tt>p2</tt> is <tt>0.0</tt> then the KL-divergence
	     * is <tt>infinite</tt>. Limin changes it to zero instead of infinite. 
	     * 
	     */
	    public static double klDivergence(double[] p1, double[] p2) {


	      double klDiv = 0.0;

	      for (int i = 0; i < p1.length; ++i) {
	        if (p1[i] == 0) { continue; }
	        if (p2[i] == 0.0) { continue; } // Limin

	      klDiv += p1[i] * Math.log( p1[i] / p2[i] );
	      }

	      return klDiv / log2; // moved this division out of the loop -DM
	    }
	    
	    //public static 
	    public static int computePrecision(ArrayList<String> topuser, Set<String > real, int topk){
	    		int res=0;
	    		for(int i=0;i<topk;i++){
	    			if(real.contains(topuser.get(i))  ){
	    				
	    				res++;
	    			}
	    			
	    		}
	    		return res;
	    		
	    }

	    public static int computeMSC(ArrayList<String> topuser, Set<String > real, int topk){
	    		int res=0;
	    		for(int i=0;i<topk;i++){
	    			if(real.contains(topuser.get(i))  ){
	    				
	    				res++;
	    				return 1;
	    			}
	    			
	    		}
	    		return 0;
	    		//return res;
	    		
	    }
	    
	    public static double computeNDCG(ArrayList<String> recU, ArrayList<String> realU, int topk){
	    	//score for each position is from 10 to 1.
	    		//assign score
	    	if (recU.size()==0){
	    		return 0.0f;
	    	}
	    	Map<String,Integer> ideaScore= new HashMap<String,Integer>();
	    	int score=10;
	    	for(int i=0;i<realU.size();i++){
	    		ideaScore.put(realU.get(i), score);
	    		if(score>2){
	    			score-=2;
	    		}
	    		System.out.println("score"+score);
	    		
	    	}
	    	
	    	double dcg=0.0f;
	    
	    	
	    	
	    	for(int i=0;i<topk;i++){
	    		if(i>=recU.size()){
	    			break;
	    		}
	    		int curScore=ideaScore.get(recU.get(i));
	    		if(i==0){
	    			dcg+= curScore;
	    		}else{
	    			dcg+= (double)curScore / (Math.log(i+1) / Math.log(2)); 
	    			//System.out.println((Math.log(i) / Math.log(2)));
	    		}
	    		//System.out.println(dcg);
	    		
	    	}
	    	
	    	class myComp implements Comparator<String>{
	    		private Map<String,Integer> ids;
	    		public myComp(Map<String,Integer> ids){
	    			this.ids=ids;
	    		}
				@Override
				public int compare(String o1, String o2) {
					// TODO Auto-generated method stub
	    			return -1* this.ids.get(o1).compareTo(this.ids.get(o2));
				}
	    		
	    	}
	    	
	    	//RE ORDER REC U BY SCORE;
	    //	System.out.println("testsort");
	    //	for(String u:recU){
	    		//System.out.println(u);
	    //	}
	    	Collections.sort(recU, new myComp(ideaScore));
	    //	System.out.println("testsort");
	    //	for(String u:recU){
	    		//System.out.println(u);
	    //	}
	    	//compute idcg
	    	double idcg=0.0f;
	    	for(int i=0;i<topk;i++){
	    		if(i>=recU.size()){
	    			break;
	    		}
	    		int curScore=ideaScore.get(recU.get(i));
	    		if(i==0){
	    			idcg+= curScore;
	    		}else{
	    			
	    			idcg+= (double)curScore / (Math.log(i+1) / Math.log(2)); 
	    			//System.out.println((Math.log(i) / Math.log(2)));
	    		}
	    		
	    	}
	    	//System.out.println(idcg);
	    	double ndcg=dcg/idcg;
	    //	System.out.println(ndcg);
	    return ndcg;
	    	
	    	
	    	
	    	
	    		//return 0.0f;
	    }
	    public static void main(String args[]){
	    		ArrayList<String> recU= new ArrayList<String>();
	    		ArrayList<String> idealU= new ArrayList<String>();
	    		recU.add("U10");
	    		recU.add("U7");
	    		recU.add("U5");
	    		recU.add("U1");
	    		idealU.add("U1");
	    		idealU.add("U5");
	    		idealU.add("U7");
	    		idealU.add("U9");
	    		idealU.add("U10");
	    		
	    		
	    		System.out.println(computeNDCG(recU,idealU,100000000));
	    }



}
