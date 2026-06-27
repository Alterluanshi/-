
import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;

public class MainFrame extends JFrame {
    private GamePanel gamePanel;
    private GameServer server;
    private GameClient client;
    private boolean isHost = false;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    
    public MainFrame() {
        setTitle("五子棋 - 局域网联机版");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 700);
        setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        mainPanel.add(createMenuPanel(), "menu");
        mainPanel.add(createGamePanel(), "game");
        
        add(mainPanel);
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cleanup();
            }
        });
    }
    
    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel titleLabel = new JLabel("五子棋");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 48));
        titleLabel.setForeground(new Color(50, 50, 150));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        panel.add(titleLabel, gbc);
        
        JButton hostBtn = createMenuButton("创建对局", new Color(70, 130, 180));
        hostBtn.addActionListener(e -> startHost());
        
        JButton joinBtn = createMenuButton("加入联机", new Color(60, 179, 113));
        joinBtn.addActionListener(e -> showJoinDialog());
        
        JButton exitBtn = createMenuButton("退出游戏", new Color(220, 20, 60));
        exitBtn.addActionListener(e -> System.exit(0));
        
        gbc.gridy = 1;
        panel.add(hostBtn, gbc);
        gbc.gridy = 2;
        panel.add(joinBtn, gbc);
        gbc.gridy = 3;
        panel.add(exitBtn, gbc);
        
        return panel;
    }
    
    private JButton createMenuButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.BOLD, 20));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(200, 60));
        return button;
    }
    
    private JPanel createGamePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        gamePanel = new GamePanel(this);
        panel.add(gamePanel, BorderLayout.CENTER);
        
        JButton backBtn = new JButton("返回主菜单");
        backBtn.addActionListener(e -> backToMenu());
        panel.add(backBtn, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void startHost() {
        isHost = true;
        int port = 8888;
        
        server = new GameServer(new GameServer.GameServerHandler() {
            @Override
            public void onPlayerConnected() {
                SwingUtilities.invokeLater(() -> {
                    gamePanel.setStatus("对手已连接，游戏开始！");
                    gamePanel.startGame(true);
                });
            }
            
            @Override
            public void onPlayerMove(int row, int col) {
                SwingUtilities.invokeLater(() -> {
                    gamePanel.placeOpponentChess(row, col);
                });
            }
            
            @Override
            public void onPlayerDisconnected() {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(MainFrame.this, 
                        "对手已断开连接", "提示", JOptionPane.WARNING_MESSAGE);
                    backToMenu();
                });
            }
        });
        
        if (server.start(port)) {
            // 连接本地服务器
            client = new GameClient(new GameClient.GameClientHandler() {
                @Override
                public void onConnected() {
                    SwingUtilities.invokeLater(() -> {
                        gamePanel.setStatus("已连接到服务器");
                    });
                }
                
                @Override
                public void onOpponentMove(int row, int col) {
                    SwingUtilities.invokeLater(() -> {
                        gamePanel.placeOpponentChess(row, col);
                    });
                }
                
                @Override
                public void onDisconnected() {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(MainFrame.this, 
                            "与服务器断开连接", "提示", JOptionPane.WARNING_MESSAGE);
                        backToMenu();
                    });
                }
            });
            
            if (client.connect("127.0.0.1", port)) {
                try {
                    String ip = InetAddress.getLocalHost().getHostAddress();
                    JOptionPane.showMessageDialog(this, 
                        "服务器已启动！\n请告知好友您的IP地址: " + ip + "\n端口: " + port,
                        "创建成功", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                cardLayout.show(mainPanel, "game");
                gamePanel.setPlayerName("主机");
                gamePanel.setStatus("等待对手连接...");
            }
        }
    }
    
    private void showJoinDialog() {
        ConnectDialog dialog = new ConnectDialog(this, (host, port) -> {
            client = new GameClient(new GameClient.GameClientHandler() {
                @Override
                public void onConnected() {
                    SwingUtilities.invokeLater(() -> {
                        gamePanel.setStatus("已连接到服务器，游戏开始！");
                        gamePanel.startGame(false);
                    });
                }
                
                @Override
                public void onOpponentMove(int row, int col) {
                    SwingUtilities.invokeLater(() -> {
                        gamePanel.placeOpponentChess(row, col);
                    });
                }
                
                @Override
                public void onDisconnected() {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(MainFrame.this, 
                            "与服务器断开连接", "提示", JOptionPane.WARNING_MESSAGE);
                        backToMenu();
                    });
                }
            });
            
            if (client.connect(host, port)) {
                cardLayout.show(mainPanel, "game");
                gamePanel.setPlayerName("客户端");
                gamePanel.setStatus("连接成功，等待对手下棋...");
            }
        });
        dialog.setVisible(true);
    }
    
    private void backToMenu() {
        cleanup();
        cardLayout.show(mainPanel, "menu");
    }
    
    private void cleanup() {
        if (server != null) server.stop();
        if (client != null) client.disconnect();
        if (gamePanel != null) gamePanel.reset();
    }
    
    public GameClient getClient() { return client; }
    public boolean isHost() { return isHost; }
}