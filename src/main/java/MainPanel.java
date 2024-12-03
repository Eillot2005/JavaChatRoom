import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.Timer;

public class MainPanel {
    private JFrame frame;
    private JLabel onlineUsersLabel;
    private JButton publicChatButton;
    private JButton privateChatButton;
    private JButton groupChatButton;
    private JButton addFriendButton;
    private JButton ChangePasswordButton;
    private JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem logoutMenuItem;
    private DatabaseManager databaseManager;
    private User user;
    private JButton refreshOnlineButton;
    private JPanel notificationPanel;
    private JPanel friendNotificationPanel;
    private JTextArea notificationArea;
    private JTextArea friendNotificationArea;
    private FriendManager friendManager;
    private DefaultListModel<User> friendListModel;
    private JList<User> friendList;

    public MainPanel(DatabaseManager databaseManager, User user) {
        this.databaseManager = databaseManager;
        this.user = user;
        this.friendManager = new FriendManager(databaseManager, user);
        this.friendListModel = new DefaultListModel<>();
        this.friendList = new JList<>(friendListModel);
        initializeComponents();
        loadFriends();
        //定时器，每隔2秒刷新一次在线用户
        AtomicInteger count = new AtomicInteger(0);
        new Timer(1000, e -> {
            count.getAndIncrement();
            if (count.get() % 5 == 0) {
                loadFriendRequests();//每5秒刷新一次好友请求

            }
            refreshOnlineUsers();//每秒刷新一次在线用户
        }).start();
    }

    private void initializeComponents() {
        frame = new JFrame("主菜单");
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 设置窗口关闭时的操作
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                logout();
                JOptionPane.showMessageDialog(frame, "您已退出登录", "通知", JOptionPane.PLAIN_MESSAGE);
            }
        });

        // 菜单栏
        menuBar = new JMenuBar();
        menu = new JMenu("菜单");
        logoutMenuItem = new JMenuItem("登出");
        logoutMenuItem.addActionListener(e -> logout());
        JMenuItem aboutAuthorMenuItem = new JMenuItem("关于作者");
        aboutAuthorMenuItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(null, "作者：刘逸潇\n学号：20233593\n班级：计算机科学与技术4班", "关于作者", JOptionPane.PLAIN_MESSAGE);
        });
        JMenuItem exitMenuItem = new JMenuItem("重新登录");
        exitMenuItem.addActionListener(e -> {
            //退出登录
            user.setOnlineStatus(false);
            databaseManager.updateUserOnlineStatus(user, false);
            frame.dispose();
            new LoginPanel(databaseManager);
        });
        menu.add(logoutMenuItem);
        menu.add(aboutAuthorMenuItem);
        menu.add(exitMenuItem);
        menuBar.add(menu);
        frame.setJMenuBar(menuBar);

        // 左侧功能按钮
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(6, 1, 10, 10)); // 设置网格布局，6行1列，按钮间距为10像素
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // 设置面板边距

        refreshOnlineButton = new JButton("注销账号");
        refreshOnlineButton.setPreferredSize(new Dimension(200, 50));
        refreshOnlineButton.addActionListener(e -> {
            String password = JOptionPane.showInputDialog(frame, "请输入密码：", "注销账号", JOptionPane.PLAIN_MESSAGE);
            if (password == null || password.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "密码不能为空", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            } else if (password.equals(user.getPasswordFromDatabase())) {
                UIManager.put("OptionPane.messageForeground", Color.RED);
                int result = JOptionPane.showConfirmDialog(frame, "确定要注销账号吗？\n您的所有信息包括您的聊天记录，\n好友关系都会被清除，\n就好像您从来没有来过这里", "警告", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    JFrame progressFrame = new JFrame();
                    JProgressBar progressBar = new JProgressBar(0, 100);
                    progressBar.setStringPainted(true);
                    progressFrame.setLayout(new FlowLayout());
                    progressFrame.getContentPane().add(progressBar);
                    progressFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    progressFrame.setSize(300, 200);
                    progressFrame.setLocationRelativeTo(null);
                    progressFrame.setVisible(true);
                    // 创建 SwingWorker 对象, 用于后台执行耗时任务
                    SwingWorker<Void, Integer> worker = new SwingWorker<>() {
                        @Override
                        protected Void doInBackground() throws Exception {// 后台执行耗时任务
                            int currentNumber = 0;
                            while (currentNumber <= 100) {
                                if(currentNumber < 30)
                                    progressBar.setString("正在加载数据");
                                else if(currentNumber < 70)
                                    progressBar.setString("正在删除用户数据");
                                else
                                    progressBar.setString("账户注销即将完成");
                                Thread.sleep(30);
                                currentNumber += 1;
                                publish(currentNumber);//publish() 方法用于触发 process() 方法
                            }
                            return null;
                        }
                        @Override
                        // 处理后台任务的中间结果，调用 publish() 方法后，会自动调用此方法
                        protected void process(List<Integer> chunks) {// 更新进度条
                            for (int number : chunks) {// 遍历 List, 取出最新的值
                                progressBar.setValue(number);
                            }
                        }
                        @Override
                        protected void done() {// 任务完成后的操作
                            databaseManager.deleteUser(user);
                            JOptionPane.showMessageDialog(progressFrame, "账号注销成功", "通知", JOptionPane.PLAIN_MESSAGE);
                            progressFrame.dispose();
                            frame.dispose();
                            new LoginPanel(databaseManager);
                        }
                    };
                    worker.execute();// 启动 SwingWorker
                } else {
                    UIManager.put("OptionPane.messageForeground", Color.BLACK);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "密码错误", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        publicChatButton = new JButton("公共聊天");
        publicChatButton.setPreferredSize(new Dimension(200, 50));
        publicChatButton.addActionListener(e -> openPublicChat());

        privateChatButton = new JButton("私聊入口");
        privateChatButton.setPreferredSize(new Dimension(200, 50));
        privateChatButton.addActionListener(e -> openPrivateChat());

        groupChatButton = new JButton("群聊入口");
        groupChatButton.setPreferredSize(new Dimension(200, 50));
        groupChatButton.addActionListener(e -> openGroupChat());

        addFriendButton = new JButton("好友管理");
        addFriendButton.setPreferredSize(new Dimension(200, 50));
        addFriendButton.addActionListener(e -> friendManager.showFriendManager());

        ChangePasswordButton = new JButton("修改密码");
        ChangePasswordButton.setPreferredSize(new Dimension(200, 50));
        ChangePasswordButton.addActionListener(e -> {
            // 弹出修改密码对话框
            String newPassword1 = JOptionPane.showInputDialog(frame, "请输入新密码：", "修改密码", JOptionPane.PLAIN_MESSAGE);
            if(newPassword1 == null || newPassword1.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "密码不能为空", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }else if(newPassword1.equals(user.getPasswordFromDatabase())){
                JOptionPane.showMessageDialog(frame, "新密码不能与旧密码相同", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }else{
                String newPassword2 = JOptionPane.showInputDialog(frame, "请再次输入新密码：", "修改密码", JOptionPane.PLAIN_MESSAGE);
                if(newPassword2.equals(newPassword1)){
                    databaseManager.updateUserPassword(user, newPassword1);
                    JOptionPane.showMessageDialog(frame, "密码修改成功", "通知", JOptionPane.PLAIN_MESSAGE);
                }else{
                    JOptionPane.showMessageDialog(frame, "两次输入的密码不一致", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 将按钮添加到面板，GridLayout 自动均匀分布按钮
        buttonPanel.add(refreshOnlineButton);
        buttonPanel.add(publicChatButton);
        buttonPanel.add(privateChatButton);
        buttonPanel.add(groupChatButton);
        buttonPanel.add(addFriendButton);
        buttonPanel.add(ChangePasswordButton);

        // 添加一个空白填充组件，让按钮整体居中并适当调整高度
        buttonPanel.add(Box.createVerticalGlue());

        // 在线用户显示
        onlineUsersLabel = new JLabel();
        onlineUsersLabel.setHorizontalAlignment(SwingConstants.CENTER);
        buttonPanel.add(onlineUsersLabel);
        onlineUsersLabel.setText("在线用户: " + databaseManager.getOnlineUsers().size());

        // 通知区域
        notificationPanel = new JPanel(new BorderLayout());
        notificationArea = new JTextArea();
        notificationArea.setEditable(false);
        notificationArea.setText("欢迎使用聊天室，" + user.getUsername() + "!\n我是刘逸潇，也是这个聊天室的作者。\n如果有任何问题，请联系我：19359989643\n祝你使用愉快！\n\n");
        notificationArea.append("聊天功能\n" +
                "公共聊天:\n"+"  点击“公共聊天”按钮可进入公共聊天界面，与所有在线用户聊天。\n" +
                "私聊:\n"+"  点击“私聊入口”按钮可选择一个好友进行私聊。\n" +
                "群聊:\n"+"  点击“群聊入口”按钮可进入群聊界面选择群组，与群组成员聊天。\n" +
                "好友管理:\n" +
                "  点击“好友管理”按钮可打开好友管理界面，查看和管理好友列表。\n" +
                "修改密码:\n" +
                "  点击“修改密码”按钮可弹出修改密码对话框，输入新密码并确认后即可修改密码。");
        //设置字体
        notificationArea.setFont(new Font("宋体", Font.PLAIN, 13));
        //自动换行
        notificationArea.setLineWrap(true);
        notificationPanel.add(new JScrollPane(notificationArea), BorderLayout.CENTER);
        notificationPanel.setBorder(BorderFactory.createTitledBorder("通知"));

        // 好友申请区域
        friendNotificationPanel = new JPanel(new BorderLayout());
        friendNotificationArea = new JTextArea();
        friendNotificationArea.setEditable(false);
        friendNotificationPanel.add(new JScrollPane(friendNotificationArea), BorderLayout.CENTER);
        friendNotificationPanel.setBorder(BorderFactory.createTitledBorder("好友申请"));
        loadFriendRequests();

        // 分割右侧面板
        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, notificationPanel, friendNotificationPanel);
        rightSplitPane.setDividerLocation(300); // 上下分割比例

        // 主界面分割
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buttonPanel, rightSplitPane);
        mainSplitPane.setDividerLocation(250); // 左右分割比例

        frame.getContentPane().add(mainSplitPane);
        frame.setVisible(true);

        //查找是否有好友拒绝我的好友请求
        List<FriendRequest> friendRequests = databaseManager.getFriendRequestsReject(user);
        if(friendRequests.size() != 0){
            for(FriendRequest request : friendRequests){
                JOptionPane.showMessageDialog(frame, request.getReceiverName()+"拒绝了你的好友请求", "通知", JOptionPane.PLAIN_MESSAGE);
            }
        }else{
            JOptionPane.showMessageDialog(frame, "暂时没有好友拒绝你的好友请求", "通知", JOptionPane.PLAIN_MESSAGE);
        }
    }
    private void loadFriendRequests() {
        List<FriendRequest> friendRequests = databaseManager.getFriendRequestsRec(user);
        if(friendRequests.size() == 0){
            friendNotificationArea.setText("没有新的好友请求");
            friendNotificationPanel.removeAll();
            friendNotificationPanel.add(friendNotificationArea, BorderLayout.CENTER);
            friendNotificationPanel.revalidate();
            friendNotificationPanel.repaint();
            return;
        }
        friendNotificationArea.setText("");
        friendNotificationPanel.removeAll();  // 清空按钮面板
        //for (FriendRequest request : friendRequests) {
        FriendRequest request = friendRequests.get(0);
            friendNotificationArea.append(request.getSender().getUsername() + " 请求添加你为好友\n");
            JButton agreeButton = new JButton("同意");
            JButton refuseButton = new JButton("拒绝");
            agreeButton.addActionListener(e -> {
                request.accept();
                databaseManager.updateFriendRequestStatus(request, "Accepted");
                JOptionPane.showMessageDialog(frame, "已同意"+request.getSenderName()+"的好友申请", "通知", JOptionPane.PLAIN_MESSAGE);
                loadFriends();
                loadFriendRequests();
            });
            refuseButton.addActionListener(e -> {
                request.reject();
                databaseManager.updateFriendRequestStatus(request, "Rejected");
                JOptionPane.showMessageDialog(frame, "已拒绝"+request.getSenderName()+"好友申请", "通知", JOptionPane.PLAIN_MESSAGE);
                loadFriendRequests();
            });
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(agreeButton);
            buttonPanel.add(refuseButton);
            friendNotificationPanel.add(buttonPanel, BorderLayout.SOUTH);
            friendNotificationPanel.add(friendNotificationArea, BorderLayout.CENTER);
        //}
        friendNotificationPanel.revalidate();
        friendNotificationPanel.repaint();
    }

    private void loadFriends() {
        List<User> friends = databaseManager.getFriends(user);
        friendListModel.clear();
        for (User friend : friends) {
            friendListModel.addElement(friend);
        }
    }

    private void refreshOnlineUsers() {
        onlineUsersLabel.setText("在线用户: " + databaseManager.getOnlineUsers().size());
    }

    private void openPublicChat() {
        try {
            Socket socket = new Socket("localhost", 8080);
            new PublicChatRoom(socket, user, databaseManager);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "无法连接到公共聊天服务器", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openPrivateChat() {
        loadFriends();
        if (friendListModel.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "你没有好友", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        User selectedFriend = (User) JOptionPane.showInputDialog(
                frame,
                "选择一个好友进行私聊:",
                "选择好友",
                JOptionPane.PLAIN_MESSAGE,
                null,
                friendListModel.toArray(),
                null
        );
        if (selectedFriend != null) {
            try {
                Socket socket = new Socket("localhost", 8080);
                //new PrivateChat(user, selectedFriend, socket, databaseManager);
                new PrivateChatUI(new PrivateChat(user, selectedFriend, socket, databaseManager),user);

            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "无法连接到私聊服务器", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openGroupChat() {
        new GroupChatUI(user, databaseManager);
    }

    private void logout() {
        user.setOnlineStatus(false);
        databaseManager.updateUserOnlineStatus(user, false);
        JOptionPane.showMessageDialog(frame, "您已退出登录", "通知", JOptionPane.PLAIN_MESSAGE);
        frame.dispose();
        System.exit(0);
    }

    public User getUser() {
        return user;
    }
}
