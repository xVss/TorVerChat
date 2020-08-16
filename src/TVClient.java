import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.*;
import java.util.regex.*;

/**
 * @author Alessandro Straziota
 * @version 0.1
 */
public class TVClient {

    public static final int porta = 1234;
    public static final String addr = "localhost";

    public static void main(String[] args) throws Exception {
        //Socket di connessione col Server
        Socket connessione = new Socket(TVClient.addr, TVClient.porta);

        //Object input e output stream
        ObjectOutputStream outStream = new ObjectOutputStream( connessione.getOutputStream() );
        ObjectInputStream inStream = new ObjectInputStream( connessione.getInputStream() );

        //Input stream da tastiera
        BufferedReader tastiera = new BufferedReader( new InputStreamReader(System.in) );

        /* Creo un thread che rimane perennemente in ascolto di stringhe e non di oggetti */
        ThreadStanza threadStanza = new ThreadStanza();
        Thread th = new Thread( threadStanza );
        th.start();
        

        Command cmd = new Command("NaC");

        /*
        outStream.writeObject( cmd );

        cmd = new Command("JOinRooM");

        outStream.writeObject( cmd );

        cmd = new Command("send");
        cmd.campi.put("FROM", "Alessandro");
        cmd.campi.put("DATE", (new Date()).toString());
        cmd.campi.put("MSG", "Questo è un messaggio di prova\ninvato da Alessandro ;).\n");

        outStream.writeObject( cmd );
        */

        System.out.println("############### T.V.Chat ###############");
        System.out.println("Digita un comando oppure digita \"help\" per aiuto.");


        String keybord = "";
        String MSG = "";
        
        while( ! keybord.equals("EXIT") ) {
            keybord = tastiera.readLine().toUpperCase();
            
            switch( keybord ){

                case "LOG":
                    cmd = TVClient.LOGIN();
                    outStream.writeObject( cmd );
                    // TODO: tutto quello che succede dopo l'accesso
                    cmd = (Command)inStream.readObject();
                    if( cmd.type == Tipo.OK ) TVClient.SERVICE(outStream, inStream);
                    else System.out.println( "Username o password errate." );
                    break;
                
                case "SIGN":
                    cmd = TVClient.SIGNIN();
                    outStream.writeObject( cmd );
                    // TODO: tutto quello che succede dopo la registrazione
                    cmd = (Command)inStream.readObject();
                    if( cmd.type == Tipo.OK ) System.out.println( "Registrazione avvenuta correttamente." );
                    else System.out.println( "Username gia' in uso." );
                    break;
                
                case "EXIT":
                    System.out.println("Arrivederci!");
                    cmd = new Command("EXIT");
                    cmd.campi.put("USR", connessione.getLocalAddress().toString() );
                    outStream.writeObject( cmd );
                    break;

                case "HELP":
                    System.out.println("\t- \"log\" per effettuare l'accesso");
                    System.out.println("\t- \"sign\" per registrare un account");
                    System.out.println("\t- \"exit\" per terminare");
                    System.out.println("\t- \"help\" per visualizzare i comandi");
                    System.out.println("\t- \"version\" per visualizzare la versione");
                    break;
                
                case "VERSION":
                    break;
                
                default:
                    System.out.println("Comando inesistente.");
                    break;
            }
        }

        connessione.close();
    }
    
    private static Command SIGNIN(){
        Command cmd = new Command("SIGN");
        try{
            BufferedReader tastiera = new BufferedReader( new InputStreamReader( System.in ) );
            System.out.println("Inserire un username:");
            String usr = tastiera.readLine().toLowerCase();
            System.out.println("Inserire una password lunga almeno 8 caratteri, con almeno un carattere minuscolo, uno maiuscolo e un numero:");
            String psw = tastiera.readLine();
            
            while( ! Password.isGood(psw) ) {
                System.out.println("Password non corretta.");
                System.out.println("La password lunga almeno 8 caratteri, con almeno un carattere minuscolo, uno maiuscolo e un numero:");
                psw = tastiera.readLine();
            }
    
            cmd.campi.put("USR", usr);
            cmd.campi.put("PSW", psw);
            return cmd;
    
        } catch (Exception e) {
            //Conviene inviare un comando NULLO oppure un comando di tipo SIGN ma vuoto???
            cmd.type = Tipo.NaC;
            return cmd;
        }
    }

    private static Command LOGIN(){
        Command cmd = new Command("LOG");
        try{
            
            BufferedReader tastiera = new BufferedReader( new InputStreamReader( System.in ) );
            System.out.println("Username:");
            String usr = tastiera.readLine().toLowerCase();
            System.out.println("Password:");
            String psw = tastiera.readLine();
    
            cmd.campi.put("USR", usr);
            cmd.campi.put("PSW", psw);
            return cmd;
    
        } catch (Exception e) {
            //Conviene inviare un comando NULLO oppure un comando di tipo SIGN ma vuoto???
            cmd.type = Tipo.NaC;
            return cmd;
        }
    }


    private static void SERVICE(ObjectOutputStream outStream, ObjectInputStream inStream) throws Exception {

        System.out.println("Benvenuto!\nDigita:");
        System.out.println("\t- \"make\" per creare una stanza");
        System.out.println("\t- \"join\" per accedere a una stanza");
        System.out.println("\t- \"leave\" per abbandonare la stanza");
        System.out.println("\t- \"send\" se sei in una stanza per inviare un messaggio");
        System.out.println("\t- \"list\" per avere una lista delle stanze");
        System.out.println("\t- \"help\" per visualizzare i comandi");
        System.out.println("\t- \"out\" per uscire");

        Command CMD = new Command("NaC");

        String room;
        String psw;
        String txt;
        String currentRoom = null;

        //Input stream da tastiera
        BufferedReader tastiera = new BufferedReader( new InputStreamReader(System.in) );
        String keyboard = "";

        while ( ! keyboard.equals("OUT") ) {
            
            keyboard = tastiera.readLine().toUpperCase();

            /* ================== MAKEROOM ==================== */
            if( keyboard.equals("MAKE") ){
                System.out.println("Nome stanza:");
                room = tastiera.readLine();
                System.out.println("Password:");
                psw = tastiera.readLine();
                CMD = new Command("MKROOM");
                CMD.campi.put("ROOM", room);
                CMD.campi.put("PSW", psw);
                outStream.writeObject( CMD );

                CMD = (Command)inStream.readObject();
                if( CMD.type == Tipo.OK ) System.out.println("Stanza registrata con successo.");
                else System.out.println("Nome stanza gia' in uso.");
            }

            /* ================== JOINROOM ==================== */
            else if( keyboard.equals("JOIN") ){
                System.out.println("Stanza:");
                room = tastiera.readLine();
                System.out.println("Password:");
                psw = tastiera.readLine();
                CMD = new Command("JOINROOM");
                CMD.campi.put("ROOM", room);
                CMD.campi.put("PSW", psw);
                outStream.writeObject( CMD );

                CMD = (Command)inStream.readObject();
                if( CMD.type == Tipo.OK ) {
                    System.out.println("##########   " + room + "   ##########");
                    currentRoom = room;
                } else {
                    System.out.println("Nome stanza o password errate.");
                }
            }
            
            /* ================== LEAVEROOM ==================== */
            else if( keyboard.equals("LEAVE") ){
                CMD = new Command("LEAVEROOM");
                CMD.campi.put("ROOM", currentRoom);
                outStream.writeObject( CMD );

                CMD = (Command)inStream.readObject();
                if( CMD.type == Tipo.OK ) {
                    System.out.println("########## ########## ##########");
                    currentRoom = null;
                } else {
                    System.out.println("Qualcosa e' andato storto! Impossibile abbandonare la stanza.");
                }
            }

            /* ================== SEND ==================== */
            else if( keyboard.equals("SEND") ){
                CMD = new Command("SEND");
                txt = tastiera.readLine();
                CMD.campi.put("ROOM", currentRoom);
                CMD.campi.put("MSG", currentRoom);
                outStream.writeObject( CMD );

                CMD = (Command)inStream.readObject();
                if( CMD.type == Tipo.ERR ) {
                    System.out.println("\t[Qualcosa e' andato storto. Riporva.]");
                }
            }


            /* ================== HELP ==================== */
            else if( keyboard.equals("HELP") ) {
                System.out.println("\t- \"make\" per creare una stanza");
                System.out.println("\t- \"join\" per accedere a una stanza");
                System.out.println("\t- \"list\" per avere una lista delle stanze");
                System.out.println("\t- \"help\" per visualizzare i comandi");
                System.out.println("\t- \"out\" per uscire");
            }


            /* ================== OUT ==================== */
            else if( keyboard.equals("OUT") ) {
                CMD = new Command("OUT");
                outStream.writeObject( CMD );
            }

        }

        System.out.println("Arrivederci!");
    }
}




class Password{
    private String pswrd;

    public Password(String word) { super(); }

    public static boolean isGood(String pw) {
        if ( Pattern.compile( "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?!.*\\s).{8,20})" ).matcher( pw ).find() ) return true;
        else return false;
    }
}


// Thread di ascolto da stanza
class ThreadStanza implements Runnable {

    private DatagramSocket sock;

    //Costruttore
    public ThreadStanza() {
        try {
            this.sock = new DatagramSocket( TVServer.porta + 1 );                  
        } catch (Exception e) {
            //TODO: handle exception
            System.out.println("Non e' possibile ricevere i messaggi : impossibile creare thread d'ascolto.");
        }
    }

    public void run(){
        try {   
            while( this.sock.isConnected() ) {
                byte[] richiesta_b = new byte[1024]; 
                DatagramPacket richiesta_pack = new DatagramPacket(richiesta_b, richiesta_b.length); 
                this.sock.receive(richiesta_pack); 
                String mes = new String(richiesta_pack.getData());
                System.out.println( mes );
            }
        } catch (Exception e) {
            //TODO: handle exception
            System.out.println("Non e' possibile ricevere i messaggi.");
        }
    }
}