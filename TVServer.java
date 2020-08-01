import java.net.*;
import java.util.*;

public class TVServer {
    // Creare un insieme di client connessi
    public static HashMap<String, HashSet> stanze = new HashMap<>();
    public static HashSet<String> utentiConnessi = new HashSet<String>();
    public static final int porta = 1234;

    public static void main(String[] args) throws Exception {
        ServerSocket ascolto = new ServerSocket(TVServer.porta);

        // TODO : Creare il thread Broadcast che invia a tutti

        while (true) {
            Socket connessione = ascolto.accept();
            System.out.println(connessione.getPort() + "Connesso");
            ThreadConnessione rn = new ThreadConnessione(connessione);
            Thread th = new Thread(rn);
            th.start();
        }

    }

}

// Creare classe del thread broadcast
class ThreadBro implements Runnable {
    public ThreadBro() {
        super();
    }
}

// Creare la classe del thread di connessione
class ThreadConnessione implements Runnable {
    private Socket sock;

    public ThreadConnessione(Socket sock) {
        this.sock = sock;
    }

    public void run() {

    }

}
