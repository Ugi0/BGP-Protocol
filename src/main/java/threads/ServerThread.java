package threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerThread extends Thread {  
    String line = null;
    BufferedReader bufferedReader = null;
    PrintWriter printWriter = null;
    Socket socket=null;

    public ServerThread(Socket s) {
        socket = s;
        try {
            s.setSoTimeout(60000); //milliseconds, timeouts after 1min
        }catch (IOException e){
            System.out.println(("server thread timed out"));
        }
    }

    public void run() {
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printWriter = new PrintWriter(socket.getOutputStream());

        } catch(IOException e){
            System.out.println("IO error in server thread");
        }

        try {
            line= bufferedReader.readLine();
            while( line.compareTo("QUIT") != 0){
                printWriter.println(line);
                printWriter.flush();
                System.out.println("Response to Client  :  "+line);
                System.out.println("client port: " + socket.getPort());
                line= bufferedReader.readLine();
            }   
        } catch (IOException e) {

            line=this.getName(); //reused String line for getting thread name
            System.out.println("IO Error/ Client "+line+" terminated abruptly");
        } catch(NullPointerException e){
            line=this.getName(); //reused String line for getting thread name
            System.out.println("Client "+line+" Closed");
        }


        finally {    
            try {
                System.out.println("Connection Closing..");
                if (bufferedReader != null){
                    bufferedReader.close(); 
                    System.out.println(" Socket Input Stream Closed");
                }

                if(printWriter != null){
                    printWriter.close();
                    System.out.println("Socket Out Closed");
                }
                if (socket!=null){
                    socket.close();
                    System.out.println("Socket Closed");
                }

                }
            catch (IOException ie) {
                System.out.println("Socket Close Error");
            }
        }//end finally
    }
}