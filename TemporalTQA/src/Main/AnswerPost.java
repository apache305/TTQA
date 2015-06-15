package Main;

public class AnswerPost{
	public int [] Qtags;
	public int Atime;
	public int score;
	public int vote_level;
	public int qid;
	public AnswerPost(int time, int[] tags, int qid,int score,int vote_level) {
		// TODO Auto-generated constructor stub
		this.Atime=time;
		this.Qtags=tags;
		this.qid=qid;
		this.score=score;
		this.vote_level= vote_level;
		
	}
	

}