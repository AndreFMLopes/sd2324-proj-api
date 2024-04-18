package tukano.servers.java;

import java.util.*;
import java.util.logging.Logger;

import tukano.api.Follow;
import tukano.api.Short;
import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.api.java.Users;
import tukano.clients.ClientFactory;
import tukano.persistence.Hibernate;
import tukano.api.java.Result.ErrorCode;

public class JavaShorts implements Shorts {

	private static Logger Log = Logger.getLogger(JavaShorts.class.getName());

	private int blobId = 1; // ?

	@Override
	public Result<Short> createShort(String userId, String pwd) {
		Log.info("createShort : user = " + userId + "; pwd = " + pwd);
		
		Result<User> owner = checkUser(userId, pwd);

		if (!owner.isOK()) {
			return Result.error(owner.error());
		}

		String shortId = getNextShortId();
		Short s = new Short(shortId, userId, "blob" + blobId);

		Hibernate.getInstance().persist(s);
		return Result.ok(s);
	}

	@Override
	public Result<Void> deleteShort(String shortId, String pwd) {
		Log.info("deleteShort : shortId = " + shortId + "; pwd = " + pwd);

		var shortQuery = Hibernate.getInstance().jpql("SELECT s FROM Short s WHERE s.shortId = '" + shortId + "'", Short.class);

		// Check if short exists
		if(shortQuery.isEmpty()) {
			Log.info("Short does not exist.");
			return Result.error( ErrorCode.NOT_FOUND);
		}

		Short s = shortQuery.get(0);

		Result<User> owner = checkUser(s.getOwnerId(), pwd);

		if (!owner.isOK()) {
			return Result.error(owner.error());
		}

		Hibernate.getInstance().delete(s);

		return Result.ok();
	}

	@Override
	public Result<Short> getShort(String shortId) {
		Log.info("getShort : shortId = " + shortId);

		var query = Hibernate.getInstance().jpql("SELECT s FROM Short s WHERE s.shortId = '" + shortId + "'", Short.class);

		// Check if short exists
		if(query.isEmpty()) {
			Log.info("Short does not exist.");
			return Result.error( ErrorCode.NOT_FOUND);
		}
		Short s = query.get(0);

		return Result.ok(s);
	}

	@Override
	public Result<List<String>> getShorts(String userId) {
		
		Result<User> owner = checkUser(userId, "1");

		if (!owner.isOK()) {
			if(!(owner.error() == ErrorCode.FORBIDDEN)) return Result.error(owner.error());
		}

		var query = Hibernate.getInstance().jpql("SELECT s.shortId FROM Short s WHERE s.ownerId = '" + userId + "'", String.class);

		return Result.ok(query);
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String pwd) {
		Log.info("follow : userId1 = " + userId1 + " ; userId2 = " + userId2 + " ; isFollowing = " + isFollowing + " ; pwd = " + pwd);

		Result<User> user1 = checkUser(userId1, pwd);

		if (!user1.isOK()) {
			return Result.error(user1.error());
		}
		
		Result<User> user2 = checkUser(userId2, "1");

		if (!user2.isOK()) {
			if(!(user2.error() == ErrorCode.FORBIDDEN)) return Result.error(user2.error());
		}

		var query = Hibernate.getInstance().jpql("SELECT f FROM Follow f WHERE f.followedUserId = '" + userId2 + "'", Follow.class);
		var query2 = Hibernate.getInstance().jpql("SELECT f FROM Follow f WHERE f.followedUserId = '" + userId1 + "'", Follow.class);
		Follow f = query.get(0);
		List<String> followers = f.getFollowers();
		Follow f2 = query2.get(0);
		List<String> follows = f2.getFollows();

		if(!followers.contains(userId1) && isFollowing) {
			followers.add(userId1);
			follows.add(userId2);
		}
		if(followers.contains(userId1) && !isFollowing) {
			followers.remove(userId1);
			follows.remove(userId2);
		}

		f.setFollowers(followers);
		f2.setFollows(follows);
		Hibernate.getInstance().update(f);
		Hibernate.getInstance().update(f2);

		return Result.ok();
	}

	@Override
	public Result<List<String>> followers(String userId, String pwd) {
		Log.info("followers : userId = " + userId + " ; pwd = " + pwd);

		Result<User> user = checkUser(userId, pwd);

		if (!user.isOK()) {
			return Result.error(user.error());
		}

		var query = Hibernate.getInstance().jpql("SELECT f FROM Follow f WHERE f.followedUserId = '" + userId + "'", Follow.class);

		return Result.ok(query.get(0).getFollowers());
	}

	@Override
	public Result<Void> like(String shortId, String userId, boolean isLiked, String pwd) {
		Log.info("like : shortId = " + shortId + " ; userId = " + userId + " ; isLiked = " + isLiked + " ; pwd = " + pwd);

		// Check if provided info is valid
		if(shortId == null || userId == null) {
			Log.info("ShortId null.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}

		var query = Hibernate.getInstance().jpql("SELECT s FROM Short s WHERE s.shortId = '" + shortId + "'", Short.class);

		// Check if short exists
		if(query.isEmpty()) {
			Log.info("Short does not exist.");
			return Result.error( ErrorCode.NOT_FOUND);
		}
		
		Result<User> user = checkUser(userId, pwd);

		if (!user.isOK()) {
			return Result.error(user.error());
		}

		Short s = query.get(0);
		/*List<String> likedBy = s.getLikedBy();

		// Check if like to be removed does not exist
		if(!likedBy.contains(userId) && !isLiked) {
			Log.info("The like being removed does not exist.");
			return Result.error( ErrorCode.NOT_FOUND);
		}

		// Check if like exists
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
		Hibernate.getInstance().update(s);*/
		return Result.ok();
	}

	@Override
	public Result<List<String>> likes(String shortId, String pwd) {
		Log.info("likes : shortId = " + shortId + " ; pwd = " + pwd);

		// Check if short exists
		var query = Hibernate.getInstance().jpql("SELECT s FROM Short s WHERE s.shortId = '" + shortId + "'", Short.class);
		if(query.isEmpty()) {
			Log.info("Short does not exist.");
			return Result.error( ErrorCode.NOT_FOUND);
		}

		Short s = query.get(0);

		Result<User> user = checkUser(s.getOwnerId(), pwd);

		if (!user.isOK()) {
			return Result.error(user.error());
		}

		//return Result.ok(query.get(0).getLikedBy());
		return Result.ok(null);
	}

	@Override
	public Result<List<String>> getFeed(String userId, String pwd) {
		Log.info("getFeed : userId = " + userId + " ; pwd = " + pwd);

		Result<User> user = checkUser(userId, pwd);

		if (!user.isOK()) {
			return Result.error(user.error());
		}

		var query = Hibernate.getInstance().jpql("SELECT f FROM Follow f WHERE f.followedUserId = '" + userId + "'", Follow.class);
		List<String> follows = query.get(0).getFollows();

		List<String> shorts = new ArrayList<String>();
		for(String id : follows) {
			var query2 = Hibernate.getInstance().jpql("SELECT s FROM Short s WHERE s.ownerId = '" + id + "'", Short.class);
			shorts.add(query2.get(0).getShortId());
		}

		return Result.ok(shorts);
	}

	private String getNextShortId(){
		var query = Hibernate.getInstance().jpql("SELECT s.shortId FROM Short s ORDER BY s.shortId DESC", String.class);
		if (query.isEmpty()) {
			return "1";
		}
		return String.valueOf(Integer.parseInt(query.get(0)) + 1);
	}
	
	private Result<User> checkUser(String userId, String pwd){
		Result<Users> usersClient = ClientFactory.getUsersClient();

		if (!usersClient.isOK()) {
			Log.info("Server error");
			return Result.error(ErrorCode.BAD_REQUEST);
		}

		Users usersServer = usersClient.value();
		Result<User> user = usersServer.getUser(userId, pwd);

		return user;
	}

}
