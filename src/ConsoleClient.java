import Data.Packet;
import java.io.BufferedOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class ConsoleClient {
    public static String name;
    public static String password;
    public static Socket master;
    public static int id =0;

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.print("Enter IP address: ");
        Scanner in = new Scanner(System.in);
        String ip = in.nextLine();
        new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        System.out.print("Enter your name: ");
        name = in.nextLine();
        System.out.print("Enter password: ");
        password = in.nextLine();
        master = new Socket();
        InetSocketAddress address = new InetSocketAddress(ip, 4242);
        try{
            master.connect(address);
            sendRegisterationPacket();
            loginData();
            if(id > 0){
                ClientThread clientTh = new ClientThread();
                clientTh.start();
                BufferedOutputStream out = new BufferedOutputStream(master.getOutputStream());
                for(;;){
                    String input = in.nextLine();
                    Packet p = new Packet(Packet.PacketType.Chat, id);
                    p.Gdata.add(name);
                    p.Gdata.add(input);
                    out.write(p.toBytes());
                    out.flush();
                }
            }
        }
        catch(Exception e){}


    }
    public static void loginData()throws IOException{
        byte[] buffer;
        int readBytes;
        try{
            buffer = new byte[master.getReceiveBufferSize()];
            InputStream scktIs = master.getInputStream();
            scktIs.read(buffer);
            readBytes = buffer.length;
            if(readBytes > 0){
                dataManager(new Packet(buffer));
            }
        }
        catch(SocketException e){
            System.out.println("The server has disconnected!");
            System.out.println();
        }
        catch(Exception e){}
    }

    static void dataManager(Packet p){
        switch(p.packetType){
            case Registeration:
                id = p.senderId;
                break;
            case Chat:
                System.out.println(p.Gdata.get(0) + ": " + p.Gdata.get(1) );
                break;
        }

    }

    public static void sendRegisterationPacket() throws Exception{
        BufferedOutputStream out = new BufferedOutputStream(master.getOutputStream());
        Packet p = new Packet(Packet.PacketType.Registeration,id);
        p.Gdata.add(id);
        p.Gdata.add(name);
        p.Gdata.add(password);
        out.write(p.toBytes());
        out.flush();
    }

}

class ClientThread extends Thread {
    byte[] buffer;
    int readBytes;

    public void run(){
        for(;;){
            try{
                buffer = new byte[ConsoleClient.master.getSendBufferSize()];
                InputStream scktIs = ConsoleClient.master.getInputStream();
                scktIs.read(buffer);
                readBytes = buffer.length;
                if(readBytes > 0 ){
                    ConsoleClient.dataManager(new Packet(buffer));
                }
            }
            catch(SocketException e){
                System.out.println("The server has disconnected!");
                System.out.println();
                break;
            }
            catch(Exception e){}
        }
    }
}
