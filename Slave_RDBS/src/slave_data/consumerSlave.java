/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package slave_data;
import java.util.Enumeration;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
 
import javax.jms.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.*;

/**
 *
 * @author marlon
 */
public class consumerSlave {
    private static final String URL = "tcp://192.168.0.5:61616"; 
    private static final String USER = ActiveMQConnection.DEFAULT_USER; 
    private static final String PASSWORD = ActiveMQConnection.DEFAULT_PASSWORD; 
    private static final String DESTINATION_QUEUE = "MasterSlave.Queue"; 
    private static final boolean TRANSACTED_SESSION = false; 
    private static final String USERNAME="root";
    private static final String PASSWORDBD="0984287897";
    private static final String CONN_STRING="jdbc:mysql://localhost:3306/prueba";
    private int totalConsumedMessages = 0; 
    private final int numServer = 1; //Este numero se asigna manualmente
    public void processMessages() throws JMSException {
 
        final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(USER, PASSWORD, URL);
        final javax.jms.Connection connection = connectionFactory.createConnection();
 
        connection.start();
 
        final Session session = connection.createSession(TRANSACTED_SESSION, Session.AUTO_ACKNOWLEDGE);
        final Destination destination = session.createQueue(DESTINATION_QUEUE);
        final MessageConsumer consumer = session.createConsumer(destination);
 
        processMessagesInQueue(consumer, session);
 
        consumer.close();
        session.close();
        connection.close();
    }
    private void processMessagesInQueue(MessageConsumer consumer, Session session) throws JMSException {
        Message message;
        int i=0;
        QueueBrowser queuePeek;
        while (i<20) {
            System.out.println(i);
            queuePeek = session.createBrowser(session.createQueue(DESTINATION_QUEUE));
            Enumeration<?> messagesInQueue = queuePeek.getEnumeration();
            Message peek = (Message) messagesInQueue.nextElement();
            if (peek!=null){
                if (confirmMessage(peek)==1){
                    if((message = consumer.receive()) != null){
                        proccessMessage(message);
                    }
                }
            }
            i++;
        }
    }
    private int confirmMessage(Message message) throws JMSException{
        int valorVerdad = 0;
        if (message instanceof TextMessage) {
            final TextMessage textMessage = (TextMessage) message;
            int servidor = textMessage.getIntProperty("servidor");
            int tipoPeticion = textMessage.getIntProperty("tipoPeticion");
            final String text = textMessage.getText();
            //String[] tokens = text.split(",");
            //int servidor = Integer.parseInt(tokens[0]);            
            if(servidor == numServer && tipoPeticion == 1){
                valorVerdad = 1;
            }
            System.out.println("Confirmando text: "+text+" - total: "+totalConsumedMessages);
        }
        return valorVerdad;
    }
    
    private void proccessMessage(Message message) throws JMSException {
        if (message instanceof TextMessage) {
            final TextMessage textMessage = (TextMessage) message;
            String propietario = textMessage.getStringProperty("cliente");
            int peticion=textMessage.getIntProperty("tipoPeticion");
            switch(peticion){
                case 1:
                    Connection con=null;        
                    try{
                        con = DriverManager.getConnection(CONN_STRING, USERNAME, PASSWORDBD);
                        String query = "SELECT * FROM Topico";
                        Statement st=(Statement) con.createStatement();
                        String topicos="";

                        ResultSet rs = st.executeQuery(query);
                        while (rs.next()){
                            int id = rs.getInt("ID_Topico");
                            String nombre=rs.getString("nombre");                                                      
                            topicos=topicos+nombre+";";
                            System.out.println(id+":"+nombre);
                        }  
                        producerSlave ps = new producerSlave();
                        ps.sendMessages(DESTINATION_QUEUE,topicos,propietario,numServer, 0); //Es respuesta  
                        st.close();            
                    }catch (Exception e){
                        System.err.println("Got an exception! ");
                        System.err.println(e.getMessage());
                    }
                    break;
                default:
                    System.out.println("No es peticion, es respuesta");
                    break;
            }
            final String text = textMessage.getText();
            totalConsumedMessages++;
            System.out.println(propietario+" = Procesa text: "+text+" - total: "+totalConsumedMessages);
        }
    }
}
