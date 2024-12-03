import javax.swing.*;
import java.awt.*;

public class RegisterPanel extends JPanel {
    private DatabaseManager databaseManager;
    private JTextField usernameField;
    private JTextField passwordField;
    private JTextField confirmPasswordField;
    private JButton registerButton;
    private JButton cancelButton;
    private Image backgroundImage;

    public RegisterPanel(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        // 加载背景图片
        backgroundImage = new ImageIcon(getClass().getResource("/Register.png")).getImage();
        initializeComponents();
    }

    private void initializeComponents() {
        JFrame frame = new JFrame("注册");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300); // 设置窗口大小
        frame.setResizable(false); // 禁止调整窗口大小
        frame.setLocationRelativeTo(null); // 居中显示

        // 设置主布局为 BorderLayout
        setLayout(new BorderLayout());
        setOpaque(false); // 背景透明

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false); // 确保背景图片可见

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); // 减少组件间距
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 设置字体
        Font labelFont = new Font("微软雅黑", Font.PLAIN, 14); // 缩小标签字体
        Font fieldFont = new Font("微软雅黑", Font.PLAIN, 14); // 缩小输入框字体

        // 用户名
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel usernameLabel = new JLabel("用户名:");
        usernameLabel.setFont(labelFont);
        contentPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        usernameField = new JTextField(15);
        usernameField.setFont(fieldFont);
        usernameField.setPreferredSize(new Dimension(200, 25)); // 缩小输入框大小
        contentPanel.add(usernameField, gbc);

        // 密码
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setFont(labelFont);
        contentPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        passwordField = new JPasswordField(15);
        passwordField.setFont(fieldFont);
        passwordField.setPreferredSize(new Dimension(200, 25)); // 缩小输入框大小
        contentPanel.add(passwordField, gbc);

        // 确认密码
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel confirmPasswordLabel = new JLabel("确认密码:");
        confirmPasswordLabel.setFont(labelFont);
        contentPanel.add(confirmPasswordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        confirmPasswordField = new JPasswordField(15);
        confirmPasswordField.setFont(fieldFont);
        confirmPasswordField.setPreferredSize(new Dimension(200, 25)); // 缩小输入框大小
        contentPanel.add(confirmPasswordField, gbc);

        // 按钮面板
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2; // 跨两列
        gbc.fill = GridBagConstraints.NONE;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        registerButton = new JButton("注册");
        registerButton.setPreferredSize(new Dimension(100, 30)); // 缩小按钮大小
        registerButton.setFont(new Font("微软雅黑", Font.PLAIN, 12)); // 缩小按钮字体

        cancelButton = new JButton("取消");
        cancelButton.setPreferredSize(new Dimension(100, 30)); // 缩小按钮大小
        cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12)); // 缩小按钮字体

        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);

        contentPanel.add(buttonPanel, gbc);

        // 添加到主面板
        add(contentPanel, BorderLayout.CENTER);

        // 按钮事件绑定
        registerButton.addActionListener(e -> register());
        cancelButton.addActionListener(e -> {
            LoginPanel loginPanel = new LoginPanel(databaseManager);
            SwingUtilities.getWindowAncestor(this).dispose();
        });

        // 设置内容面板
        frame.setContentPane(this);

        frame.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {//在上面什么方法调用了这个方法？是在initializeComponents()方法中，因为在这个方法中调用了setBackgroundImage()方法，而setBackgroundImage()方法中调用了repaint()方法，repaint()方法又调用了paintComponent()方法
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this); // 绘制背景图片
        }
    }

    private void register() {
        //刷新界面读取输入框中的内容
        String username = usernameField.getText();//获取用户名
        String password = passwordField.getText();//获取密码
        String confirmPassword = confirmPasswordField.getText();//获取确认密码
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名或密码不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        } else if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "两次输入的密码不一致", "错误", JOptionPane.ERROR_MESSAGE);
            //重构界面
            passwordField.setText("");
            confirmPasswordField.setText("");
            return;
        } else if (databaseManager.getUser(username) != null) {
            JOptionPane.showMessageDialog(this, "用户名已存在", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        else {
            User user = new User(0, username, password, false, databaseManager);
            databaseManager.addUser(user);
            JOptionPane.showMessageDialog(this, "注册成功", "成功", JOptionPane.PLAIN_MESSAGE);
        }

        new LoginPanel(databaseManager);
        SwingUtilities.getWindowAncestor(this).dispose();
    }
}


