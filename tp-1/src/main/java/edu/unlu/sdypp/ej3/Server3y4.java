package edu.unlu.sdypp.ej3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server3y4 {
   int port;
   ServerSocket ss;
   Socket cs;
   String msg;
   BufferedReader inputChannel;
   PrintWriter outputChannel;
   public Server3y4 (int port){
      this.port = port;
      try {
         this.ss = new ServerSocket (this.port);
         int counter = 0;
         System.out.println(" -- Servidor corriendo en el puerto: "+port + " -- ");
         while (true) {
            this.cs = ss.accept();
            counter++;
            System.out.println(" -- Cliente Nro:"+counter + " -- ");
            Thread3 ts = new Thread3 (this.cs);
            Thread tsThread = new Thread (ts);
            tsThread.start();
         }
      }catch (IOException e) {
         System.out.println(" -- Socket en el puerto "+port+" esta siendo utilizado -- ");
      }
   }

   public static void main(String[] args){
      Server3y4 s = new Server3y4(9000);
   }
}
