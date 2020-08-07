import java.util.HashMap;
import java.io.Serializable;

public class Command implements Serializable {
    public Tipo type;
    public HashMap<String,String> campi = new HashMap<>();

    public Command(String type) {
        switch( type.toUpperCase() ){
            case "SIGN":
                this.type = Tipo.SIGN;
                break;
            case "LOG":
                this.type = Tipo.LOG;
                break;
            case "EXIT":
                this.type = Tipo.EXIT;
                break;
            case "OUT":
                this.type = Tipo.OUT;
                break;
            case "MKROOM":
                this.type = Tipo.MKROOM;
                break;
            case "JOINROOM":
                this.type = Tipo.JOINROOM;
                break;
            case "LEAVEROOM":
                this.type = Tipo.LEAVEROOM;
                break;
            case "ROOMNOTEXIST":
                this.type = Tipo.ROOMNOTEXIST;
                break;  
            case "SEND":
                this.type = Tipo.SEND;
                break;
            case "OK":
                this.type = Tipo.OK;
                break;
            case "ERR":
                this.type = Tipo.ERR;
                break;
            default:
                this.type = Tipo.NaC;
                break;
            }
    }
    
    public String toString(){
        switch( this.type.name() ){
            case "SIGN":
                return this.campi.get("USR") + "\n" + this.campi.get("PSW");
            case "LOG":
                return this.campi.get("USR") + "\n" + this.campi.get("PSW");
            case "OUT":
                return this.campi.get("USR");
            case "EXIT":
                return this.campi.get("USR");
            case "MKROOM":
                return this.campi.get("ROOM");
            case "JOINROOM":
                return this.campi.get("ROOM");
            case "LEAVEROOM":
                return this.campi.get("ROOM"); 
            case "SEND":
                return this.campi.get("FROM") + "\n" + this.campi.get("DATE") + "\n" +this.campi.get("MSG");
            case "OK":
                return "OK";
            case "ERR":
                return "ERR";
            }
        return "Not a Command";
    }
}

enum Tipo{ SIGN, LOG, OUT, EXIT, MKROOM, JOINROOM, LEAVEROOM, SEND, ROOMNOTEXIST, NaC, OK, ERR}

