/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import java.util.Enumeration;
import java.util.Scanner;
import javax.jms.*;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 *
 * @author marlon
 */
public class consumerSlaveClient {
    private static final String USER = ActiveMQConnection.DEFAULT_USER; 
    private static final String PASSWORD = ActiveMQConnection.DEFAULT_PASSWORD;
    private static final boolean TRANSACTED_SESSION = false;
    private String colaDest;
    private String client;
    public void processMessages(String client) throws JMSException, InterruptedException {
 
        final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(USER, PASSWORD, mainGame.topicQueueIP);
        final javax.jms.Connection connection = connectionFactory.createConnection();
 
        connection.start();
        this.colaDest=mainGame.topicQueueName;
        this.client=client;
        final Session session = connection.createSession(TRANSACTED_SESSION, Session.AUTO_ACKNOWLEDGE);
        
        processMessagesInQueue(session);
        
        session.close();
        connection.close();
    }
    private void processMessagesInQueue(Session session) throws JMSException, InterruptedException {
        Message message;
        int i=0;
        int salir=0;
        QueueBrowser queuePeek;
        MessageConsumer consumer;
        /*Se da un minuto de tiempo*/
        while (i<2000 || salir==0) {
            Thread.sleep(30);
            //System.out.println(i);
            queuePeek = session.createBrowser(session.createQueue(colaDest));
            Enumeration<?> messagesInQueue = queuePeek.getEnumeration();
            Message peek = (Message) messagesInQueue.nextElement();
            if (peek!=null){
                if (confirmMessage(peek)==1){
                    consumer = session.createConsumer(session.createQueue(colaDest));
                    if((message = consumer.receive()) != null){
                        salir=proccessMessage(message);
                        if(salir==0){
                            i=0;
                        }else{
                            i=2000;
                        }
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
        producerTopicQueue ptq=new producerTopicQueue();
        if (message instanceof TextMessage) {
            final TextMessage textMessage = (TextMessage) message;
            String propietario = textMessage.getStringProperty("cliente");
            String topic = textMessage.getStringProperty("topic");
            int peticion=textMessage.getIntProperty("tipoPeticion");
            int servidor=textMessage.getIntProperty("servidor");           
            final String text = textMessage.getText();
            if(servidor!=0 && peticion == 0 && propietario.equals(this.client)){
               String[] topics=text.split(";");
               //System.out.println(topics[0]);
               if (topics[0].equals("queue")){
                   int op;
                   Scanner input = new Scanner(System.in);                   
                   System.out.println("Conexion Establecida\nEscoger una opcion:");
                   System.out.println("\t1.Puntajes altos");
                   System.out.println("\t2.Iniciar Juego");
                   System.out.println("\t3.Salir");
                   op=input.nextInt();
                   switch(op){
                       case 1:
                           System.out.println("Puntajes Altos");
                           
                           ptq.sendMessages("game;1", client, servidor, 1, topic);
                           break;
                       case 2:
                           System.out.println("Iniciar Juego");
                           
                           ptq.sendMessages("game;2", client, servidor, 1, topic);
                           break;
                       default:
                           System.out.println("Saliendo del juego, cerrando conexion");
                           valor=1;
                           break;
                   }
               }else{
                   //mainGame.topicQueueIP=topics[1];
                   //mainGame.topicQueueName=topics[2];
                   //valor=valor+100;
               }
            }            
            //totalConsumedMessages++;
            System.out.println(propietario+" = Procesa text: "+text);
        }
        return valor;
    }
}
