import java.util.Date;
import java.io.Serializable;

class Message implements Serializable {

    String from;
    String to;
    String msg;
    Date date = new Date();

    public Message(String from, String  to, String msg){
        this.from = from;
        this.to = to;
        this.msg = msg;
    }

    public void show(){
        System.out.println( "FROM:\t" + this.from +
                            "\nTO:\t" + this.to +
                            "\nDATE:\t" + this.date +
                            "\nTEXT:\n" + this.msg);
    }
}
