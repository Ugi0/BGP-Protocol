// A simple Client Server Protocol .. Client for Echo Server
package main.code;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static main.Main.*;

public class Client {

public static void main(String args[]) throws IOException{


    InetAddress address=InetAddress.getLocalHost();
    Socket s1=null;
    String line=null;
    BufferedReader br=null;
    BufferedReader is=null;
    PrintWriter os=null;

    try {
        s1=new Socket(address, 4445); // You can use static final constant PORT_NUM
        s1.setSoTimeout(1000);
        br= new BufferedReader(new InputStreamReader(System.in));
        is=new BufferedReader(new InputStreamReader(s1.getInputStream()));
        os= new PrintWriter(s1.getOutputStream());
    }
    catch (IOException e){
        e.printStackTrace();
        printDebug("IO Exception");
    }

    printDebug("Client Address : "+address);
    printDebug("Enter Data to echo Server ( Enter QUIT to end):");

    String response=null;
    try{
        line=br.readLine(); 
        while(true){
                os.println(line);
                os.flush();
                try{
                response=is.readLine();
                if (response != null){
                    printDebug("Server Response : "+response);
                }
            } catch (SocketTimeoutException e) {
                printDebug("no message from server Timeout");
            }
                line=br.readLine();
                if (line.compareTo("QUIT")==0){
                    break;
                }
            }



    }
    catch(IOException e){
        e.printStackTrace();
        printDebug("Socket read Error");
    }
    finally{
        is.close();os.close();br.close();s1.close();
        printDebug("Connection Closed");

    }

}
}