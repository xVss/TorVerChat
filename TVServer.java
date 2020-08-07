import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.*;
import java.sql.*;

public class TVServer {

    //Associazione nome stanza. Una stanza è un Set di utenti connessi
    public static HashMap<String, HashSet> stanze = new HashMap<>();

    //identifichiamo gli utenti da stringhe o da oggetti di tipo InetAddress????
    public static HashSet<String> utentiConnessi = new HashSet<String>();
    
    public static final int porta = 1234;

    public static void main(String[] args) throws Exception {

        ServerSocket ascolto = new ServerSocket(TVServer.porta);

        // TODO : Creare il thread Broadcast che invia a tutti

        while (true) {
            //Accetto la connessione con un client
            Socket connessione = ascolto.accept();
            System.out.println( connessione.getInetAddress() + " connesso." );

            //Eseguo il thread che si occupera' di interagire col client
            TVServer.utentiConnessi.add( connessione.getInetAddress().toString() );
            ThreadConnessione rn = new ThreadConnessione(connessione);
            Thread th = new Thread(rn);
            th.start();
        }

    }

}

// Creare classe del thread broadcast
class ThreadStanza implements Runnable {
    public ThreadStanza() {
        super();
    }
    public void run(){}
}

// Classe del thread di connessione
class ThreadConnessione implements Runnable {
    private Socket sock;
    private ObjectInputStream inStream;
    private ObjectOutputStream outStream;

    //Costruttore
    public ThreadConnessione(Socket sock) {
        this.sock = sock;
        try{
            this.inStream = new ObjectInputStream( sock.getInputStream() );
            this.outStream = new ObjectOutputStream( sock.getOutputStream() );
        } catch(Exception e) {
        }
    }

    public void run() {
        Command CMD = new Command("NaC");
        try{
            while( CMD.type.name() != "EXIT" ){
                
                CMD = (Command)inStream.readObject();

                switch( CMD.type.name() ){
                    case "SIGN":
                        //avvia fase di registrazione
                        String user = CMD.campi.get("USR");
                        String pass = CMD.campi.get("PSW");
                        if(SIGN(user, pass)) outStream.writeObject(new Command("OK"));
                        else outStream.writeObject(new Command("ERR"));
                        break;
                    case "LOG":
                        //avvia fase di acceso
                        break;
                    case "EXIT":
                        //esce
                        break;
                    default:
                        //Non faccio niente
                        break;
                    }

                System.out.println( "Comando " + CMD.type.name() + " ricevuto da  " + sock.getInetAddress() );

                System.out.println( CMD );

            }

            TVServer.utentiConnessi.remove( sock.getInetAddress().toString() );
            sock.close();

        } catch(Exception e) {
            TVServer.utentiConnessi.remove( sock.getInetAddress().toString() );
        }
    }


    public boolean SIGN(String user,String pass){
        try {
            String connectionString = "jdbc:mysql://localhost:3306/TorVerChat?user=root&password=Jackbiscotto1995";
            Class.forName("com.mysql.jdbc.Driver");
            Connection db = DriverManager.getConnection(connectionString);
            PreparedStatement insert = db.prepareStatement("insert into user(Nome,Password) values (?, ?)");
            insert.setString(1, user);
            insert.setString(2, pass);
            insert.execute();
            db.close();
            return true;
        } catch (Exception e) {
            //TODO: handle exception
            System.out.println("Qualche Errore".toUpperCase());
            System.out.println(e);
            return false;
        }
    }

}