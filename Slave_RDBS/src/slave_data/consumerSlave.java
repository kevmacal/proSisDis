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
    private static final String URL = "tcp://192.168.1.3:61616"; 
    private static final String USER = ActiveMQConnection.DEFAULT_USER; 
    private static final String PASSWORD = ActiveMQConnection.DEFAULT_PASSWORD; 
    private static final String DESTINATION_QUEUE = "MasterSlave.Queue"; 
    private static final boolean TRANSACTED_SESSION = false; 
    private static final String USERNAME="root";
    private static final String PASSWORDBD="0984287897";
    private static final String CONN_STRING="jdbc:mysql://localhost:3306/prueba";
    private int totalConsumedMessages = 0; 
    private final int numServer = 1; //Este numero se asigna manualmente
    public void processMessages() throws JMSException, InterruptedException {
 
        final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(USER, PASSWORD, URL);
        final javax.jms.Connection connection = connectionFactory.createConnection();
 
        connection.start();
 
        final Session session = connection.createSession(TRANSACTED_SESSION, Session.AUTO_ACKNOWLEDGE);
        //final Destination destination = session.createQueue(DESTINATION_QUEUE);
        
 
        processMessagesInQueue(session);
        
        session.close();
        connection.close();
    }
    private void processMessagesInQueue(Session session) throws JMSException, InterruptedException {
        Message message;
        int i=0;
        QueueBrowser queuePeek;
        MessageConsumer consumer;
        while (i<1000) {
            Thread.sleep(70);
            //System.out.println(i);
            queuePeek = session.createBrowser(session.createQueue(DESTINATION_QUEUE));
            Enumeration<?> messagesInQueue = queuePeek.getEnumeration();
            Message peek = (Message) messagesInQueue.nextElement();
            if (peek!=null){
                if (confirmMessage(peek)==1){
                    consumer= session.createConsumer(session.createQueue(DESTINATION_QUEUE));
                    if((message = consumer.receive()) != null){
                        proccessMessage(message);
                        consumer.close();
                    }
                }
            }
            queuePeek.close();
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
            if(servidor == numServer && tipoPeticion >= 1){
                valorVerdad = 1;
            }
            //System.out.println("Confirmando text: "+text+" - total: "+totalConsumedMessages);
        }
        return valorVerdad;
    }
    
    private void proccessMessage(Message message) throws JMSException {
        if (message instanceof TextMessage) {
            final TextMessage textMessage = (TextMessage) message;
            String propietario = textMessage.getStringProperty("cliente");
            int peticion=textMessage.getIntProperty("tipoPeticion");
            int servidor=textMessage.getIntProperty("servidor");
            final String text = textMessage.getText();
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
                            //System.out.println(id+":"+nombre);
                        }  
                        producerSlave ps = new producerSlave();
                        ps.sendMessages(DESTINATION_QUEUE,topicos,propietario,numServer, 0); //Es respuesta  
                        st.close();            
                    }catch (SQLException | JMSException e){
                        System.err.println("Got an exception! ");
                        System.err.println(e.getMessage());
                    }
                    break;
                case 2:
                    String[] queues=text.split(";");
                    producerTopicQueue ptq=new producerTopicQueue();
                    String[] topic=queues[1].split("\\.");
                    //System.out.println(topic[1]);
                    ptq.sendMessages(queues[1], "queue;Cola creada", propietario, servidor, 0,topic[1]); //Se envia al cliente en la nueva cola una respuesta de creacion, en espera de que el cliente acepte
                    producerSlave ps=new producerSlave();
                    ps.sendMessages(DESTINATION_QUEUE, "queue;"+mainSlave.SERVER_IP+";"+queues[1], propietario, numServer, 0);
                    break;
                default:
                    System.out.println("No es peticion, es respuesta");
                    break;
            }            
            totalConsumedMessages++;
            System.out.println(propietario+" = Procesa text: "+text+" - total: "+totalConsumedMessages);
        }
    }
}
