import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.regex.*;

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
                    break;
                
                case "SIGN":
                    cmd = TVClient.SIGNIN();
                    outStream.writeObject( cmd );
                    // TODO: tutto quello che succede dopo la registrazione
                    cmd = (Command)inStream.readObject();
                    System.out.println(cmd);
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
}




class Password{
    private String pswrd;

    public Password(String word) { super(); }

    public static boolean isGood(String pw) {
        if ( Pattern.compile( "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?!.*\\s).{8,20})" ).matcher( pw ).find() ) return true;
        else return false;
    }
}