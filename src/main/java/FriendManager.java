import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class FriendManager {
    private DatabaseManager databaseManager;
    private User user;
    private JFrame frame;
    private List<User> friendList;
    private JList<User> JfriendList;
    private DefaultListModel<User> friendListModel;
    private JTextField searchField;
    private JButton searchButton;
    private JButton addButton;
    private JButton deleteButton;
    private JLabel statusLabel;

    public FriendManager(DatabaseManager databaseManager, User user) {
        this.databaseManager = databaseManager;
        this.user = user;
        initializeComponents();
    }

    private void initializeComponents() {
        frame = new JFrame("好友管理");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(500, 300);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        friendListModel = new DefaultListModel<>();
        JfriendList = new JList<>(friendListModel);
        JfriendList.setCellRenderer(new FriendListCellRenderer());
        loadFriends();

        searchField = new JTextField(10);
        searchButton = new JButton("搜索");
        addButton = new JButton("添加好友");
        statusLabel = new JLabel();
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);//含义是将标签中的文本居中显示
        statusLabel.setPreferredSize(new Dimension(statusLabel.getWidth(), 30));

        deleteButton = new JButton("删除好友");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                User friend = JfriendList.getSelectedValue();
                if (friend != null) {
                    int result = JOptionPane.showConfirmDialog(frame, "确定要删除好友 " + friend.getUsername() + " 吗?", "删除好友", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        databaseManager.removeFriend(user, friend);
                        loadFriends();
                    }
                }
            }
        });

        searchButton.addActionListener(e -> searchFriends());
        addButton.addActionListener(e -> addFriend());

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("搜索用户:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(addButton);
        topPanel.add(deleteButton);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(JfriendList), BorderLayout.CENTER);
        frame.add(statusLabel, BorderLayout.SOUTH);

        frame.setVisible(false);
    }

    public void showFriendManager() {
        loadFriends();
        frame.setVisible(true);
    }

    private void loadFriends() {
        List<User> friends = databaseManager.getFriends(user);//获取好友列表
        friendListModel.clear();//清空列表
        for (User friend : friends) {
            friendListModel.addElement(friend);//将好友添加到列表中
        }
        friendList = databaseManager.getFriends(user);
        for(User friend: friendList){
            System.out.println(friend.getUsername());
        }
    }

    private void searchFriends() {
        String username = searchField.getText();
        if (username.isEmpty()) {
            statusLabel.setText("请输入用户名进行搜索");
            return;
        }
        User friend = databaseManager.findUserByUsername(username);
        if (user.getUsername().equals(username)) {
            JOptionPane.showMessageDialog(frame, "不能添加自己为好友", "Error", JOptionPane.ERROR_MESSAGE);
        }else if (user.isFriend(friend,friendList)) {
            statusLabel.setText("他已经是你的朋友");
        } else if (user.hasSentFriendRequestTo(friend)) {
            statusLabel.setText("已经发送了好友请求");
        } else if (friend != null) {
            statusLabel.setText("找到用户: " + friend.getUsername()+ (friend.isOnline() ? " (在线)" : " (离线)"));
            addButton.setEnabled(true);
        } else {
            statusLabel.setText("未找到用户");
            addButton.setEnabled(false);
        }
    }

    private void addFriend() {
        String username = searchField.getText();
        User friend = databaseManager.findUserByUsername(username);
        if (friend != null && !user.isFriend(friend,friendList) && !user.hasSentFriendRequestTo(friend)) {
            sendFriendRequest(friend);
            statusLabel.setText("好友请求已发送");
        } else if (user.isFriend(friend,friendList)) {
            JOptionPane.showMessageDialog(frame, "他已经是你的朋友", "Error", JOptionPane.ERROR_MESSAGE);
        }
        else {
            JOptionPane.showMessageDialog(frame, "已经发送了好有申请请求", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void sendFriendRequest(User friend) {
        if (!user.isFriend(friend,friendList) && !user.hasSentFriendRequestTo(friend)) {
            int requestId = generateUniqueRequestId();
            FriendRequest request = new FriendRequest(requestId,user, friend, "请求添加你为好友");
            databaseManager.addFriendRequest(request); // Ensure this method is called
            JOptionPane.showMessageDialog(null, "好友申请已发送", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "他已经是你的朋友或已经发送了请求", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class FriendListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            User friend = (User) value;
            label.setText(friend.getUsername() + (friend.isOnline() ? " (在线)" : " (离线)"));
            return label;
        }
    }

    private int generateUniqueRequestId() {
        //确保生成的请求ID是唯一的
        return (int) (System.currentTimeMillis() & 0xfffffff);//产生一个唯一的请求ID
    }
}
