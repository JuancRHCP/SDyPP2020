package edu.unlu.sdypp.ej3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;

public class Thread3 implements Runnable{
   Socket client;
   BufferedReader inputChannel;
   PrintWriter outputChannel;
   ArrayList<Message> coladeMensaje;
   final String ARCHIVO = "./arch1.txt";

   public Thread3(Socket client) {
      this.client = client;
      try {
         this.inputChannel = new BufferedReader (new InputStreamReader (this.client.getInputStream()));
         this.outputChannel = new PrintWriter (this.client.getOutputStream(), true);
      } catch (IOException e) {
         e.printStackTrace();
      }

   }

   public String getMessages() {
      String r = "";
      for (Message message : coladeMensaje) {
         r += message.toString()+"\n----\n";
      }
      return r;
   }

   public String getMessages(String from) {
      String r = "";
      for (Message message : coladeMensaje) {
         if (message.to.contentEquals(from))
            r += message.toString()+"\n----\n";
      }
      if (r.isEmpty()) {
         r = " -- No hay mensajes nuevos -- \n";
      }
      return r;
   }

   public void addMessage(String from, String to, String msg){
      this.coladeMensaje.add(new Message(from,to,msg));
      try {
         this.Changes();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public Message splitMsg(String s) {
      String[] sv = s.split("-");
      Message m = new Message(sv[0],sv[1],sv[2]);
      return m;
   }

   public void Changes() throws IOException {
      String s = "";
      File file = new File(ARCHIVO);
      FileWriter fw = new FileWriter(file, false);
      for (Message message : coladeMensaje) {
         s = message.from+"-"+message.to+"-"+message.msg+"\r\n.END\r\n";
         fw.write(s);
      }
      fw.close();
   }

   public void readMessages(){
      File tempFile = new File(ARCHIVO);
      if (tempFile.exists()) {
         try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String s;
            while ((s = br.readLine()) != null) {
               if(!s.contentEquals(".END") || !s.contentEquals(""))
                  this.coladeMensaje.add(splitMsg(s));
            }
         } catch (IOException e) {
            e.printStackTrace();
         }
      }else {
         try {
            System.out.println(" -- El archivo de mensajes no existe...\nCreandolo en "+ tempFile.getCanonicalPath());
            tempFile.createNewFile();
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

   private void sendMessage(String us) {
      String destino = "";
      String msg = "";
      String user = us;
      try {
         this.outputChannel.println(" -- Ingrese el destino: -- ");
         this.outputChannel.flush();
         while(destino.equalsIgnoreCase("")) {
            destino = this.inputChannel.readLine();
         }
         System.out.println(" - Destino: "+destino + " -");
         this.outputChannel.println(" -- Ingrese el mensaje a enviar -- ");
         this.outputChannel.flush();
         while(msg.equalsIgnoreCase("")) {
            msg = this.inputChannel.readLine();
         }
         System.out.println(" - Mensaje: "+msg + " -");
      } catch (IOException e) {
         e.printStackTrace();
      }
      addMessage(user,destino,msg);
      this.outputChannel.println("\n -- Mensaje enviado -- ");
   }

   public void run() {
      String msgClient;
      String user;
      boolean salir = false;
      try {
         this.coladeMensaje = new ArrayList<Message>();
         System.out.println(" -- Leyendo mensajes --");
         readMessages();
         user = this.inputChannel.readLine();
         System.out.println(" -- Se conecto el "+user+" desde "+client.getInetAddress()+":"+client.getPort());
         msgClient = this.inputChannel.readLine();
         while (!salir) {
            this.outputChannel.flush();
            switch(msgClient) {
               case "Enviar":
                  sendMessage(user);
                  msgClient = "";
                  break;
               case "Leer":
                  this.outputChannel.print(getMessages(user));
                  this.outputChannel.println(".END");
                  msgClient = "";
                  break;
               case "salir":
                  salir = true;
                  this.client.close();
                  break;
               case "":
                  msgClient = this.inputChannel.readLine();
            }
         }
         System.out.println(" -- Cerrando la conexion : "+client.getPort() + " -- ");

         this.client.close();
      } catch (IOException e1) {
         e1.printStackTrace();
      }
   }

}
