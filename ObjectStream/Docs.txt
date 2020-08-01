Gli ObjectStream permettono di inviare/ricevere un oggetto serializzato, infatti l'oggetto da "streammare" deve implementare la classe speciale "Serializable".
Nell'esempio l'oggetto inviato è la classe "Message".


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



Per leggere o scrivere un oggeto serializzato si necessinta degli appositi input e output stream "ObjectInputStream" e "ObjectInputStream".
Questi costruttori prendono in input come paramatro un relativo InputStream e OutputStream. Quindi nel caso di connessioni via socket si possono inizializzare
come in esempio

        ObjectOutputStream output = new ObjectOutputStream( socket.getOutputStream() );
        ObjectInputStream input = new ObjectInputStream( socket.getInputStream() );
        
I metodi per inviare e ricevere un oggetto serializzato sono
      
        (Object)input.readObject();
        output.writeObject( obj );

Nell'esempio precedente quindi si possono scrivere e leggere oggetti di tipo messaggio come segue

        Message ricevuto = (Message)input.readObject();
        output.writeObject( new Message(...) );
