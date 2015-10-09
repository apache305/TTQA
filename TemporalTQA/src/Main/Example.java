package Main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Example {
	public Map<String,Integer>  mingzi ;
	
	public void  com(  ){
		//
		  mingzi = new HashMap<String,Integer>();
		 mingzi.put("first",10);
		 mingzi.put("second",2);
			ArrayList<String> mylist= new ArrayList<String>();
			mylist.add("second");
			mylist.add("first");
			Collections.sort(mylist, new Comparator<String>(){
				public int compare(String o1, String o2	){
					return mingzi.get(o1).compareTo(mingzi.get(o2));
				}
			});
		
	}
	
	
	
	public static void main(String [] args){
		
		Example a= new Example();
		a.com();
		
		
		
		
		
		
	}

}
