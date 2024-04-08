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
	
	private int id = 0;
	
	@Override
	public Result<Short> createShort(String userId, String pwd) {
		Log.info("createShort : user = " + userId + "; pwd = " + pwd);
		
		Short s = new Short(String.valueOf(id++), userId, "blob.txt");
		
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
		if(query.isEmpty()) {
			Log.info("User does not exist.");
			return Result.error( ErrorCode.NOT_FOUND);
		}
		
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
	public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<List<String>> likes(String shortId, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result<List<String>> getFeed(String userId, String password) {
		// TODO Auto-generated method stub
		return null;
	}

}
