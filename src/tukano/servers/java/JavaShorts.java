package tukano.servers.java;

import java.net.URI;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.function.Function;

import tukano.Discovery;
import tukano.api.Follow;
import tukano.api.Short;
import tukano.api.User;
import tukano.api.Likes;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.api.java.Users;
import tukano.clients.ClientFactory;
import tukano.persistence.Hibernate;
import tukano.api.java.Result.ErrorCode;

public class JavaShorts implements Shorts {

    private static Logger Log = Logger.getLogger(JavaShorts.class.getName());

    private static int shortId = 1;
    private static int blobId = 1;
    private static Map<Integer, String> blobLocations = new HashMap<>(); // Where all blobs are located
    private Set<String> blobServers = new HashSet<>(); //All known blob server URLs

    @Override
    public Result<Short> createShort(String userId, String pwd) {
        Log.info("createShort : user = " + userId + "; pwd = " + pwd);

        Result<User> owner = checkUser(userId, pwd);

        if (!owner.isOK()) {
            return Result.error(owner.error());
        }

        // Discover and add new blob servers
        URI[] blobUris = Discovery.getInstance().knownUrisOf("blobs");
        for (URI uri : blobUris) {
            if (uri != null) {
                blobServers.add(uri.toString());
            }
        }

        if (blobServers.isEmpty()) {
            Log.info("Blob servers not online.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        // If there are servers with no blobs
        if (blobId <= blobServers.size()) {
            for (String server : blobServers) {
                if (!blobLocations.containsValue(server)) { // Check if server has blobs
                    blobLocations.put(blobId, server);
                }
            }
        } else { // If all servers already have blobs, find the one with less blobs
            String leastUsedServer = getLeastUsedServer();
            System.out.println(leastUsedServer);
            blobLocations.put(blobId, leastUsedServer);
        }

        String blobUrl = blobLocations.get(blobId) + "/blobs/" + blobId;
        blobId++;

        Short s = new Short(String.valueOf(shortId++), userId, blobUrl);

        Hibernate.getInstance().persist(s);
        return Result.ok(s);
    }

    @Override
    public Result<Void> deleteShort(String shortId, String pwd) {
        Log.info("deleteShort : shortId = " + shortId + "; pwd = " + pwd);

        var shortQuery = Hibernate.getInstance().jpql("SELECT s FROM Short s WHERE s.shortId = '" + shortId + "'", Short.class);

        // Check if short exists
        if (shortQuery.isEmpty()) {
            Log.info("Short does not exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        Short s = shortQuery.get(0);

        Result<User> owner = checkUser(s.getOwnerId(), pwd);

        if (!owner.isOK()) {
            return Result.error(owner.error());
        }

        var likes = Hibernate.getInstance().jpql("SELECT l FROM Likes l WHERE l.likedShortId = '" + s.getShortId() + "'", Likes.class);
        if (!likes.isEmpty()) Hibernate.getInstance().delete(likes.get(0));
        Hibernate.getInstance().delete(s);

        return Result.ok();
    }

    @Override
    public Result<Short> getShort(String shortId) {
        Log.info("getShort : shortId = " + shortId);

        var query = Hibernate.getInstance().jpql("SELECT s FROM Short s WHERE s.shortId = '" + shortId + "'", Short.class);

        // Check if short exists
        if (query.isEmpty()) {
            Log.info("Short does not exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }
        Short s = query.get(0);

        return Result.ok(s);
    }

    @Override
    public Result<List<String>> getShorts(String userId) {

        Result<User> owner = checkUser(userId, "1");

        if (!owner.isOK()) {
            if (!(owner.error() == ErrorCode.FORBIDDEN)) return Result.error(owner.error());
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
            if (!(user2.error() == ErrorCode.FORBIDDEN)) return Result.error(user2.error());
        }

        var query = Hibernate.getInstance().jpql("SELECT f FROM Follow f WHERE f.followedUserId = '" + userId2 + "'", Follow.class);
        var query2 = Hibernate.getInstance().jpql("SELECT f FROM Follow f WHERE f.followedUserId = '" + userId1 + "'", Follow.class);

        Follow f;
        Follow f2;

        if (query.isEmpty()) {
            f = new Follow(userId2);
            Hibernate.getInstance().persist(f);
        } else f = query.get(0);

        if (query2.isEmpty()) {
            f2 = new Follow(userId1);
            Hibernate.getInstance().persist(f2);
        } else f2 = query2.get(0);

        List<String> followers = f.getFollowers();
        List<String> follows = f2.getFollows();

        if (isFollowing) {
            if (!followers.contains(userId1)) {
                followers.add(userId1);
                follows.add(userId2);
            } else {
                Log.info("follow already exists.");
                return Result.error(ErrorCode.CONFLICT);
            }
        } else {
            if (followers.contains(userId1)) {
                followers.remove(userId1);
                follows.remove(userId2);
            }
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
        if (query.isEmpty()) return Result.ok(new ArrayList<String>());
        return Result.ok(query.get(0).getFollowers());
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String pwd) {
        Log.info("like : shortId = " + shortId + " ; userId = " + userId + " ; isLiked = " + isLiked + " ; pwd = " + pwd);

        // Check if provided info is valid
        if (shortId == null || userId == null) {
            Log.info("ShortId null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        var query = Hibernate.getInstance().jpql("SELECT s FROM Short s WHERE s.shortId = '" + shortId + "'", Short.class);

        // Check if short exists
        if (query.isEmpty()) {
            Log.info("Short does not exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        Result<User> user = checkUser(userId, pwd);

        if (!user.isOK()) {
            return Result.error(user.error());
        }

        var query2 = Hibernate.getInstance().jpql("SELECT l FROM Likes l WHERE l.likedShortId = '" + shortId + "'", Likes.class);

        Short s = query.get(0);

        Likes l;
        if (query2.isEmpty()) {
            l = new Likes(shortId);
            Hibernate.getInstance().persist(l);
        } else l = query2.get(0);
        List<String> likedBy = l.getLikedBy();

        // Check if like to be removed does not exist
        if (!likedBy.contains(userId) && !isLiked) {
            Log.info("The like being removed does not exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        // Check if like exists
        if (likedBy.contains(userId) && isLiked) {
            Log.info("The like already exists.");
            return Result.error(ErrorCode.CONFLICT);
        }

        if (isLiked) {
            likedBy.add(userId);
            s.setTotalLikes(s.getTotalLikes() + 1);
        } else {
            likedBy.remove(userId);
            s.setTotalLikes(s.getTotalLikes() - 1);
        }
        l.setLikedBy(likedBy);
        Hibernate.getInstance().update(s);
        Hibernate.getInstance().update(l);
        return Result.ok();
    }

    @Override
    public Result<List<String>> likes(String shortId, String pwd) {
        Log.info("likes : shortId = " + shortId + " ; pwd = " + pwd);

        // Check if short exists
        var query = Hibernate.getInstance().jpql("SELECT s FROM Short s WHERE s.shortId = '" + shortId + "'", Short.class);
        if (query.isEmpty()) {
            Log.info("Short does not exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        Short s = query.get(0);

        Result<User> user = checkUser(s.getOwnerId(), pwd);

        if (!user.isOK()) {
            return Result.error(user.error());
        }

        var query2 = Hibernate.getInstance().jpql("SELECT l FROM Likes l WHERE l.likedShortId = '" + shortId + "'", Likes.class);
        if (query2.isEmpty()) return Result.ok(new ArrayList<String>());
        return Result.ok(query2.get(0).getLikedBy());
    }

    @Override
    public Result<List<String>> getFeed(String userId, String pwd) {
        Log.info("getFeed : userId = " + userId + " ; pwd = " + pwd);

        Result<User> user = checkUser(userId, pwd);

        if (!user.isOK()) {
            return Result.error(user.error());
        }

        var userShorts = Hibernate.getInstance().jpql("SELECT s FROM Short s WHERE s.ownerId = '" + userId + "'", Short.class);
        List<Short> shorts = new ArrayList<Short>(userShorts);

        var followActivity = Hibernate.getInstance().jpql("SELECT f FROM Follow f WHERE f.followedUserId = '" + userId + "'", Follow.class);

        if (!followActivity.isEmpty()) {
            List<String> follows = followActivity.get(0).getFollows();
            // Getting followed users' shorts
            for (String id : follows) {
                if (!id.equals(userId)) {
                    var followedUserShorts = Hibernate.getInstance().jpql("SELECT s FROM Short s WHERE s.ownerId = '" + id + "'", Short.class);
                    shorts.addAll(followedUserShorts);
                }
            }
        }

        // Sort all shorts by timestamp
        shorts.sort(Comparator.comparingLong(Short::getTimestamp).reversed());

        // Extract shortId only from shorts list
        List<String> shortIds = shorts.stream().map(Short::getShortId).collect(Collectors.toList());

        return Result.ok(shortIds);
    }

    public Result<Void> deleteAllAboutUser(String userId, String pwd) {
        Log.info("deleteAboutByUser : userId = " + userId + " ; pwd = " + pwd);

        Result<User> user = checkUser(userId, pwd);

        if (!user.isOK()) {
            return Result.error(user.error());
        }

        var userShorts = Hibernate.getInstance().jpql("SELECT s FROM Short s WHERE s.ownerId = '" + userId + "'", Short.class);
        for (Short s : userShorts) {
            var likes = Hibernate.getInstance().jpql("SELECT l FROM Likes l WHERE l.likedShortId = '" + s.getShortId() + "'", Likes.class);
            if (!likes.isEmpty()) Hibernate.getInstance().delete(likes.get(0));

            String[] urlTokens = s.getBlobUrl().split("/");
            ClientFactory.getBlobsClient(s.getBlobUrl()).value().deleteBlob(urlTokens[urlTokens.length - 1]);

            Hibernate.getInstance().delete(s);
        }

        var likes = Hibernate.getInstance().jpql("SELECT l FROM Likes l", Likes.class);
        for (Likes l : likes) {
            List<String> likedBy = l.getLikedBy();
            if (likedBy.remove(userId)) Hibernate.getInstance().update(l);
        }

        var follow = Hibernate.getInstance().jpql("SELECT f FROM Follow f WHERE f.followedUserId = '" + userId + "'", Follow.class);
        if (follow.isEmpty()) return Result.ok();
        List<String> followers = follow.get(0).getFollowers();
        for (String follower : followers) {
            var f = Hibernate.getInstance().jpql("SELECT f FROM Follow f WHERE f.followedUserId = '" + follower + "'", Follow.class);
            List<String> follows = f.get(0).getFollows();
            if (follows.remove(userId)) Hibernate.getInstance().update(f.get(0));
        }

        List<String> follows = follow.get(0).getFollows();
        for (String followed : follows) {
            var f = Hibernate.getInstance().jpql("SELECT f FROM Follow f WHERE f.followedUserId = '" + followed + "'", Follow.class);
            List<String> followersTemp = f.get(0).getFollowers();
            if (followersTemp.remove(userId)) Hibernate.getInstance().update(f.get(0));
        }

        return Result.ok();
    }

    private Result<User> checkUser(String userId, String pwd) {
        Result<Users> usersClient = ClientFactory.getUsersClient();

        if (!usersClient.isOK()) {
            Log.info("Server error");
            return Result.error(ErrorCode.BAD_REQUEST);
        }
        return usersClient.value().getUser(userId, pwd);
    }

    public static String getLeastUsedServer() {
        List<String> usedServers = new ArrayList<String>(blobLocations.values());

        int min = Integer.MAX_VALUE;
        String server = "";

        for (String sv : usedServers) {
            int count = Collections.frequency(usedServers, sv);
            if (count < min) {
                min = count;
                server = sv;
            }
        }
        return server;
    }

}
