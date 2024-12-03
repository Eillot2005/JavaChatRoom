import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class GroupChatUI extends JPanel {
    private JFrame frame;
    private JList<String> groupList;
    private DefaultListModel<String> groupListModel;
    private DatabaseManager dbManager;
    private User user;
    private Image backgroundImage;

    public GroupChatUI(User user, DatabaseManager dbManager) {
        this.user = user;
        this.dbManager = dbManager;
        this.backgroundImage= new ImageIcon(getClass().getResource("/img.png")).getImage();
        initializeComponents();
        frame.setLocationRelativeTo(null);
    }

    private void initializeComponents() {
        frame = new JFrame("群聊");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        groupListModel = new DefaultListModel<>();
        loadGroupChats();
        groupList = new JList<>(groupListModel);
        groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton joinGroupButton = new JButton("加入群聊");
        joinGroupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                joinGroupChat();
            }
        });

        JButton createGroupButton = new JButton("创建群聊");
        createGroupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                createGroupChat();
            }
        });

        JButton deleteGroupButton = new JButton("删除群聊");
        deleteGroupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = JOptionPane.showConfirmDialog(frame, "确定要删除群聊吗?", "删除群聊", JOptionPane.YES_NO_OPTION);
                if(result == JOptionPane.YES_OPTION) {
                    deleteGroupChat();
                }else{
                    JOptionPane.showMessageDialog(frame, "已取消删除", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(joinGroupButton);
        buttonPanel.add(createGroupButton);
        buttonPanel.add(deleteGroupButton);

        //将群聊列表添加到滚动面板，再将滚动面板添加到主面板，将滚动面板设置为透明
        JScrollPane scrollPane = new JScrollPane(groupList);
        scrollPane.setOpaque(false);//设置为透明
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private void deleteGroupChat() {
        String selectedGroup = groupList.getSelectedValue();
        if (selectedGroup != null) {
            int groupId = dbManager.getGroupIdByName(selectedGroup);
            if (dbManager.deleteGroupChat(groupId)) {
                groupListModel.remove(groupList.getSelectedIndex());
            } else {
                JOptionPane.showMessageDialog(frame, "无法删除群聊", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "请选择一个群聊", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadGroupChats() {
        List<String> groups = dbManager.getGroupChats(user);
        for (String group : groups) {
            groupListModel.addElement(group);
        }
    }

    private void joinGroupChat() {
        String selectedGroup = groupList.getSelectedValue();//获取选中的群聊
        if (selectedGroup != null) {
            int groupId = dbManager.getGroupIdByName(selectedGroup);
            try {
                Socket socket = new Socket("localhost", 8080); // Connect to the server
                GroupChat groupChat = new GroupChat(user, dbManager.getGroupMembers(groupId), groupId, socket, dbManager);
                new GroupChatWindow(groupChat, dbManager, user);
                frame.dispose();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "无法连接到服务器", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "请选择一个群聊", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createGroupChat() {
        List<User> friends = dbManager.getFriends(user);
        if (friends.size() < 2) {
            JOptionPane.showMessageDialog(frame, "你需要至少两个好友来创建群聊", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String groupName = JOptionPane.showInputDialog(frame, "输入群聊名称:");
        if (groupName != null && !groupName.trim().isEmpty()) {
            new CreateGroupChatUI(user, dbManager, groupName);
        } else {
            JOptionPane.showMessageDialog(frame, "群聊名称不能空", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
