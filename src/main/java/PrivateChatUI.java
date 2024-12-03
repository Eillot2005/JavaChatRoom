import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public class PrivateChatUI {
    private static final Logger logger = Logger.getLogger(PrivateChatUI.class.getName());
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private PrivateChat privateChat;
    private User user;

    public PrivateChatUI(PrivateChat privateChat,User user1) {
        this.privateChat = privateChat;
        initializeComponents();
        loadChatHistory();
        startMessageListener();
        frame.setLocationRelativeTo(null);
        user=user1;
    }

    private void initializeComponents() {
        frame = new JFrame("私聊对象: " + privateChat.getReceiver().getUsername());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        inputField = new JTextField();
        sendButton = new JButton("发送");
        sendButton.addActionListener(e -> sendMessage());

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private void sendMessage() {
        String message = inputField.getText();
        if (!message.isEmpty()) {
            logger.info("Sending message: " + message);
            privateChat.sendMessage(privateChat.getReceiver()+":"+privateChat.getSender()+":"+message);
            chatArea.append("Me: " + message + "\n");
            inputField.setText("");
        }
    }

    private void startMessageListener() {
        new Thread(() -> {
            try {
                String message;
                //如果接收到消息,并且信息":"前的内容是接收者的用户名,则显示消息
                while ((message=privateChat.receiveMessage())!=null) {
                    //如果消息第一个":"前的内容是接收者的用户名,并且第二个":"前的内容是发送者的用户名,则显示消息
                    if(message.startsWith(privateChat.getSender().getUsername()+":")&&message.split(":")[1].equals(privateChat.getReceiver().getUsername())){
                        final String receivedMessage = message.split(":")[2];
                        logger.info("Received message: " + receivedMessage);
                        SwingUtilities.invokeLater(() -> {
                            // 过滤掉自己发送的消息
                            if (!receivedMessage.startsWith("Me: ")) {
                                logger.info("Appending message to chat area: " + receivedMessage);
                                chatArea.append(privateChat.getReceiver().getUsername() + ": " + receivedMessage + "\n");
                            }else {
                                logger.info("Message sent by user, not appending: " + receivedMessage);
                            }
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadChatHistory() {
        for (DatabaseManager.PrivateMessage message : privateChat.loadChatHistory()) {
            chatArea.append(message.getSenderName(privateChat.getDbManager()) + ": " + message.getMessage() + "\n" + message.getTimestamp() + "\n\n");
        }
    }
}