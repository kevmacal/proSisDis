/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import java.util.Enumeration;
import javax.jms.*;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 *
 * @author marlon
 */
public class consumerMasterClient {
    private static final String USER = ActiveMQConnection.DEFAULT_USER; 
    private static final String PASSWORD = ActiveMQConnection.DEFAULT_PASSWORD;
    private static final String URL = "tcp://192.168.1.3:61616";
    private static final boolean TRANSACTED_SESSION = false;
    private String colaDest;
    private String client;
    public void processMessages(String queueDest, String client) throws JMSException, InterruptedException {
 
        final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(USER, PASSWORD, URL);
        final javax.jms.Connection connection = connectionFactory.createConnection();
 
        connection.start();
        this.colaDest=queueDest;
        this.client=client;
        final Session session = connection.createSession(TRANSACTED_SESSION, Session.AUTO_ACKNOWLEDGE);
        
        processMessagesInQueue(session);
        
        session.close();
        connection.close();
    }
    private void processMessagesInQueue(Session session) throws JMSException, InterruptedException {
        Message message;
        int i=0;
        QueueBrowser queuePeek;
        MessageConsumer consumer;
        while (i<100) {
            Thread.sleep(30);
            //System.out.println(i);
            queuePeek = session.createBrowser(session.createQueue(colaDest));
            Enumeration<?> messagesInQueue = queuePeek.getEnumeration();
            Message peek = (Message) messagesInQueue.nextElement();
            if (peek!=null){
                if (confirmMessage(peek)==1){
                    consumer = session.createConsumer(session.createQueue(colaDest));
                    if((message = consumer.receive()) != null){
                        i=i+proccessMessage(message);
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
            int tipoPeticion = textMessage.getIntProperty("tipoPeticion");
            String cliente=textMessage.getStringProperty("cliente");
            //final String text = textMessage.getText();
            //int servidor = textMessage.getIntProperty("servidor");
            //String[] tokens = text.split(",");
            //int servidor = Integer.parseInt(tokens[0]);            
            if(tipoPeticion == 0 && cliente.equals(this.client)){
                valorVerdad = 1;
            }
            //System.out.println("Confirmando text: "+text+" - total: "+totalConsumedMessages);
        }
        return valorVerdad;
    }
    private int proccessMessage(Message message) throws JMSException {
        int valor=0;
        if (message instanceof TextMessage) {
            final TextMessage textMessage = (TextMessage) message;
            String propietario = textMessage.getStringProperty("cliente");
            int peticion=textMessage.getIntProperty("tipoPeticion");
            int servidor=textMessage.getIntProperty("servidor");
            String cliente=textMessage.getStringProperty("cliente");            
            final String text = textMessage.getText();
            if(servidor!=0 && peticion == 0 && cliente.equals(this.client)){
               String[] topics=text.split(";");
               //System.out.println(topics[0]);
               if (topics[0].equals("topicsAnswer")){
                   int i;
                   //System.out.println(topics.length);
                   for(i=1;i<topics.length;i++){
                       mainGame.topicos.add(topics[i]+";"+servidor);
                       valor++;
                       //System.out.println("Aqui");
                   }
               }
            }            
            //totalConsumedMessages++;
            System.out.println(propietario+" = Procesa text: "+text);
        }
        return valor;
    }
}
