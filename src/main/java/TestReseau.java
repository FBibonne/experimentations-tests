import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class TestReseau {


    public static void main(String[] args) throws NumberFormatException, IOException {
        InetAddress host=InetAddress.getByName(args[0]);
        System.out.println("IP : "+host.toString());
        Socket s = new Socket(args[0], Integer.parseInt(args[1]));
        s.getOutputStream().write((byte) '\n');
        System.out.println(s.getInputStream().read());
        s.close();
    }
}