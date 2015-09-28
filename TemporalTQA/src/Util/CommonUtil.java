package Util;

import java.util.ArrayList;
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

}
