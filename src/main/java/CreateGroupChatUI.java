import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class CreateGroupChatUI {
    private JFrame frame;
    private JList<User> friendList;
    private DefaultListModel<User> friendListModel;
    private DatabaseManager dbManager;
    private User user;
    private String groupName;

    public CreateGroupChatUI(User user, DatabaseManager dbManager, String groupName) {
        this.user = user;
        this.dbManager = dbManager;
        this.groupName = groupName;
        initializeComponents();
        frame.setLocationRelativeTo(null);
    }

    private void initializeComponents() {
        frame = new JFrame("创建群聊");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        friendListModel = new DefaultListModel<>();
        loadFriends();
        friendList = new JList<>(friendListModel);
        //允许选择多个好友
        friendList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JButton createButton = new JButton("创建");
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createGroupChat();
            }
        });

        frame.add(new JScrollPane(friendList), BorderLayout.CENTER);
        frame.add(createButton, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private void loadFriends() {
        List<User> friends = dbManager.getFriends(user);
        for (User friend : friends) {
            friendListModel.addElement(friend);
        }
    }

    private void createGroupChat() {
        List<User> selectedFriends = friendList.getSelectedValuesList();
        if (selectedFriends.size() < 2) {
            JOptionPane.showMessageDialog(frame, "请选择至少两名好友创建群聊", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if(groupName.isEmpty()){
            JOptionPane.showMessageDialog(frame, "请输入群聊名称", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if(dbManager.getGroupChatId(groupName) != -1){
            JOptionPane.showMessageDialog(frame, "群聊名称已存在,请重新取一个吧", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            //将selectedFriends的值复制到一个新的List中
            List<User> allMembers = new ArrayList<>(selectedFriends);
            allMembers.add(user);
            int groupId = dbManager.addGroupChat(groupName, allMembers);//创建群聊,返回群聊ID
            Socket socket = new Socket("localhost", 8080); // Connect to the server
            GroupChat groupChat = new GroupChat(user, selectedFriends, groupId, socket, dbManager);
            new GroupChatWindow(groupChat, dbManager, user);
            frame.dispose();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "无法连接到服务器", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}