import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Logger;

public class PrivateChat {
    private static final Logger logger = Logger.getLogger(PrivateChat.class.getName());
    private User sender;
    private User receiver;
    private static Socket socket;
    private DatabaseManager dbManager;
    private PrintWriter out;

    public PrivateChat(User sender, User receiver, Socket socket, DatabaseManager dbManager) {
        this.sender = sender;
        this.receiver = receiver;
        this.socket = socket;
        try {
            this.out = new PrintWriter(socket.getOutputStream(), true);//创建一个PrintWriter对象，用于向服务器发送消息
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.dbManager = dbManager;
    }

    public void sendMessage(String message) {
        logger.info("Sending message to server: " + message.replaceFirst(".*:", "").replaceFirst(".*:", ""));
        //将message的":"前的内容替换为空字符串，然后发送给服务器
        out.println(message);
        //将message第二个":"前的内容替换为空字符串，然后发送给服务器
        message=message.replaceFirst(".*:", "").replaceFirst(".*:", "");
        dbManager.addPrivateMessage(new DatabaseManager.PrivateMessage(sender.getUserId(), receiver.getUserId(), message, new Timestamp(System.currentTimeMillis())));
    }

    //用于接收服务器发送的消息
    public String receiveMessage() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        return in.readLine();
    }

    public User getSender() {
        return sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public List<DatabaseManager.PrivateMessage> loadChatHistory() {
        return dbManager.getPrivateMessages(sender, receiver);
    }

    public DatabaseManager getDbManager() {
        return dbManager;
    }

}