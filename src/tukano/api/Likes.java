package tukano.api;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Id;

public class Likes {
	
	@Id
	String likedShortId;
	List<String> likedBy = new ArrayList<String>();
	
	public Likes() {}
	
	public Likes(String likedShortId) {
		this.likedShortId = likedShortId;
	}
	
	public String getLikedShortId() {
		return likedShortId;
	}
	public void setLikedShortId(String likedShortId) {
		this.likedShortId = likedShortId;
	}
	public List<String> getLikedBy() {
		return likedBy;
	}
	public void setLikedBy(List<String> likedBy) {
		this.likedBy = likedBy;
	}
	
	

}
