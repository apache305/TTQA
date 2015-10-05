package Main;

import java.util.ArrayList;

public class User {
	public String userId;
	public ArrayList<Post> allPosts;
	public ArrayList<AnswerPost> answerPosts;
	public ArrayList<QuestionPost> questionPosts;

	public User(String userId) {
		// TODO Auto-generated constructor stub
		this.userId=userId;
		this.allPosts=new ArrayList<Post>();
		this.answerPosts = new ArrayList<AnswerPost>();
		this.questionPosts= new ArrayList<QuestionPost>();
	}
	
	
	
	
	
	
	
	
}