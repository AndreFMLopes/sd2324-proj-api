package tukano.api;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Follow {

	@Id
	String followedUserId;
	List<String> followers = new ArrayList<String>();
	List<String> follows = new ArrayList<String>();
	
	public Follow() {}
	
	public Follow(String followedUserId) {
		this.followedUserId = followedUserId;
	}
	
	public String getFollowedUserId() {
		return followedUserId;
	}
	
	public void setFollowedUserId(String followedUserId) {
		this.followedUserId = followedUserId;
	}
	
	public List<String> getFollowers() {
		return followers;
	}
	
	public void setFollowers(List<String> followers) {
		this.followers = followers;
	}
	
	public List<String> getFollows() {
		return follows;
	}
	
	public void setFollows(List<String> follows) {
		this.follows = follows;
	}
}
