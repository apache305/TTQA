package Util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.StringTokenizer;

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
	
	public static void write3DArray(double[][][] theta, String thetaFileName) throws IOException {
		// TODO Auto-generated method stub
		BufferedWriter writer = new BufferedWriter(new FileWriter(thetaFileName));
		writer.append(theta.length+"\n");
		writer.append(theta[0].length+"\n");
		writer.append(theta[0][0].length+"\n");
		
		for(int i = 0; i < theta.length; i++){
			for(int j = 0; j < theta[i].length; j++){
				for(int k = 0; k < theta[i][j].length; k++){
					writer.append(i + "\t" + j + "\t" + k + "\t" +  theta[i][j][k] + "\n");
				}
			}
			writer.flush();
		}
		writer.flush();
		writer.close();
	}
	
	

	public static void write2DArray(double[][] theta, String savefilename) throws IOException {
		// TODO Auto-generated method stub
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(savefilename));
		for(int i = 0; i < theta.length; i++){
			for(int j = 0; j < theta[i].length; j++){
				writer.append(theta[i][j] + "\t");
			}
			writer.append("\n");
		}
		writer.flush();
		writer.close();
	}

	public static double[][][] read3DArray(String file){
		
		//double [][][] something = new double []
		ArrayList<String> lines = new ArrayList<String>();
		ArrayList<String> tokens = new ArrayList<String>();
		readLines2(file, lines);
		int lid=0;
		tokenize(lines.get(lid), tokens);
		int imax = Integer.parseInt(tokens.get(0));
		lid+=1;
		tokens.clear();
		tokenize(lines.get(lid), tokens);
		int jmax = Integer.parseInt(tokens.get(0));
		lid+=1;
		tokens.clear();
		tokenize(lines.get(lid), tokens);
		int kmax = Integer.parseInt(tokens.get(0));
		lid+=1;
		
		double[][][] data = new double[imax][jmax][kmax];
		
		
		
		for(int i = 0; i < imax; i++) {
			for(int j=0;j<jmax;j++){
				for(int k=0;k<kmax;k++){
					tokens.clear();
					tokenize(lines.get(lid), tokens);
					data[i][j][k]= Double.parseDouble(tokens.get(0));
					lid+=1;
				}
			}
				
		}
		return data;
		
	}

	public static double[][] read2DArray(String file) {
		// TODO Auto-generated method stub
		ArrayList<String> lines = new ArrayList<String>();
		ArrayList<String> tokens = new ArrayList<String>();
		readLines2(file, lines);
	
		tokenize(lines.get(0), tokens);
		int d = tokens.size();
		
		double[][] data = new double[lines.size()][d];
		for(int i = 0; i < lines.size(); i++) {
			tokens.clear();
			tokenize(lines.get(i), tokens);
			for(int j = 0; j < d; j++) {
				data[i][j] = Double.parseDouble(tokens.get(j));
			}			
		}
		return data;
	}

	public static void readLines2(String file, ArrayList<String> lines) {
		BufferedReader reader = null;

		try {

			reader = new BufferedReader(new FileReader(new File(file)));

			String line = null;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
	public static void tokenize(String line, ArrayList<String> tokens) {
		StringTokenizer strTok = new StringTokenizer(line);
		while (strTok.hasMoreTokens()) {
			String token = strTok.nextToken();
			tokens.add(token);
		}
	}

	
	
	
	
}
