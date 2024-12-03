import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DatabaseManager {
    private Connection connection;

    public DatabaseManager() {
        try {
            createDatabaseIfNotExists();
            connection = DriverManager.getConnection("jdbc:sqlserver://localhost;databaseName=ChatApp;user=sa;password=abc;encrypt=false");
            initializeDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public DatabaseManager(Connection connection) {
        this.connection = connection;
    }

    public void addFriendship(User user1, User user2) {
        String query = "INSERT INTO Friendships (user1_id, user2_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, user1.getUserId());
            stmt.setInt(2, user2.getUserId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createDatabaseIfNotExists() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlserver://localhost;user=sa;password=abc;encrypt=true;trustServerCertificate=true");
             Statement stmt = conn.createStatement()) {
            ResultSet resultSet = stmt.executeQuery("SELECT name FROM master.dbo.sysdatabases WHERE name = 'ChatApp'");
            if (!resultSet.next()) {
                stmt.executeUpdate("CREATE DATABASE ChatApp");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeDatabase() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("IF OBJECT_ID('Users', 'U') IS NULL CREATE TABLE Users (user_id INT PRIMARY KEY IDENTITY, username VARCHAR(50), password VARCHAR(50), online_status BIT)");
            stmt.execute("IF OBJECT_ID('FriendRequests', 'U') IS NULL CREATE TABLE FriendRequests (request_id INT PRIMARY KEY IDENTITY, sender_id INT, receiver_id INT, status VARCHAR(20))");
            stmt.execute("IF OBJECT_ID('PrivateMessages', 'U') IS NULL CREATE TABLE PrivateMessages (message_id INT PRIMARY KEY IDENTITY, sender_id INT, receiver_id INT, message TEXT, timestamp DATETIME)");
            stmt.execute("IF OBJECT_ID('PublicMessages', 'U') IS NULL CREATE TABLE PublicMessages (message_id INT PRIMARY KEY IDENTITY, sender_id INT, message TEXT, timestamp DATETIME)");
            stmt.execute("IF OBJECT_ID('GroupChats', 'U') IS NULL CREATE TABLE GroupChats (group_id INT PRIMARY KEY IDENTITY, group_name VARCHAR(50))");
            stmt.execute("IF OBJECT_ID('GroupMembers', 'U') IS NULL CREATE TABLE GroupMembers (group_id INT, user_id INT)");
            stmt.execute("IF OBJECT_ID('GroupMessages', 'U') IS NULL CREATE TABLE GroupMessages (message_id INT PRIMARY KEY IDENTITY, group_id INT, sender_id INT, message TEXT, timestamp DATETIME)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User getUser(String username) {
        String query = "SELECT * FROM Users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("password"), rs.getBoolean("online_status"), this);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateUserOnlineStatus(User user, boolean status) {
        String query = "UPDATE Users SET online_status = ? WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setBoolean(1, status);
            stmt.setInt(2, user.getUserId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addUser(User user) {
        String query = "INSERT INTO Users (username, password, online_status) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setBoolean(3, user.isOnline());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Collection<User> getOnlineUsers() {
        Collection<User> onlineUsers = new ArrayList<>();
        String query = "SELECT * FROM Users WHERE online_status = 1";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                onlineUsers.add(new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("password"), rs.getBoolean("online_status"), this));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return onlineUsers;
    }

    public void addPublicMessage(PublicMessage publicMessage) {
        String query = "INSERT INTO PublicMessages (sender_id, message, timestamp) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, publicMessage.getSenderId());
            stmt.setString(2, publicMessage.getMessage());
            stmt.setTimestamp(3, publicMessage.getTimestamp());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addGroupMessage(GroupMessage groupMessage) {
        String query = "INSERT INTO GroupMessages (group_id, sender_id, message, timestamp) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, groupMessage.getGroupId());
            stmt.setInt(2, groupMessage.getSenderId());
            stmt.setString(3, groupMessage.getMessage());
            stmt.setTimestamp(4, groupMessage.getTimestamp());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addPrivateMessage(PrivateMessage privateMessage) {
        String query = "INSERT INTO PrivateMessages (sender_id, receiver_id, message, timestamp) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, privateMessage.getSenderId());
            stmt.setInt(2, privateMessage.getReceiverId());
            stmt.setString(3, privateMessage.getMessage());
            stmt.setTimestamp(4, privateMessage.getTimestamp());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<PrivateMessage> getPrivateMessages(User user1, User user2) {
        List<PrivateMessage> messages = new ArrayList<>();
        String query = "SELECT * FROM PrivateMessages WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) ORDER BY timestamp ASC";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, user1.getUserId());
            stmt.setInt(2, user2.getUserId());
            stmt.setInt(3, user2.getUserId());
            stmt.setInt(4, user1.getUserId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                messages.add(new PrivateMessage(rs.getInt("sender_id"), rs.getInt("receiver_id"), rs.getString("message"), rs.getTimestamp("timestamp")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public Iterable<? extends PublicMessage> getPublicMessages() {
        Collection<PublicMessage> publicMessages = new ArrayList<>();
        String query = "SELECT * FROM PublicMessages";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                publicMessages.add(new PublicMessage(rs.getInt("sender_id"), rs.getString("message"), rs.getTimestamp("timestamp")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return publicMessages;
    }

    public User getUserById(int userId) {
        String query = "SELECT * FROM Users WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("password"), rs.getBoolean("online_status"), this);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User findUserByUsername(String username) {
        String query = "SELECT * FROM Users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("password"), rs.getBoolean("online_status"), this);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addFriendRequest(FriendRequest request) {
        String query = "INSERT INTO FriendRequests (sender_id, receiver_id, status) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, request.getSender().getUserId());
            stmt.setInt(2, request.getReceiver().getUserId());
            stmt.setString(3, "Pending");
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<FriendRequest> getFriendRequests(User user) {
        List<FriendRequest> friendRequests = new ArrayList<>();
        String query = "SELECT * FROM FriendRequests WHERE sender_id = ? AND status = 'Pending'";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, user.getUserId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println("request_id: "+rs.getInt("request_id"));
                friendRequests.add(new FriendRequest(rs.getInt("request_id"),getUserById(rs.getInt("sender_id")),getUserById(rs.getInt("receiver_id")), rs.getString("status")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friendRequests;
    }

    public List<FriendRequest> getFriendRequestsRec(User user) {
        System.out.println("正在加载好友请求Rec");
        List<FriendRequest> friendRequests = new ArrayList<>();
        String query = "SELECT * FROM FriendRequests WHERE receiver_id = ? AND status = 'Pending'";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            System.out.println("user.getUserId(): "+user.getUserId());
            stmt.setInt(1, user.getUserId());//receiver_id = user.getUserId()
            ResultSet rs = stmt.executeQuery();//执行查询
            while (rs.next()) {
                friendRequests.add(new FriendRequest(rs.getInt("request_id"),getUserById(rs.getInt("sender_id")),getUserById(rs.getInt("receiver_id")), rs.getString("status")));
            }
        } catch (SQLException e) {
            System.out.println("加载好友请求Rec失败");
            e.printStackTrace();
        }
        return friendRequests;
    }

    //获取好友列表
    public List<User> getFriends(User user) {
        List<User> friends = new ArrayList<>();
        String query = "SELECT * FROM FriendRequests WHERE (sender_id = ? OR receiver_id = ?) AND status = 'Accepted'";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            //代码意思是：查询sender_id = user.getUserId() 或 receiver_id = user.getUserId()的记录
            stmt.setInt(1, user.getUserId());
            stmt.setInt(2, user.getUserId());
            ResultSet rs = stmt.executeQuery();
            //遍历结果集
            while (rs.next()) {
                int friendId = rs.getInt("sender_id") == user.getUserId() ? rs.getInt("receiver_id") : rs.getInt("sender_id");
                friends.add(getUserById(friendId));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friends;
    }

    public void updateFriendRequestStatus(FriendRequest request, String status) {
        String query = "UPDATE FriendRequests SET status = ? WHERE request_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            //更新好友请求状态
            stmt.setString(1, status);//status: Pending, Accepted, Rejected
            stmt.setInt(2, request.getRequestId());//request_id
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int addGroupChat(String groupName, List<User> members) {
        String query = "INSERT INTO GroupChats (group_name) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS))
        {
            stmt.setString(1, groupName);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int groupId = rs.getInt(1);
                addGroupMembers(groupId, members);//添加群成员
            }
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void addGroupMembers(int groupId, List<User> members) {
        String query = "INSERT INTO GroupMembers (group_id, user_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            for (User member : members) {
                stmt.setInt(1, groupId);
                stmt.setInt(2, member.getUserId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<GroupMessage> getGroupMessages(int groupId) {
        List<GroupMessage> messages = new ArrayList<>();
        String query = "SELECT * FROM GroupMessages WHERE group_id = ? ORDER BY timestamp ASC";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                messages.add(new GroupMessage(rs.getInt("group_id"), rs.getInt("sender_id"), rs.getString("message"), rs.getTimestamp("timestamp")));
                System.out.println(messages);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public List<String> getGroupChats(User user) {
        List<String> groupChats = new ArrayList<>();
        String query = "SELECT group_name FROM GroupChats where group_id in (select group_id from GroupMembers where user_id = ?)";//查询用户所在的群聊
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, user.getUserId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                groupChats.add(rs.getString("group_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groupChats;
    }

    public int getGroupIdByName(String groupName) {
        String query = "SELECT group_id FROM GroupChats WHERE group_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, groupName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("group_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return an invalid groupId if not found
    }

    public List<User> getGroupMembers(int groupId) {
        List<User> members = new ArrayList<>();
        String query = "SELECT user_id FROM GroupMembers WHERE group_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                members.add(getUserById(rs.getInt("user_id")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    public User getUser(String username, String password) {
        String query = "SELECT * FROM Users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("password"), rs.getBoolean("online_status"), this);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateUserPassword(User user, String newPassword) {
        String query = "UPDATE Users SET password = ? WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, newPassword);
            stmt.setInt(2, user.getUserId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeFriend(User user, User friend) {
        String query = "UPDATE FriendRequests SET status = 'Rejected' WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, user.getUserId());
            stmt.setInt(2, friend.getUserId());
            stmt.setInt(3, friend.getUserId());
            stmt.setInt(4, user.getUserId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getGroupName(int groupId) {
        String query = "SELECT group_name FROM GroupChats WHERE group_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("group_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean InThisGroup(int groupId, int userId) {
        String query = "SELECT * FROM GroupMembers WHERE group_id = ? AND user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, groupId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getUserName(int i) {
        String query = "SELECT username FROM Users WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, i);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getGroupChatId(String groupName) {
        String query = "SELECT group_id FROM GroupChats WHERE group_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, groupName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("group_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean deleteGroupChat(int groupId) {
        String query1 = "DELETE FROM GroupChats WHERE group_id = ?";
        String query2 = "DELETE FROM GroupMembers WHERE group_id = ?";
        String query3 = "DELETE FROM GroupMessages WHERE group_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query3)) {
            stmt.setInt(1, groupId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try (PreparedStatement stmt = connection.prepareStatement(query2)) {
            stmt.setInt(1, groupId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try (PreparedStatement stmt = connection.prepareStatement(query1)) {
            stmt.setInt(1, groupId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public User[] getUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM Users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                users.add(new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("password"), rs.getBoolean("online_status"), this));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users.toArray(new User[0]);
    }

    public List<FriendRequest> getFriendRequestsReject(User user) {
        List<FriendRequest> friendRequests = new ArrayList<>();
        String query = "SELECT * FROM FriendRequests WHERE sender_id = ? AND status = 'Rejected'";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, user.getUserId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                friendRequests.add(new FriendRequest(rs.getInt("request_id"),getUserById(rs.getInt("sender_id")),getUserById(rs.getInt("receiver_id")), rs.getString("status")));
                //删除此条记录
                String query2 = "DELETE FROM FriendRequests WHERE sender_id = ? AND receiver_id = ?";
                try (PreparedStatement stmt2 = connection.prepareStatement(query2)) {
                    stmt2.setInt(1, rs.getInt("sender_id"));
                    stmt2.setInt(2, rs.getInt("receiver_id"));
                    stmt2.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friendRequests;
    }

    public String getPassword(User user) {
        String query = "SELECT password FROM Users WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, user.getUserId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("password");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteUser(User user) {
        //删除用户
        String query = "DELETE FROM Users WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, user.getUserId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //删除好友关系
        String query2 = "DELETE FROM FriendRequests WHERE sender_id = ? OR receiver_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query2)) {
            stmt.setInt(1, user.getUserId());
            stmt.setInt(2, user.getUserId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //删除群聊
        String query3 = "DELETE FROM GroupMembers WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query3)) {
            stmt.setInt(1, user.getUserId());
            stmt.executeUpdate();
            //如果群聊中成员<=2,则删除群聊
            String query4 = "SELECT group_id FROM GroupMembers GROUP BY group_id HAVING COUNT(user_id) <= 2";//查询群聊成员<=2的群聊
            try (Statement stmt2 = connection.createStatement();//创建Statement对象
                 ResultSet rs = stmt2.executeQuery(query4)) {//执行查询
                while (rs.next()) {//遍历结果集
                    deleteGroupChat(rs.getInt("group_id"));//删除群聊
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //删除私聊
        String query4 = "DELETE FROM PrivateMessages WHERE sender_id = ? OR receiver_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query4)) {
            stmt.setInt(1, user.getUserId());
            stmt.setInt(2, user.getUserId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //删除公聊
        String query5 = "DELETE FROM PublicMessages WHERE sender_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query5)) {
            stmt.setInt(1, user.getUserId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class PrivateMessage {
        private int senderId;
        private int receiverId;
        private String message;
        private Timestamp timestamp;

        public PrivateMessage(int senderId, int receiverId, String message, Timestamp timestamp) {
            this.senderId = senderId;
            this.receiverId = receiverId;
            this.message = message;
            this.timestamp = timestamp;
        }

        public int getSenderId() {
            return senderId;
        }

        public int getReceiverId() {
            return receiverId;
        }

        public String getMessage() {
            return message;
        }

        public Timestamp getTimestamp() {
            return timestamp;
        }


        public String getSenderName(DatabaseManager dbManager) {
            User sender = dbManager.getUserById(senderId);
            return sender != null ? sender.getUsername() : "Unknown";
        }

    }

    public static class PublicMessage {
        private int senderId;
        private String message;
        private Timestamp timestamp;

        public PublicMessage(int senderId, String message, Timestamp timestamp) {
            this.senderId = senderId;
            this.message = message;
            this.timestamp = timestamp;
        }

        public String getSenderName(DatabaseManager databaseManager) {
            User sender = databaseManager.getUserById(senderId);
            return sender != null ? sender.getUsername() : "Unknown";
        }

        public int getSenderId() {
            return senderId;
        }

        public String getMessage() {
            return message;
        }

        public Timestamp getTimestamp() {
            return timestamp;
        }
    }

    public static class GroupMessage {
        private int groupId;
        private int senderId;
        private String message;
        private Timestamp timestamp;

        public GroupMessage(int groupId, int senderId, String message, Timestamp timestamp) {
            this.groupId = groupId;
            this.senderId = senderId;
            this.message = message;
            this.timestamp = timestamp;
        }

        public int getGroupId() {
            return groupId;
        }

        public int getSenderId() {
            return senderId;
        }

        public String getMessage() {
            return message;
        }

        public Timestamp getTimestamp() {
            return timestamp;
        }
    }
}