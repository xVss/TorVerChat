Per prima cosa scaricare il MySQL connector per java dal sito della oracle.
Settare il calss path, per esempio col comando
        
        export CLASSPATH=/[path]/mysql-connector-java-5.1.49-bin.jar:$CLASSPATH
 
 In alternativa è possibile definire il classpath del driver direttamente come opzione del comando java
        
        java -cp /[path]/mysql-connector-java-5.1.49-bin.jar [Programma]
 
 
 Esempio di programma con JDBC
 
        import java.sql.*;

        public class Interrogazione {
          
          public static void main(String[] args) {
                try{
                    //Stringa per la connessione
                    String connectionString = "jdbc:mysql://localhost:3306/Articoli?user=root&password=[password]";
                    
                    //Caricare in runtime la classe principale del driver del MySQL jdbc
                    Class.forName("com.mysql.jdbc.Driver");
                    
                    //Aprire la connessione col database
                    Connection connection =  DriverManager.getConnection(connectionString);
                    
                    //Creare uno statement, ovvero un oggetto che rappresenta un comando per il database
                    Statement stm = connection.createStatement();
                    
                    //Col metodo verrà eseguita la query richiesta e il risultato verrà riportato nell'oggetto di tipo ResultSet
                    ResultSet rs = stm.executeQuery("select arau.titolo, arau.autore, ar.rivista, ar.data from arau, ar where arau.titolo = ar.titolo  order by ar.data;");
                    
                    //Mostro i risultati della query
                    while (rs.next()) {
                        System.out.println(rs.getString("Titolo") + " |\t" + rs.getString("Autore") + " |\t" + rs.getString("Rivista") + " |\t" + rs.getDate("Data").toString());
                    }
                    
                    //Chiudo la connessione col database
                    connection.close();
                    
                } catch (Exception e) {
                    System.err.println("Errore");
                }
           }  
        }
