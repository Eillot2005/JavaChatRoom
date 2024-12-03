import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;

public class PublicChatRoom {
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private User sender;
    private DatabaseManager dbManager;

    public PublicChatRoom(Socket socket, User sender, DatabaseManager dbManager) {
        this.socket = socket;
        this.sender = sender;
        this.dbManager = dbManager;
        setupNetworking();
        initializeComponents();
        loadChatHistory();
        startMessageListener();
        //将窗口置于中间
        frame.setLocationRelativeTo(null);
    }

    private void initializeComponents() {
        frame = new JFrame("公共聊天室");
        chatArea = new JTextArea(20, 40);//设置聊天区域的大小,20行50列
        chatArea.setEditable(false);
        inputField = new JTextField(40);
        sendButton = new JButton("发送");

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        JPanel panel = new JPanel();
        panel.add(new JScrollPane(chatArea));
        panel.add(inputField);
        panel.add(sendButton);

        //在群聊右侧显示群成员及其在线状态
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(new JLabel("群成员\n"));
        //设置右侧面板的大小
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        rightPanel.removeAll();
                        rightPanel.add(new JLabel("群成员\n"));
                        for (User user : dbManager.getUsers()) {
                            String status = user.isOnline() ? "在线" : "离线";
                            JLabel userLabel = new JLabel(user.getUsername() + "(" + status + ")");
                            if (user.isOnline()) {
                                userLabel.setForeground(Color.GREEN);
                            } else {
                                userLabel.setForeground(Color.RED);
                            }
                            rightPanel.add(userLabel);
                        }
                        rightPanel.revalidate();
                        rightPanel.repaint();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        rightPanel.setPreferredSize(new Dimension(100, 400));
        for (User user : dbManager.getUsers()) {
            String status = user.isOnline() ? "在线" : "离线";
            JLabel userLabel = new JLabel(user.getUsername()+"("+status+")");
            if (user.isOnline()) {
                userLabel.setForeground(Color.GREEN);
            } else {
                userLabel.setForeground(Color.RED);
            }
            rightPanel.add(userLabel);
        }

        frame.getContentPane().add(BorderLayout.WEST, rightPanel);
        frame.getContentPane().add(BorderLayout.CENTER, panel);
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
        //当用户进入聊天室时，服务器向所有用户发送欢迎消息
        sendWelcomeMessage();
        //当用户离开聊天室时，服务器向所有用户发送离开消息
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                sendLeaveMessage();
            }
        });
    }

    //设置网络连接
    private void setupNetworking() {
        try {
            //获取socket的输入输出流
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            //如果发生异常，打印异常信息
            e.printStackTrace();
        }
    }

    //开始监听服务器发送的消息
    private void startMessageListener() {
        Thread listenerThread = new Thread(new Runnable() {
            //重写run方法
            @Override
            public void run() {
                String message;
                try {
                    //当服务器发送消息时，将消息显示在聊天区域
                    while ((message = in.readLine()) != null) {
                        chatArea.append(message + "\n" + new Timestamp(System.currentTimeMillis()) + "\n\n");
                    }
                    //如果发生异常，打印异常信息
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        //启动监听线程
        listenerThread.start();
    }

    //发送消息
    private void sendMessage() {
        //获取输入框中的消息
        String message = inputField.getText();
        message+="("+sender.getUsername()+")";
        //如果消息不为空
        if (!message.isEmpty()) {
            //将消息发送给服务器
            out.println(message);
            //将信息显示到聊天面板
            chatArea.append(sender.getUsername() + ":" + message + "\n" + new Timestamp(System.currentTimeMillis()) + "\n\n");
            //将信息存储到数据库
            dbManager.addPublicMessage(new DatabaseManager.PublicMessage(sender.getUserId(), message, new Timestamp(System.currentTimeMillis())));
            //清空输入框
            inputField.setText("");
        }
    }

    //当用户进入聊天室时，服务器向所有用户发送欢迎消息
    public void sendWelcomeMessage() {
        //将消息发送给服务器
        out.println("欢迎  "  + sender.getUsername()+ " ("+socket.getInetAddress() +") "+ " 进入聊天室！");
    }

    //当用户离开聊天室时，服务器向所有用户发送离开消息
    public void sendLeaveMessage() {
        //将消息发送给服务器
        out.println(socket.getInetAddress() + " 离开了聊天室！");
    }

    //加载聊天记录
    public void loadChatHistory() {
        //获取公共聊天记录
        for (DatabaseManager.PublicMessage message : dbManager.getPublicMessages()) {
            //将消息显示在聊天区域
            chatArea.append(message.getSenderName(dbManager) + ":" + message.getMessage() + "\n" + message.getTimestamp() + "\n\n");
        }
    }

}
