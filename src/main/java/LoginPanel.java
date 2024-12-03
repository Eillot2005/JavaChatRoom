import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private DatabaseManager databaseManager;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private Image backgroundImage;

    public LoginPanel(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        // 加载背景图片
        backgroundImage = new ImageIcon(getClass().getResource("/Login.png")).getImage();
        initializeComponents();
    }

    private void initializeComponents() {
        JFrame frame = new JFrame("登录");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null); // 居中显示
        frame.setResizable(false); // 禁止调整窗口大小

        // 设置主布局为 BoxLayout 垂直排列
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false); // 背景透明

        // 顶部留白
        add(Box.createVerticalStrut(50));

        // 创建一个居中面板
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();//创建一个网格限制对象
        gbc.insets = new Insets(10, 10, 10, 10); //设置组件之间的间距
        gbc.fill = GridBagConstraints.HORIZONTAL;//设置组件如何填充空白区域

        // 用户名
        gbc.gridx = 0;//设置组件所在的行
        gbc.gridy = 0;//设置组件所在的列
        JLabel usernameLabel = new JLabel("用户名:");
        formPanel.add(usernameLabel, gbc);
        //字体变大
        usernameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        gbc.gridx = 1;//设置组件所在的行
        gbc.gridy = 0;//设置组件所在的列
        usernameField = new JTextField(15); // 设置输入框宽度
        usernameField.setPreferredSize(new Dimension(200, 30)); // 设置输入框大小
        formPanel.add(usernameField, gbc);

        // 密码
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel passwordLabel = new JLabel("密码:");
        formPanel.add(passwordLabel, gbc);
        //字体变大
        passwordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        gbc.gridx = 1;
        gbc.gridy = 1;
        passwordField = new JPasswordField(15); // 设置输入框宽度
        passwordField.setPreferredSize(new Dimension(200, 30)); // 设置输入框大小
        formPanel.add(passwordField, gbc);

        // 登录按钮
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2; // 跨两列
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));//设置按钮面板,流式布局
        buttonPanel.setOpaque(false);

        loginButton = new JButton("登录");
        loginButton.setPreferredSize(new Dimension(80, 30)); // 设置按钮大小
        //改变按钮颜色
        loginButton.setBackground(new Color(200, 220, 200));
        registerButton = new JButton("注册");
        registerButton.setPreferredSize(new Dimension(80, 30)); // 设置按钮大小
        //改变按钮颜色
        registerButton.setBackground(new Color(200, 220, 200));

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        formPanel.add(buttonPanel, gbc);
        add(formPanel);

        // 底部留白
        add(Box.createVerticalStrut(50));//创建一个不可见的填充组件，用于填充垂直空间

        // 设置按钮的监听
        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> register());

        // 设置内容面板为当前面板
        frame.setContentPane(this);
        frame.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {//绘制背景图片，覆盖父类的方法，在上面什么方法调用了这个方法？是在initializeComponents()方法中，因为在这个方法中调用了setBackgroundImage()方法，而setBackgroundImage()方法中调用了repaint()方法，repaint()方法又调用了paintComponent()方法
        super.paintComponent(g);
        if (backgroundImage != null) {
            // 绘制背景图片
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        if(username.equals("")||password.equals("")){
            JOptionPane.showMessageDialog(this, "用户名或密码不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        User user = databaseManager.getUser(username, password);
        if (user != null&&user.isOnline()==false) {
            databaseManager.updateUserOnlineStatus(user, true);
            MainPanel mainPanel = new MainPanel(databaseManager, user);//登录成功后进入主界面
            // 关闭登录窗口
            SwingUtilities.getWindowAncestor(this).dispose();
            JOptionPane.showMessageDialog(this, "登录成功", "成功", JOptionPane.PLAIN_MESSAGE);
        } else if(user==null){
            JOptionPane.showMessageDialog(this, "用户名或密码错误", "错误", JOptionPane.ERROR_MESSAGE);
        } else if(user.isOnline()==true){
            JOptionPane.showMessageDialog(this, "用户已登录", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void register() {
        // 关闭登录窗口
        SwingUtilities.getWindowAncestor(this).dispose();
        RegisterPanel registerPanel = new RegisterPanel(databaseManager);
    }
}

