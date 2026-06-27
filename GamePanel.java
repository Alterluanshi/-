
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GamePanel extends JPanel {
    private static final int CELL_SIZE = 35;
    private static final int MARGIN = 30;
    private static final int BOARD_SIZE = ChessBoard.SIZE;
    
    private ChessBoard board;
    private MainFrame mainFrame;
    private String playerName = "玩家";
    private String status = "准备开始";
    private boolean myTurn = false;
    private boolean gameStarted = false;
    
    public GamePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.board = new ChessBoard();
        setBackground(new Color(222, 184, 135));
        setPreferredSize(new Dimension(
            MARGIN * 2 + (BOARD_SIZE - 1) * CELL_SIZE,
            MARGIN * 2 + (BOARD_SIZE - 1) * CELL_SIZE + 40
        ));
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!gameStarted || !myTurn || board.isGameOver()) return;
                
                int row = (e.getY() - MARGIN + CELL_SIZE / 2) / CELL_SIZE;
                int col = (e.getX() - MARGIN + CELL_SIZE / 2) / CELL_SIZE;
                
                if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) return;
                
                if (board.placeChess(row, col)) {
                    repaint();
                    myTurn = false;
                    
                    if (mainFrame.getClient() != null && mainFrame.getClient().isConnected()) {
                        mainFrame.getClient().sendMove(row, col);
                    }
                    
                    if (board.isGameOver()) {
                        String winner = board.getWinner();
                        JOptionPane.showMessageDialog(GamePanel.this, 
                            "游戏结束！" + winner + "获胜！", "游戏结束", JOptionPane.INFORMATION_MESSAGE);
                        reset();
                    }
                }
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        drawBoard(g2d);
        drawChesses(g2d);
        drawInfo(g2d);
    }
    
    private void drawBoard(Graphics2D g) {
        g.setColor(Color.BLACK);
        for (int i = 0; i < BOARD_SIZE; i++) {
            int x = MARGIN + i * CELL_SIZE;
            int y = MARGIN + i * CELL_SIZE;
            g.drawLine(MARGIN, y, MARGIN + (BOARD_SIZE - 1) * CELL_SIZE, y);
            g.drawLine(x, MARGIN, x, MARGIN + (BOARD_SIZE - 1) * CELL_SIZE);
        }
        
        int[][] stars = {{3, 3}, {3, 7}, {7, 3}, {7, 7}, {7, 11}, {11, 7}, {11, 11}};
        g.setColor(Color.BLACK);
        for (int[] star : stars) {
            int x = MARGIN + star[0] * CELL_SIZE;
            int y = MARGIN + star[1] * CELL_SIZE;
            g.fillOval(x - 4, y - 4, 8, 8);
        }
    }
    
    private void drawChesses(Graphics2D g) {
        int[][] boardData = board.getBoard();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (boardData[row][col] == ChessBoard.EMPTY) continue;
                
                int x = MARGIN + col * CELL_SIZE;
                int y = MARGIN + row * CELL_SIZE;
                int radius = CELL_SIZE / 2 - 3;
                
                if (boardData[row][col] == ChessBoard.BLACK) {
                    g.setColor(Color.BLACK);
                } else {
                    g.setColor(Color.WHITE);
                }
                g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
                
                if (boardData[row][col] == ChessBoard.BLACK) {
                    g.setColor(new Color(100, 100, 100));
                } else {
                    g.setColor(Color.LIGHT_GRAY);
                }
                g.drawOval(x - radius, y - radius, radius * 2, radius * 2);
            }
        }
    }
    
    private void drawInfo(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("宋体", Font.BOLD, 14));
        
        String statusText;
        if (board.isGameOver()) {
            statusText = "游戏结束 - " + board.getWinner() + "获胜";
        } else if (!gameStarted) {
            statusText = status;
        } else if (myTurn) {
            statusText = "你的回合 (" + (board.getCurrentPlayer() == ChessBoard.BLACK ? "黑棋" : "白棋") + ")";
        } else {
            statusText = "等待对手下棋...";
        }
        
        g.drawString("玩家: " + playerName, 10, 20);
        g.drawString("状态: " + statusText, 10, 40);
        g.drawString("步数: " + board.getMoveCount(), 10, 60);
    }
    
    public void placeOpponentChess(int row, int col) {
        if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
            board.placeChess(row, col);
            myTurn = true;
            repaint();
            
            if (board.isGameOver()) {
                String winner = board.getWinner();
                JOptionPane.showMessageDialog(this, 
                    "游戏结束！" + winner + "获胜！", "游戏结束", JOptionPane.INFORMATION_MESSAGE);
                reset();
            }
        }
    }
    
    public void startGame(boolean isHost) {
        board.reset();
        myTurn = isHost;
        gameStarted = true;
        repaint();
    }
    
    public void reset() {
        board.reset();
        gameStarted = false;
        myTurn = false;
        repaint();
    }
    
    public void setPlayerName(String name) { 
        this.playerName = name; 
    }
    
    public void setStatus(String status) { 
        this.status = status; 
        repaint();
    }
}