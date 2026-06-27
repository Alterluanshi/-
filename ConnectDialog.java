
import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;

public class ConnectDialog extends JDialog {
    private JTextField hostField;
    private JTextField portField;
    private ConnectCallback callback;
    private boolean confirmed = false;
    
    public interface ConnectCallback {
        void onConnect(String host, int port);
    }
    
    public ConnectDialog(Frame owner, ConnectCallback callback) {
        super(owner, "加入联机", true);
        this.callback = callback;
        setSize(350, 200);
        setLocationRelativeTo(owner);
        initUI();
    }
    
    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridy = 0;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("主机地址:"), gbc);
        gbc.gridx = 1;
        hostField = new JTextField(getLocalIP(), 15);
        mainPanel.add(hostField, gbc);
        
        gbc.gridy = 1;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("端口:"), gbc);
        gbc.gridx = 1;
        portField = new JTextField("8888", 15);
        mainPanel.add(portField, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton connectBtn = new JButton("连接");
        JButton cancelBtn = new JButton("取消");
        
        connectBtn.setBackground(new Color(60, 179, 113));
        connectBtn.setForeground(Color.WHITE);
        connectBtn.setFocusPainted(false);
        connectBtn.addActionListener(e -> connect());
        cancelBtn.addActionListener(e -> dispose());
        
        buttonPanel.add(connectBtn);
        buttonPanel.add(cancelBtn);
        add(buttonPanel, BorderLayout.SOUTH);
        
        getRootPane().setDefaultButton(connectBtn);
    }
    
    private void connect() {
        String host = hostField.getText().trim();
        String portStr = portField.getText().trim();
        
        if (host.isEmpty() || portStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写所有字段", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            int port = Integer.parseInt(portStr);
            if (port < 1 || port > 65535) {
                JOptionPane.showMessageDialog(this, "端口号必须在1-65535之间", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            confirmed = true;
            dispose();
            callback.onConnect(host, port);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "端口号格式错误", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private String getLocalIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
    
    @Override
    public void dispose() {
        super.dispose();
    }
}