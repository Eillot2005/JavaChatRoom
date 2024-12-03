import java.util.ArrayList;
import java.util.List;

public class User {
    private int userId;
    private String username;
    private String password;
    private boolean onlineStatus;
    private List<User> friends;
    private List<User> friendRequests;
    private DatabaseManager databaseManager;

    public User(int userId, String username, String password, boolean onlineStatus, DatabaseManager databaseManager) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.onlineStatus = onlineStatus;
        this.friends = new ArrayList<>();
        this.friendRequests = new ArrayList<>();
        this.databaseManager = databaseManager;
    }

    public String getPassword() {
        //从数据库中获取用户的密码
        return password;
    }

    public String getPasswordFromDatabase() {
        //从数据库中获取用户的密码
        password = databaseManager.getPassword(this);
        return password;
    }

    public void setOnlineStatus(boolean b) {
        onlineStatus = b;
    }

    public void addFriend(User friend) {
        friends.add(friend);
        databaseManager.addFriendship(this, friend);
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public boolean isOnline() {
        return onlineStatus;
    }

    // 添加好友、删除好友、发送好友请求等方法...
    //添加好友
    public void addFriendRequest(User user) {
        friendRequests.add(user);
    }

    public void removeFriendRequest(User user) {
        friendRequests.remove(user);
    }

    public void acceptFriendRequest(User user) {
        friendRequests.remove(user);
        friends.add(user);
        user.addFriend(this);
    }

    public List<User> getFriends() {
        return friends;
    }

    public List<User> getFriendRequests() {
        return friendRequests;
    }

    public boolean isFriend(User user, List<User> Lfriends) {
        //friends中的元素的名称是否与user的名称相同
        System.out.println("搜索好友中。。。。。。");
        for (User friend : Lfriends) {
            System.out.println(friend.getUsername());
            if (friend.getUsername().equals(user.getUsername())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return username;
    }

    public boolean hasSentFriendRequestTo(User friend) {
        List<FriendRequest> friendRequests = databaseManager.getFriendRequests(this);
        System.out.println("已经发送好友请求的用户");
        for (FriendRequest request : friendRequests) {
            System.out.println(request.getReceiver().getUsername());
            if (request.getReceiver().getUsername().equals(friend.getUsername())) {
                return true;
            }
        }
        return false;
    }
}