package tukano.servers.java;

import java.util.List;
import java.util.logging.Logger;

import tukano.api.Short;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.persistence.Hibernate;
import tukano.api.java.Result.ErrorCode;

public class JavaShorts implements Shorts{

	private static Logger Log = Logger.getLogger(JavaShorts.class.getName());
	
	private int shortId = 1;
	private int blobId = 1;
	
	@Override
	public Result<Short> createShort(String userId, String pwd) {
		Log.info("createShort : user = " + userId + "; pwd = " + pwd);
		Short s = new Short(String.valueOf(shortId++), userId, "Blobs/blob"+ blobId +".txt");
		
		Hibernate.getInstance().persist(s);
		
		return Result.ok(s);
	}

	@Override
	public Result<Void> deleteShort(String shortId, String pwd) {
		Log.info("deleteShort : shortId = " + shortId + "; pwd = " + pwd);
		
		var query = Hibernate.getInstance().sql("SELECT s FROM Short s WHERE s.shortId = '" + shortId + "'", Short.class);
		if(query.isEmpty()) {
			Log.info("Short does not exist.");
			return Result.error( ErrorCode.NOT_FOUND);
		}
		Short s = query.get(0);
		Hibernate.getInstance().delete(s);
		
		return Result.ok();
	}

	@Override
	public Result<Short> getShort(String shortId) {
		Log.info("getShort : shortId = " + shortId);
		
		var query = Hibernate.getInstance().sql("SELECT s FROM Short s WHERE s.shortId = '" + shortId + "'", Short.class);
		if(query.isEmpty()) {
			Log.info("Short does not exist.");
			return Result.error( ErrorCode.NOT_FOUND);
		}
		Short s = query.get(0);
		
		return Result.ok(s);
	}

	@Override
	public Result<List<String>> getShorts(String userId) {
		
		
		var query = Hibernate.getInstance().sql("SELECT s.shortId FROM Short s WHERE s.userId = '" + userId + "'", String.class);
		
		return Result.ok(query);
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<List<String>> followers(String userId, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<Void> like(String shortId, String userId, boolean isLiked, String pwd) {
		Log.info("like : shortId = " + shortId + " ; userId = " + userId + " ; isLiked = " + isLiked + " ; pwd = " + pwd);
		
		if(shortId == null) {
			Log.info("ShortId null.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}
		var query = Hibernate.getInstance().sql("SELECT s FROM Short s WHERE s.shortId = '" + shortId + "'", Short.class);
		if(query.isEmpty()) {
			Log.info("Short does not exist.");
			return Result.error( ErrorCode.NOT_FOUND);
		}
		
		Short s = query.get(0);
		List<String> likedBy = s.getLikedBy();
		if(!likedBy.contains(userId) && !isLiked) {
			Log.info("The like being removed does not exist.");
			return Result.error( ErrorCode.NOT_FOUND);
		}
		
		if(likedBy.contains(userId) && isLiked) {
			Log.info("The like already exists.");
			return Result.error( ErrorCode.CONFLICT);
		}
		
		if(isLiked) {
			likedBy.add(userId);
			s.setTotalLikes(s.getTotalLikes()+1);
		}
		else {
			likedBy.remove(userId);
			s.setTotalLikes(s.getTotalLikes()-1);
		}
		s.setLikedBy(likedBy);
		Hibernate.getInstance().update(s);
		return Result.ok();
	}

	@Override
	public Result<List<String>> likes(String shortId, String pwd) {
		Log.info("likes : shortId = " + shortId + " ; pwd = " + pwd);
		
		var query = Hibernate.getInstance().sql("SELECT s FROM Short s WHERE s.shortId = '" + shortId + "'", Short.class);
		if(query.isEmpty()) {
			Log.info("Short does not exist.");
			return Result.error( ErrorCode.NOT_FOUND);
		}
		
		return Result.ok(query.get(0).getLikedBy());
	}

	@Override
	public Result<List<String>> getFeed(String userId, String password) {
		// TODO Auto-generated method stub
		return null;
	}

}
