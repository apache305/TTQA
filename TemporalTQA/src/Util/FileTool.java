package Util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class FileTool {
	public static void readLines(String file, ArrayList<String> lines){
		BufferedReader reader=null;
		try{
			reader=new BufferedReader(new FileReader(new File(file)));
			String eachLine=null;
			while( (eachLine=reader.readLine())!=null){
				lines.add(eachLine);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(reader!=null){
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
			
		
	}
	
	public static BufferedWriter getNewWriter(String filepath){
		BufferedWriter writer =null;
		try {
			writer = new BufferedWriter(new FileWriter( filepath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return writer;
	}
	
	public static void readLinesAsTaglist(String file,ArrayList<ArrayList<String>> lines){
		BufferedReader reader=null;
		String [] parts=null;
		ArrayList<String> taglist=null;
		try{
			reader=new BufferedReader(new FileReader(new File(file)));
			String eachLine=null;
			while( (eachLine=reader.readLine())!=null){
				parts=eachLine.split(" ");
				taglist=new ArrayList<String>(Arrays.asList(parts));
				lines.add(taglist);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(reader!=null){
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public static void printFSArray(float [] f){
		StringBuilder sb=new StringBuilder();
		for(float ef:f){
			sb.append(ef);
			sb.append(" ");
		}
		System.out.println(sb.toString());
	}
	
	public static <T> void printFSArray(Collection<T> collection){
		StringBuilder sb=new StringBuilder();
		for(T ef:collection){
			sb.append(ef.toString());
			sb.append(" ");
		}
		System.out.println(sb.toString());
		
	}
	public static <T> void printFSArray(T[] tf){
		StringBuilder sb=new StringBuilder();
		for(T ef:tf){
			sb.append(ef);
			sb.append(" ");
		}
		System.out.println(sb.toString());
		
	}
	
	
	
}
