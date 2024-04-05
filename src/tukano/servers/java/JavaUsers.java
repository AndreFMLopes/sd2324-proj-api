package tukano.servers.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.api.java.Users;

public class JavaUsers implements Users{
	private final Map<String,User> users = new HashMap<>();

	private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

	@Override
	public Result<String> createUser(User user) {
		Log.info("createUser : " + user);
		
		// Check if user data is valid
		if(user.userId() == null || user.pwd() == null || user.displayName() == null || user.email() == null) {
			Log.info("User object invalid.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}
		
		// Insert user, checking if name already exists
		if( users.putIfAbsent(user.userId(), user) != null ) {
			Log.info("User already exists.");
			return Result.error( ErrorCode.CONFLICT);
		}
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
		
		User user = users.get(userId);			
		// Check if user exists 
		if( user == null ) {
			Log.info("User does not exist.");
			return Result.error( ErrorCode.NOT_FOUND);
		}
		
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
		if(userId == null || pwd == null) {
			Log.info("Name or Password null.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}
				
		// Check if user exists 
		if( users.get(userId) == null ) {
			Log.info("User does not exist.");
			return Result.error( ErrorCode.NOT_FOUND);
		}
		
		//Check if the password is correct
		if( !user.pwd().equals( pwd)) {
			Log.info("Password is incorrect.");
			return Result.error( ErrorCode.FORBIDDEN);
		}
		User updatedUser = users.get(userId);
		if(user.getDisplayName() != null)updatedUser.setDisplayName(user.getDisplayName());
		if(user.getEmail() != null)updatedUser.setEmail(user.getEmail());
		if(user.getPwd() != null)updatedUser.setPwd(user.getPwd());
		
		return Result.ok(users.get(userId));
	}

	@Override
	public Result<User> deleteUser(String userId, String pwd) {
		Log.info("deleteUser : user = " + userId + "; pwd = " + pwd);
		
		// Check if user is valid
		if(userId == null || pwd == null) {
			Log.info("Name or Password null.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}
		
		User user = users.get(userId);			
		// Check if user exists 
		if( user == null ) {
			Log.info("User does not exist.");
			return Result.error( ErrorCode.NOT_FOUND);
		}
		
		//Check if the password is correct
		if( !user.pwd().equals( pwd)) {
			Log.info("Password is incorrect.");
			return Result.error( ErrorCode.FORBIDDEN);
		}
		
		users.remove(userId);
		
		return Result.ok(user);
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
        Log.info("searchUsers : pattern = " + pattern);

		// Check if pattern is valid
		if(pattern == null) {
			Log.info("Pattern null.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}
		
		List<User> result = new ArrayList<User>();
		for (Entry<String, User> entry : users.entrySet()) {
	        if (entry.getKey().toUpperCase().startsWith(pattern.toUpperCase())) {
	        	User u = entry.getValue();
	            result.add(new User(u.getUserId(),"",u.getEmail(),u.getDisplayName()));
	        }
	    }
		return Result.ok(result);
	}

}
