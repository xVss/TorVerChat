import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server{

    static int port = 1234;

    public static void main(final String[] args) throws Exception {
        // Instauro una connessione
        ServerSocket ascolto = new ServerSocket(Server.port);
        Socket connessione = ascolto.accept();

        // Creo gli objectStream in input e output
        ObjectInputStream in = new ObjectInputStream(connessione.getInputStream());
        ObjectOutputStream out = new ObjectOutputStream(connessione.getOutputStream());

        // Ricevo un oggetto Message da un client col metodo readObject e lo mostro a schermo
        Message ricevuto = (Message)in.readObject();
        ricevuto.show();

        // Invio un oggetto Message al client col metodo writeObject
        Message inviato = new Message("SERVER",ricevuto.from,"MESSAGGIO RICEVUTO DAL SERVER");
        out.writeObject(inviato);

        connessione.close();
    }
}

