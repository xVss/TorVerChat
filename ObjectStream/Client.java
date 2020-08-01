import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Client {

    static int port = 1234;

    public static void main(final String[] args) throws Exception {
        // Instauro una connessione col server
        Socket connessione = new Socket("localhost", Client.port);

        // Creo gli objectStream in input e output
        ObjectOutputStream out = new ObjectOutputStream(connessione.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(connessione.getInputStream());

        // Invio un oggetto Message al server col metodo writeObject
        Message inviato = new Message("CLIENT","SERVER","MESSAGGIO INVIATO DA " + connessione.getLocalAddress());
        out.writeObject(inviato);

        // Ricevo un oggetto Message dal server col metodo readObject e lo mostro a schermo
        Message ricevuto = (Message)in.readObject();
        ricevuto.show();

        connessione.close();
    }
    
}