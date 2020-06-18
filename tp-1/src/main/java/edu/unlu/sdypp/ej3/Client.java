package edu.unlu.sdypp.ej3;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
   String user;
   BufferedReader inputChannel;
   PrintWriter outputChannel;
   Socket s;

   public Client (String ip, int port) {
      try {
         Scanner sc = new Scanner(System.in);
         this.s = new Socket (ip, port);
         System.out.println("--- Cliente conectado al servidor en "+ ip +":"+Integer.toString(port) + " --- ");
         System.out.println("--- Configurando canales de I/O ---");
         this.inputChannel = new BufferedReader (new InputStreamReader (this.s.getInputStream()));
         this.outputChannel = new PrintWriter (this.s.getOutputStream(), true);

         System.out.println("--- Ingrese su usuario: ---");
         this.user = sc.nextLine();
         outputChannel.println(this.user);
         int opt;
         String seleccion;
         boolean salir = false;
         while (!salir) {
            System.out.println("--- Ingrese el n�mero con la opcion de lo que desea hacer : ---");
            System.out.println("1- Enviar mensaje\n2- Ver casilla de mensajes\n3- Salir ");
            seleccion = sc.nextLine();
            while(!seleccion.equals("1") && !seleccion.equals("2") && !seleccion.equals("3")) {
               System.out.println("--- Error en la seleccion de opcion.\nIngrese que desea hacer:\n");
               System.out.println("1- Enviar mensaje\n2- Ver casilla de mensajes\n3- Salir ");
               seleccion = sc.nextLine();
            }
            opt = Integer.parseInt(seleccion);
            switch(opt) {
               case 1:
                  sendMessage();
                  break;
               case 2:
                  recoverMessage();
                  break;
               case 3:
                  salir = true;
                  outputChannel.println("salir");
                  break;
            }
         }
         s.close();

      } catch (UnknownHostException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private void recoverMessage() throws IOException {
      outputChannel.println("Leer");
      String r = "";
      String cadena = "";
      while(!r.contentEquals(".END")) {
         r = this.inputChannel.readLine();
         if(!r.contentEquals(".END"))
            cadena += r+"\n";
      }
      System.out.println(cadena);
   }

   private void sendMessage() throws IOException {
      String destino = "";
      String msg = "";
      String r= "";
      Scanner sc = new Scanner(System.in);
      outputChannel.println("Enviar");
      outputChannel.flush();
      r = this.inputChannel.readLine();
      System.out.println(" -- Server: "+ r + " -- ");
      destino = sc.nextLine();
      outputChannel.println(destino);
      outputChannel.flush();
      r = this.inputChannel.readLine();
      System.out.println(" -- Server: "+ r + " -- ");
      msg = sc.nextLine();
      outputChannel.println(msg);
      outputChannel.flush();
      r = this.inputChannel.readLine();
      System.out.println(" -- Server: "+r + " -- ");
   }

   public static void main(String[] args){
      Client c = new Client("localhost",9000);
   }
}
