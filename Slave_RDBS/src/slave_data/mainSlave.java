/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package slave_data;
 
import java.io.*;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.jms.*;

/**
 *
 * @author marlon
 */
public class mainSlave {
    
    public static final String SERVER_IP="tcp://192.168.1.3:61616";  //Se cambia manualmente para cada servidor
    public static Map<String, ArrayList<String>> hScores;
    /*Configuracion de la base de datos es manual para cada server*/
    protected static final String USERNAME="root";
    protected static final String PASSWORDBD="0984287897";
    protected static final String CONN_STRING="jdbc:mysql://localhost:3306/prueba";
    
    public static void main(String[] args)  throws JMSException, InterruptedException, IOException {
        hScores=new HashMap<>();
        java.sql.Connection con=null;        
        try{
            con = DriverManager.getConnection(CONN_STRING, USERNAME, PASSWORDBD);
            String query = "SELECT * FROM Topico";
            Statement st=(Statement) con.createStatement();

            ResultSet rs = st.executeQuery(query);
            while (rs.next()){
                String nombre=rs.getString("nombre");
                leerScores("./scores/"+nombre+".txt", nombre);
                //System.out.println(id+":"+nombre);
            }            
        }catch (SQLException e){
            System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
        }
        //System.out.println(hScores.get("Fundamentos de Programacion").get(0));
        final consumerSlave waitPlayer = new consumerSlave();
        waitPlayer.processMessages();
    }
    
    public static void leerScores(String archivo, String key) throws FileNotFoundException, IOException {
      String cadena;
      ArrayList<String> puntajes=new ArrayList<>();
      FileReader f = new FileReader(archivo);
      BufferedReader b = new BufferedReader(f);
      while((cadena = b.readLine())!=null) {
          puntajes.add(cadena);
          //System.out.println(cadena);
      }
      b.close();
      hScores.put(key, puntajes);
    }
}
