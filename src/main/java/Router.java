// echo server
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import threads.ServerThread;

public class Router extends Thread {
    private int port;
    private Integer[] connectionPorts;
    private Socket[] connections;

    Socket socket = null;
    ServerSocket serverSocket = null;

    public Router(int portNum) {
        port = portNum;
    }

    public void run() {
        System.out.println("Server Listening......");
        try {
            serverSocket = new ServerSocket(port); // can also use static final PORT_NUM , when defined

        } catch(IOException e) {
            e.printStackTrace();
            System.out.println("Server error");
            return;
        }

        int i = 0;
        for (Integer port : connectionPorts) {
            //TODO Connect to another router, with port given
            i++;
        }

        while(true) {
            try {
                socket = serverSocket.accept();
                System.out.println("connection Established");
    
                ServerThread st = new ServerThread(socket);
                st.start();

            } catch(Exception e) {
                e.printStackTrace();
                System.out.println("Connection Error");
            }
        }

    }

    public void setConnections(Integer[] ports) {
        connectionPorts = ports;
        connections = new Socket[ports.length];
    }

    @Override
    public void interrupt() {
        try {
            socket.close();
        } catch (IOException ignored) {}
        try {
            serverSocket.close();
        } catch (IOException ignored) {}
        super.interrupt();
    }

}