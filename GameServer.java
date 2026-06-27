
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class GameServer {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean running;
    private GameServerHandler handler;
    
    public interface GameServerHandler {
        void onPlayerConnected();
        void onPlayerMove(int row, int col);
        void onPlayerDisconnected();
    }
    
    public GameServer(GameServerHandler handler) {
        this.handler = handler;
    }
    
    public boolean start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("服务器启动在端口: " + port);
            
            new Thread(() -> {
                try {
                    clientSocket = serverSocket.accept();
                    System.out.println("客户端已连接");
                    reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    writer = new PrintWriter(clientSocket.getOutputStream(), true);
                    
                    handler.onPlayerConnected();
                    
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(",");
                        if (parts[0].equals("MOVE")) {
                            int row = Integer.parseInt(parts[1]);
                            int col = Integer.parseInt(parts[2]);
                            handler.onPlayerMove(row, col);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("客户端连接断开");
                    handler.onPlayerDisconnected();
                }
            }).start();
            
            return true;
        } catch (IOException e) {
            System.err.println("启动服务器失败: " + e.getMessage());
            return false;
        }
    }
    
    public void sendMove(int row, int col) {
        if (writer != null) {
            writer.println("MOVE," + row + "," + col);
        }
    }
    
    public void stop() {
        running = false;
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("服务器已停止");
    }
    
    public boolean isRunning() {
        return running && clientSocket != null && !clientSocket.isClosed();
    }
}