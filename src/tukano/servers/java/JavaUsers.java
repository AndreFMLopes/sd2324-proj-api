package tukano.servers.java;


import java.util.List;
import java.util.logging.Logger;

import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.persistence.Hibernate;
import tukano.api.java.Users;

public class JavaUsers implements Users{

	private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

	@Override
	public Result<String> createUser(User user) {
		Log.info("createUser : " + user);
		
		// Check if user data is valid
		if(user.userId() == null || user.pwd() == null || user.displayName() == null || user.email() == null) {
			Log.info("User object invalid.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}

		var query = Hibernate.getInstance().jpql("SELECT u FROM User u WHERE u.userId = '" + user.getUserId() + "'", User.class);
		if(!query.isEmpty()) {
			Log.info("User already exists.");
			return Result.error( ErrorCode.CONFLICT);
		}
		Hibernate.getInstance().persist(user);
		return Result.ok( user.userId() );
	}

	@Override
	public Result<User> getUser(String userId, String pwd) {
		Log.info("getUser : user = " + userId + "; pwd = " + pwd);
		
		// Check if user is valid
		if(userId == null || pwd == null) {
			Log.info("Name or Password null.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}
		
		var query = Hibernate.getInstance().jpql("SELECT u FROM User u WHERE u.userId = '" + userId + "'", User.class);
		if(query.isEmpty()) {
			Log.info("User does not exist.");
			return Result.error( ErrorCode.NOT_FOUND);
		}
		User user = query.get(0);
		//Check if the password is correct
		if( !user.pwd().equals( pwd)) {
			Log.info("Password is incorrect.");
			return Result.error( ErrorCode.FORBIDDEN);
		}
		
		return Result.ok(user);
	}

	@Override
	public Result<User> updateUser(String userId, String pwd, User user) {
		Log.info("updateUser : user = " + userId + "; pwd = " + pwd);
		
		// Check if user is valid
		if(userId == null || pwd == null || user == null) {
			Log.info("Name, Password or user null.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}
		
		if(user.getUserId() != null) {
			Log.info("Can't change userId.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}
		
		var query = Hibernate.getInstance().jpql("SELECT u FROM User u WHERE u.userId = '" + userId + "'", User.class);
		if(query.isEmpty()) {
			Log.info("User does not exist.");
			return Result.error( ErrorCode.NOT_FOUND);
		}
		User u = query.get(0);
		//Check if the password is correct
		if( !u.pwd().equals( pwd)) {
			Log.info("Password is incorrect.");
			return Result.error( ErrorCode.FORBIDDEN);
		}
		if(user.getDisplayName() != null)u.setDisplayName(user.getDisplayName());
		if(user.getEmail() != null)u.setEmail(user.getEmail());
		if(user.getPwd() != null)u.setPwd(user.getPwd());
		Hibernate.getInstance().update(u);
		
		return Result.ok(u);
	}

	@Override
	public Result<User> deleteUser(String userId, String pwd) {
		Log.info("deleteUser : user = " + userId + "; pwd = " + pwd);
		
		// Check if user is valid
		if(userId == null || pwd == null) {
			Log.info("Name or Password null.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}
				
		var query = Hibernate.getInstance().jpql("SELECT u FROM User u WHERE u.userId = '" + userId + "'", User.class);
		if(query.isEmpty()) {
			Log.info("User does not exist.");
			return Result.error( ErrorCode.NOT_FOUND);
		}
		User u = query.get(0);
		//Check if the password is correct
		if( !u.pwd().equals( pwd)) {
			Log.info("Password is incorrect.");
			return Result.error( ErrorCode.FORBIDDEN);
		}
		
		Hibernate.getInstance().delete(u);
		
		return Result.ok(u);
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
		Log.info("searchUsers : pattern = " + pattern);
		// Check if pattern is valid
		if(pattern == null) {
			Log.info("Pattern null.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}
		
		var query = Hibernate.getInstance().jpql("SELECT u FROM User u WHERE UPPER(u.userId) LIKE UPPER('%" + pattern + "%')", User.class);
		
		return Result.ok(query);
	}
	
	public Result<Void> checkPassword(String userId, String pwd){
		Log.info("checkPassword : userId = " + userId + "; pwd = " + pwd);
		// Check if user is valid
		if(userId == null || pwd == null) {
			Log.info("Name or Password null.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}
		var query = Hibernate.getInstance().jpql("SELECT u FROM User u WHERE u.userId = '" + userId + "'", User.class);
		if(query.isEmpty()) {
			Log.info("User does not exist.");
			return Result.error( ErrorCode.NOT_FOUND);
		}
		User u = query.get(0);
		//Check if the password is correct
		if( !u.pwd().equals( pwd)) {
			Log.info("Password is incorrect.");
			return Result.error( ErrorCode.FORBIDDEN);
		}
		return Result.ok();
	}

}
