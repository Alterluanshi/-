
public class ChessBoard {
    public static final int SIZE = 15;
    public static final int EMPTY = 0;
    public static final int BLACK = 1;
    public static final int WHITE = 2;
    
    private int[][] board;
    private int currentPlayer;
    private boolean gameOver;
    private String winner;
    private int moveCount;
    
    public ChessBoard() {
        reset();
    }
    
    public boolean placeChess(int row, int col) {
        if (gameOver || row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            return false;
        }
        if (board[row][col] != EMPTY) {
            return false;
        }
        
        board[row][col] = currentPlayer;
        moveCount++;
        
        if (checkWin(row, col)) {
            gameOver = true;
            winner = currentPlayer == BLACK ? "黑棋" : "白棋";
            return true;
        }
        
        if (moveCount >= SIZE * SIZE) {
            gameOver = true;
            winner = "平局";
            return true;
        }
        
        currentPlayer = (currentPlayer == BLACK) ? WHITE : BLACK;
        return true;
    }
    
    private boolean checkWin(int row, int col) {
        int color = board[row][col];
        return checkDirection(row, col, color, 1, 0) ||
               checkDirection(row, col, color, 0, 1) ||
               checkDirection(row, col, color, 1, 1) ||
               checkDirection(row, col, color, 1, -1);
    }
    
    private boolean checkDirection(int row, int col, int color, int dx, int dy) {
        int count = 1;
        for (int i = 1; i < 5; i++) {
            int newRow = row + dx * i;
            int newCol = col + dy * i;
            if (newRow < 0 || newRow >= SIZE || newCol < 0 || newCol >= SIZE) break;
            if (board[newRow][newCol] != color) break;
            count++;
        }
        for (int i = 1; i < 5; i++) {
            int newRow = row - dx * i;
            int newCol = col - dy * i;
            if (newRow < 0 || newRow >= SIZE || newCol < 0 || newCol >= SIZE) break;
            if (board[newRow][newCol] != color) break;
            count++;
        }
        return count >= 5;
    }
    
    public void reset() {
        board = new int[SIZE][SIZE];
        currentPlayer = BLACK;
        gameOver = false;
        winner = null;
        moveCount = 0;
    }
    
    public int getChessAt(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) return EMPTY;
        return board[row][col];
    }
    
    public int getCurrentPlayer() { return currentPlayer; }
    public boolean isGameOver() { return gameOver; }
    public String getWinner() { return winner; }
    public int getMoveCount() { return moveCount; }
    public int[][] getBoard() { return board; }
}