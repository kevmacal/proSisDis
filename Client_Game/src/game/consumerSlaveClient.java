/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    private static int puntaje = 0;
    threadGame r1;
    Thread t1;
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
        int salir=0;
        int i=0;
        QueueBrowser queuePeek;
        MessageConsumer consumer;
        while (salir==0) {
            if(i==1000){
                i=0;
            }
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
        int i;
        Scanner input = new Scanner(System.in); 
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
                           r1=new threadGame(client,servidor,topic);

                           t1=new Thread(r1);

                           t1.start();

                           ptq.sendMessages("game;2", client, servidor, 2, topic);
                           break;
                       default:
                           System.out.println("Saliendo del juego, cerrando conexion");
                           ptq.sendMessages("close;CerrarConexion", client, servidor, 6, topic); //Peticion 6 cerrar la conexion
                           valor=1;
                           break;
                   }
               }               
               if (topics[0].equals("hs")){
                   puntaje=0;
                   for(i=1;i<topics.length;i++){
                       System.out.println(i+".- "+topics[i]);
                   }
                   System.out.println("Presionar enter para continuar...");
                   input.nextLine();
                   ptq.sendMessages("queue;Continuar", client, servidor, 0, topic);
               }
               if (topics[0].equals("endGame")){
                   System.out.println("El tiempo se ha terminado\n\n\tTu puntaje final: "+puntaje+" respuestas correctas");
                   ptq.sendMessages("game;4;"+puntaje, client, servidor, 4, topic);
               }
               if(topics[0].equals("game")){
                   int answ;
                   int correct=0;
                   ArrayList<String> respuestasOrd=new ArrayList<>();
                   ArrayList<String> respuestas=new ArrayList<>();
                   System.out.println(topics[1]); //Pregunta
                   for(i=2;i<topics.length;i++){
                       respuestasOrd.add(topics[i]);
                   }
                   Integer[] arr = new Integer[respuestasOrd.size()];
                   for (int j = 0; j < arr.length; j++) {
                       arr[j] = j;
                   }
                   Collections.shuffle(Arrays.asList(arr));
                   answ=1;
                   for (Integer arr1 : arr) {
                       respuestas.add(respuestasOrd.get(arr1));
                       if(arr1==0){
                           correct=answ;
                       }
                       answ++;
                   }
                   for(i=0;i<respuestasOrd.size();i++){
                       System.out.println((i+1)+respuestas.get(i));
                   }
                   System.out.println("Seleccionar su respuesta: ");
                   answ=input.nextInt();
                   if(answ==correct && r1.getOnTime()==1){
                       System.out.println("Respuesta correcta");
                       puntaje++;
                       ptq.sendMessages("game;3", client, servidor, 3, topic); //Peticion 3 es otra pregunta
                   }else{
                       r1.setStop(1);
                       if(answ!=correct){
                        System.out.println("La respuesta es incorrecta, gracias por jugar");
                        System.out.println("La respuesta correcta es: "+respuestasOrd.get(0));
                        System.out.println("\t\tPuedes intentarlo de nuevo en este topico");                        
                        ptq.sendMessages("game;4;"+puntaje, client, servidor, 4, topic);
                       }else{
                           System.out.println("Respuesta fuera del tiempo, gracias por jugar");
                           System.out.println("La respuesta correcta es: "+respuestasOrd.get(0));
                           System.out.println("\t\tPuedes intentarlo de nuevo en este topico");
                       }
                   }                   
               }
            }            
            //totalConsumedMessages++;
            System.out.println(propietario+" = Procesa text: "+text);
        }
        return valor;
    }
}
