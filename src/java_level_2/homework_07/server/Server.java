package java_level_2.homework_07.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Vector;

public class Server {
    private Vector<ClientHandler> clients;

    public Server() throws SQLException {

        clients = new Vector<>();
        ServerSocket server = null;
        Socket socket = null;

        try {
            AuthService.connect();
            server = new ServerSocket(8189);
            System.out.println("Сервер запущен!");

            while (true) {
                socket = server.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this,socket);
            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            AuthService.disconnect();
        }
    }

    public void subscribe(ClientHandler client) {
        clients.add(client);
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
    }

    public void broadcastMsg(String msg) {
        for(ClientHandler o: clients){
            o.sendMsg(msg);
        }
    }
    //Реализация ЛС
    public String privateMsg(String fromNick, String toNick, String msg){
        for(ClientHandler o: clients){
            if (toNick.equals(o.getNick())){
             o.privateMsg(fromNick, msg);
             return ("PM to "+toNick+": "+msg);
            }
        }
        return "No such nickname!";
    }

    //Реализация уникальности пользователя
    public boolean isOnline(String nick){
        for(ClientHandler o: clients){
            if (nick.equals(o.getNick())){
                return true;
            }
        }
        return false;
    }

}
