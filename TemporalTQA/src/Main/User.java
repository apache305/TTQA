package Main;

import java.util.ArrayList;

public class User {
	public String userId;
	public ArrayList<AnswerPost> answerPosts;
	public ArrayList<QuestionPost> questionPosts;

	public User(String userId) {
		// TODO Auto-generated constructor stub
		this.userId=userId;
		this.answerPosts = new ArrayList<AnswerPost>();
		this.questionPosts= new ArrayList<QuestionPost>();
	}
	
	public void addNewAnswerPost(ArrayList<String> eachAnswerInfo, Users users){
		
		String timeLabel = eachAnswerInfo.get(0).substring(0,7);
		eachAnswerInfo.remove(0);
		int qid=Integer.parseInt(eachAnswerInfo.get(0));
		eachAnswerInfo.remove(0);
		int score =Integer.parseInt(eachAnswerInfo.get(0)) ;
		eachAnswerInfo.remove(0);
		
		double cur_vote_log=0.0 ;
		if (score>=0){
			cur_vote_log=Math.log(score);
		}
		int vote_level=0;
		for(;vote_level< users.voteStep ;vote_level++){
			if( cur_vote_log <= users.voteMap[vote_level]){
				break;
			}
		}
		
		
		ArrayList<String> tagInfos = eachAnswerInfo;
		//System.out.println(timeLabel);
		int time= users.timeToIndexMap.get(timeLabel);
		int oldCount=0;
		oldCount=users.timeCountMap.get(timeLabel);
		users.timeCountMap.put(timeLabel,oldCount+1);
		int [] tags =  new int[tagInfos.size()];
		for(int i=0;i<tagInfos.size();i++	){
			String tag= tagInfos.get(i);
			if( ! users.tagToIndexMap.containsKey(tag)){
				int newIndex= users.tagToIndexMap.size();
				users.indexToTagMap.add(tag);
				users.tagToIndexMap.put(tag, newIndex);
				users.tagCountMap.put(tag, 1);	
				tags[i]=newIndex;
			}else{
				tags[i]=users.tagToIndexMap.get(tag);
				oldCount= users.tagCountMap.get(tag);
				users.tagCountMap.put(tag, oldCount+1);
			}
		}
		//System.out.print(score);
		//System.out.print("\t");
		//System.out.println(vote_level);
		AnswerPost ans= new AnswerPost(time,tags,qid,score, vote_level);
		this.answerPosts.add(ans);
		
		
	}
	
	
	
	
	
	
}