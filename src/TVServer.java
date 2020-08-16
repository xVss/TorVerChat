import java.io.DataOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.*;
import java.sql.*;

/**
 * @author Alessandro Straziota
 * @version 0.1
 */
public class TVServer {

    // Associazione nome stanza. Una stanza è un Set di utenti connessi
    public static HashMap<String, HashSet<Socket>> stanze = new HashMap<>();

    // identifichiamo gli utenti da stringhe o da oggetti di tipo InetAddress????
    public static HashSet<String> utentiConnessi = new HashSet<String>();

    public static final int porta = 1234;

    public static void main(String[] args) throws Exception {

        ServerSocket ascolto = new ServerSocket(TVServer.porta);

        /* Prima cosa da fare è caricare tutte le stanze */
        try {
            String connectionString = "jdbc:mysql://localhost:3306/TorVerChat?user=TVServer&password=Guala";
            Class.forName("com.mysql.jdbc.Driver");
            Connection db = DriverManager.getConnection(connectionString);
            String select = "select Nome from room";
            Statement query = db.createStatement();
            ResultSet res = query.executeQuery(select);
            while (res.next()) {
                TVServer.stanze.put(res.getString("Nome"), new HashSet<Socket>());
            }
            query.close();
            db.close();
        } catch (Exception e) {
            System.out.println("\tIMPOSSIBILE CARICARE STANZE.");
        }

        // TODO : Creare il thread Broadcast che invia a tutti

        while (true) {
            // Accetto la connessione con un client
            Socket connessione = ascolto.accept();
            System.out.println(connessione.getInetAddress() + " connesso.");

            // Eseguo il thread che si occupera' di interagire col client
            TVServer.utentiConnessi.add(connessione.getInetAddress().toString());
            ThreadConnessione rn1 = new ThreadConnessione(connessione);
            Thread th1 = new Thread(rn1);
            th1.start();

            /*
             * //Eseguo il thread che invece si occupa del trasferimento dei soli messaggi
             * ThreadStanza rn2 = new ThreadStanza( connessione ); Thread th2 = new Thread(
             * rn2 ); th2.start();
             */
        }

    }

}

// Classe del thread di connessione
class ThreadConnessione implements Runnable {
    private Socket sock;
    private ObjectInputStream inStream;
    private ObjectOutputStream outStream;
    private String user;

    // Costruttore
    public ThreadConnessione(Socket sock) {
        this.sock = sock;
        try {
            this.inStream = new ObjectInputStream(sock.getInputStream());
            this.outStream = new ObjectOutputStream(sock.getOutputStream());
        } catch (Exception e) {
        }
    }

    public void run() {
        Command CMD = new Command("NaC");
        try {
            while (CMD.type != Tipo.EXIT) {

                CMD = (Command) inStream.readObject();

                switch (CMD.type.name()) {
                    case "SIGN":
                        // avvia fase di registrazione
                        String user = CMD.campi.get("USR");
                        String pass = CMD.campi.get("PSW");
                        if (SIGN(user, pass))
                            outStream.writeObject(new Command("OK"));
                        else
                            outStream.writeObject(new Command("ERR"));
                        break;
                    case "LOG":
                        // avvia fase di acceso
                        String user2 = CMD.campi.get("USR");
                        String pass2 = CMD.campi.get("PSW");
                        if (LOG(user2, pass2)) {
                            outStream.writeObject(new Command("OK"));
                            this.user = user2;
                            ThreadConnessione.SERVE(this);
                        } else
                            outStream.writeObject(new Command("ERR"));
                        break;
                    case "EXIT":
                        // esce
                        break;
                    default:
                        // Non faccio niente
                        break;
                }

                // System.out.println( "Comando " + CMD.type.name() + " ricevuto da " +
                // sock.getInetAddress() );
                // System.out.println( CMD );

            }

            TVServer.utentiConnessi.remove(sock.getInetAddress().toString());
            System.out.println(sock.getInetAddress().toString() + " disconnesso.");
            sock.close();

        } catch (Exception e) {
            TVServer.utentiConnessi.remove(sock.getInetAddress().toString());
            System.out.println(sock.getInetAddress().toString() + " disconnesso.");
        }
    }

    public boolean SIGN(String user, String pass) {
        try {
            String connectionString = "jdbc:mysql://localhost:3306/TorVerChat?user=TVServer&password=Guala";
            Class.forName("com.mysql.jdbc.Driver");
            Connection db = DriverManager.getConnection(connectionString);
            PreparedStatement insert = db.prepareStatement("insert into user(Nome,Password) values (?, ?)");
            insert.setString(1, user);
            insert.setString(2, pass);
            insert.execute();
            db.close();
            return true;
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Qualche Errore".toUpperCase());
            System.out.println(e);
            return false;
        }
    }

    public boolean LOG(String user, String pass) {
        try {
            String connectionString = "jdbc:mysql://localhost:3306/TorVerChat?user=TVServer&password=Guala";
            Class.forName("com.mysql.jdbc.Driver");
            Connection db = DriverManager.getConnection(connectionString);
            String select = "select Nome, Password from user where Nome = \'" + user + "\' and Password = \'" + pass
                    + "\'";
            Statement query = db.createStatement();
            ResultSet res = query.executeQuery(select);
            res.next();
            if (res.getString("Nome").equals(user) && res.getString("Password").equals(pass)) {
                query.close();
                db.close();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Qualche Errore".toUpperCase());
            System.out.println(e);
            return false;
        }
    }

    public static void SERVE(ThreadConnessione th) throws Exception {
        /*
         * In questa modalita' il server attende istruzioni perservire poi il client
         * dopo la fase di accesso
         */
        Command CMD = new Command("NaC");

        System.out.println(th.sock.getInetAddress().toString() + " logged in as " + th.user);

        String room;
        String psw;
        String txt;

        while (CMD.type != Tipo.OUT) {

            CMD = (Command) th.inStream.readObject();

            /* ================== MAKEROOM ==================== */
            if (CMD.type == Tipo.MKROOM) {
                room = CMD.campi.get("ROOM");
                psw = CMD.campi.get("PSW");
                if (th.MAKE(room, psw, th.user)) {
                    TVServer.stanze.put(room, new HashSet<Socket>());
                    th.outStream.writeObject(new Command("OK"));
                } else {
                    th.outStream.writeObject(new Command("ERR"));
                }
            }

            /* ================== JOINROOM ==================== */
            else if (CMD.type == Tipo.JOINROOM) {
                room = CMD.campi.get("ROOM");
                psw = CMD.campi.get("PSW");
                if (th.JOIN(room, psw)) {
                    HashSet<Socket> stanza = TVServer.stanze.get(room);
                    if (stanza != null) {
                        stanza.add(th.sock);
                        th.outStream.writeObject(new Command("OK"));
                        System.out.println(
                                th.sock.getInetAddress().toString() + " join in room " + room + " as " + th.user);
                    } else {
                        th.outStream.writeObject(new Command("ERR"));
                    }
                } else {
                    th.outStream.writeObject(new Command("ERR"));
                }
            }

            /* ================== LEAVEROOM ==================== */
            else if (CMD.type == Tipo.LEAVEROOM) {
                room = CMD.campi.get("ROOM");
                HashSet<Socket> stanza = TVServer.stanze.get(room);
                if (stanza != null) {
                    stanza.add(th.sock);
                    th.outStream.writeObject(new Command("OK"));
                    System.out
                            .println(th.sock.getInetAddress().toString() + " leave in room " + room + " as " + th.user);
                } else {
                    th.outStream.writeObject(new Command("ERR"));
                }
            }

            /* ================== SEND ==================== */
            else if (CMD.type == Tipo.SEND) {
                try {
                    room = CMD.campi.get("ROOM");
                    txt = CMD.campi.get("MSG");
                    HashSet<Socket> stanza = TVServer.stanze.get(room);
                    if (stanza != null) {
                        // Invia a tutti
                        th.outStream.writeObject(new Command("OK"));
                        for (Socket s : stanza) {
                            /*
                             * BufferedWriter broadstream = new BufferedWriter( new OutputStreamWriter(
                             * s.getOutputStream() ) ); broadstream.write( th.user + ": " + txt + "\n" );
                             * //DataOutputStream broadstream = new DataOutputStream( s.getOutputStream() );
                             * //broadstream.writeBytes( th.user + ": " + txt + "\n" );
                             */

                            /*
                             * ObjectOutputStream broadstream = new ObjectOutputStream( s.getOutputStream()
                             * ); Command mes = new Command("SEND"); mes.campi.put("MSG", txt);
                             * mes.campi.put("FROM", th.user); broadstream.writeObject( mes );
                             */
                            ThreadConnessione.SENDMESSAGE(s, th.user + ": " + txt);
                        }
                        th.outStream.writeObject(new Command("OK"));
                    } else {
                        th.outStream.writeObject(new Command("ERR"));
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                    th.outStream.writeObject(new Command("ERR"));
                }
            }
        }

        System.out.println(th.sock.getInetAddress().toString() + " logged out as " + th.user);
    }

    public boolean MAKE(String room, String pass, String user) {
        try {
            String connectionString = "jdbc:mysql://localhost:3306/TorVerChat?user=TVServer&password=Guala";
            Class.forName("com.mysql.jdbc.Driver");
            Connection db = DriverManager.getConnection(connectionString);
            PreparedStatement insert = db.prepareStatement("insert into room(Nome,Password,Creatore) values (?, ?, ?)");
            insert.setString(1, room);
            insert.setString(2, pass);
            insert.setString(3, user);
            insert.execute();
            db.close();
            return true;
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Qualche Errore".toUpperCase());
            System.out.println(e);
            return false;
        }
    }

    public boolean JOIN(String room, String pass) {
        try {
            String connectionString = "jdbc:mysql://localhost:3306/TorVerChat?user=TVServer&password=Guala";
            Class.forName("com.mysql.jdbc.Driver");
            Connection db = DriverManager.getConnection(connectionString);
            String select = "select Nome, Password from room where Nome = \'" + room + "\'";
            Statement query = db.createStatement();
            ResultSet res = query.executeQuery(select);
            res.next();
            if (res.getString("Nome").equals(room) && res.getString("Password").equals(pass)) {
                query.close();
                db.close();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Qualche Errore".toUpperCase());
            System.out.println(e);
            return false;
        }
    }

    public static void SENDMESSAGE(Socket s, String msg) throws Exception {

        DatagramSocket clientSocket = new DatagramSocket();
        byte[] richiesta_b = msg.getBytes();
        DatagramPacket messaggio = new DatagramPacket(richiesta_b, richiesta_b.length, s.getInetAddress(),
                TVServer.porta + 1);
        clientSocket.send(messaggio);
        clientSocket.close();
    }
}
