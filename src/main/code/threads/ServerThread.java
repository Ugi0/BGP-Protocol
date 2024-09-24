package main.code.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static main.Main.*;

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
            printDebug(("server thread timed out"));
        }
    }

    public void run() {
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printWriter = new PrintWriter(socket.getOutputStream());

        } catch(IOException e){
            printDebug("IO error in server thread");
        }

        try {
            line= bufferedReader.readLine();
            while (line.compareTo("QUIT") != 0){
                printWriter.println(line);
                printWriter.flush();
                printDebug("Response to Client  :  "+line);
                printDebug("client port: " + socket.getPort());
                line= bufferedReader.readLine();
            }   
        } catch (IOException e) {

            line = getName(); //reused String line for getting thread name
            printDebug("IO Error/ Client "+line+" terminated abruptly");
        } catch(NullPointerException e){
            line = getName(); //reused String line for getting thread name
            printDebug("Client "+line+" Closed");
        }


        finally {    
            try {
                printDebug("Connection Closing..");
                if (bufferedReader != null){
                    bufferedReader.close(); 
                    printDebug(" Socket Input Stream Closed");
                }

                if (printWriter != null){
                    printWriter.close();
                    printDebug("Socket Out Closed");
                }
                if (socket!=null){
                    socket.close();
                    printDebug("Socket Closed");
                }

                }
            catch (IOException ie) {
                printDebug("Socket Close Error");
            }
        }//end finally
    }
}