package Main;

import java.util.ArrayList;

public class AnswerPost{
	public int [] Qtags;
	public int Atime;
	public int score;
	public int vote_level;
	public int qid;
	
	public String aid;
	public User user;
	public String date;
	public QuestionPost question;
	public ArrayList<Integer> words;
	public AnswerPost(){
		
	}
	public AnswerPost(int time, int[] tags, int qid,int score,int vote_level) {
		// TODO Auto-generated constructor stub
		this.Atime=time;
		this.Qtags=tags;
		this.qid=qid;
		this.score=score;
		this.vote_level= vote_level;
		
	}
	

}