
import java.io.*;
import java.net.Socket;

public class GameClient {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean connected;
    private GameClientHandler handler;
    
    public interface GameClientHandler {
        void onConnected();
        void onOpponentMove(int row, int col);
        void onDisconnected();
    }
    
    public GameClient(GameClientHandler handler) {
        this.handler = handler;
    }
    
    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            connected = true;
            
            new Thread(this::receiveMessages).start();
            
            handler.onConnected();
            System.out.println("连接到服务器成功");
            return true;
        } catch (IOException e) {
            System.err.println("连接服务器失败: " + e.getMessage());
            return false;
        }
    }
    
    private void receiveMessages() {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals("MOVE")) {
                    int row = Integer.parseInt(parts[1]);
                    int col = Integer.parseInt(parts[2]);
                    handler.onOpponentMove(row, col);
                }
            }
        } catch (IOException e) {
            System.out.println("与服务器断开连接");
        } finally {
            handler.onDisconnected();
            disconnect();
        }
    }
    
    public void sendMove(int row, int col) {
        if (connected && writer != null) {
            writer.println("MOVE," + row + "," + col);
        }
    }
    
    public void disconnect() {
        connected = false;
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
}