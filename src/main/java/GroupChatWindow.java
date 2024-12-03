import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.Socket;
import java.sql.Timestamp;

public class GroupChatWindow {
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private GroupChat groupChat;
    private DatabaseManager dbManager;
    private User user;

    public GroupChatWindow(GroupChat groupChat, DatabaseManager dbManager, User user) {
        this.groupChat = groupChat;
        this.dbManager = dbManager;
        initializeComponents();
        loadChatHistory();
        startMessageListener();
        this.user = user;
    }

    private void initializeComponents() {
        frame = new JFrame("群聊 - " + groupChat.getGroupName(this.groupChat.getGroupId()));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);//添加滚动条

        inputField = new JTextField();
        sendButton = new JButton("发送");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        //在群聊右侧显示群成员及其在线状态
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(100, 400));
        rightPanel.setBorder(BorderFactory.createTitledBorder("群成员\n"));
        for (User user : dbManager.getGroupMembers(groupChat.getGroupId())) {
            String status = user.isOnline() ? "在线" : "离线";
            JLabel label = new JLabel(user.getUsername()+"("+status+")");
            label.setForeground(user.isOnline() ? Color.GREEN : Color.RED);//在线为绿色,不在线为红色
            rightPanel.add(label);
        }

        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.add(rightPanel, BorderLayout.EAST);
        frame.setVisible(true);
        //显示在屏幕中央
        frame.setLocationRelativeTo(null);
    }

    private void sendMessage() {
        String message = inputField.getText();
        if (!message.isEmpty()) {
            groupChat.sendMessage(message);
            chatArea.append("我: " + message + "\n");
            inputField.setText("");
        }
    }

    private void loadChatHistory() {
        //通过GroupId获取群聊的历史消息
        for (DatabaseManager.GroupMessage message : groupChat.loadChatHistory()) {
            chatArea.append(dbManager.getUserById(message.getSenderId()).getUsername() + ": " + message.getMessage() + "\n");
            chatArea.append(new Timestamp(message.getTimestamp().getTime()).toString() + "\n\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());//将滚动条自动滚动到最下面
        }
    }

    private void startMessageListener() {
        new Thread(() -> {
            try {
                String message;
                while ((message = groupChat.receiveMessage()) != null) {
                    //如果本用户的Id在message第一个":"前对用GroupId的群组中,则显示消息
                    int groupId = Integer.parseInt(message.split(":")[0]);
                    if(dbManager.InThisGroup(groupId, user.getUserId()))
                    {
                        chatArea.append(dbManager.getUserName(Integer.parseInt(message.split(":")[1])) + ": ");
                        chatArea.append(message.split(":")[2] + "\n");
                        //将消息显示在最下面,即自动滚动到最下面,显示最新消息
                        SwingUtilities.invokeLater(() -> {
                            chatArea.setCaretPosition(chatArea.getDocument().getLength());//将滚动条自动滚动到最下面
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
