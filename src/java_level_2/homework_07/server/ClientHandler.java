package java_level_2.homework_07.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientHandler {

   private Socket socket;
   private DataInputStream in;
   private DataOutputStream out ;
   private Server server;
   private String nick;

    public String getNick() {
        return nick;
    }

    private Vector<ClientHandler> clients;

    public Socket getSocket() {
        return socket;
    }

    public ClientHandler(Server server, Socket socket) {
       try {
           this.server = server;
           this.socket = socket;
           this.in = new DataInputStream(socket.getInputStream());
           this.out = new DataOutputStream(socket.getOutputStream());

           new Thread(new Runnable() {
               @Override
               public void run() {
                   try {
                       while (true) {
                           String str = in.readUTF();

                           if(str.startsWith("/auth")) {
                               String[] tokens = str.split(" ");
                               String newNick = AuthService.getNickByLoginAndPass(tokens[1], tokens[2]);
                               if(newNick != null) {
                                   if(server.isOnline(newNick)){ //Реализация уникальности пользователя
                                       sendMsg("Внимание! Пользователь с указаным логином и паролем уже подключен к чату! Если это не вы - обратитесь в службу поддержки!");
                                   } else {
                                   sendMsg("/authok");
                                   nick = newNick;
                                   server.subscribe(ClientHandler.this);
                                   break;}
                               } else {
                                   sendMsg("неверный логин/пароль");
                               }
                           }
                       }

                       while (true) {
                           String str = in.readUTF();
                           if(str.equals("/end")) {
                               out.writeUTF("/serverClosed");
                               break;
                           }
                           // Реализация ЛС
                           Pattern p = Pattern.compile("^/w\\s(\\S*)\\s?(.*)$");
                           Matcher m = p.matcher(str);
                           if (m.matches()) {
                               try {
//                                   out.writeUTF(m.group(1));
//                                   out.writeUTF(m.group(2));
                                   out.writeUTF(server.privateMsg(nick, m.group(1), m.group(2)));
                               } catch (IOException e) {
                                   e.printStackTrace();
                               }
                           } else {
                               server.broadcastMsg(nick + ": " + str);
                               System.out.println("Client " + str);
                           }
//                           server.broadcastMsg(nick + ": " + str);
//                           System.out.println("Client " + str);
                       }

                   } catch (IOException e) {
                       e.printStackTrace();
                   } finally {
                       try {
                           in.close();
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                       try {
                           out.close();
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                       try {
                           socket.close();
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                       server.unsubscribe(ClientHandler.this);
                   }

               }
           }).start();

       } catch (IOException e) {
           e.printStackTrace();
       }

   }

   public void sendMsg(String msg) {
       try {
           out.writeUTF(msg);
       } catch (IOException e) {
           e.printStackTrace();
       }
   }

//    Реализация ЛС
    public void privateMsg(String fromNick, String msg){
        try {
            out.writeUTF("PM from "+fromNick+": "+msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
