import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class GroupChat {
    private User user;
    private List<User> members;
    private Socket socket;
    private PrintWriter out;
    private DatabaseManager dbManager;
    private int groupId;

    public GroupChat(User user, List<User> members,int groupId, Socket socket, DatabaseManager dbManager) {
        this.user = user;
        this.members = members;
        this.groupId = groupId;
        this.socket = socket;
        this.dbManager = dbManager;
        setupNetworking();
    }
    //初始化网络连接
    private void setupNetworking() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);//true表示自动flush
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        message=groupId+":"+user.getUserId()+":"+message;
        out.println(message);
        dbManager.addGroupMessage(new DatabaseManager.GroupMessage( groupId, user.getUserId(),message.split(":")[2], new Timestamp(System.currentTimeMillis())));
    }

    public String receiveMessage() throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine();//读取服务器发送的消息
    }

    public List<DatabaseManager.GroupMessage> loadChatHistory() {
        return dbManager.getGroupMessages(groupId);
    }

    public String getGroupName(int groupId) {
        return dbManager.getGroupName(groupId);
    }

    public int getGroupId() {
        return groupId;
    }

}
