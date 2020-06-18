package edu.unlu.sdypp.ej4;
public class Message {
    String from;
    String to;
    String msg;

    public Message (String from, String to, String msg) {
        this.from = from;
        this.to = to;
        this.msg = msg;
    }

    @Override
    public String toString (){
        return "-- Mensaje de: "+this.from+"\nHacia: "+this.to+"\n"+this.msg + " --";
    }
}

