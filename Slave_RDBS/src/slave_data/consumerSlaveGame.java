/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package slave_data;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import javax.jms.*;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 *
 * @author marlon
 */
public class consumerSlaveGame {
    private static final String URL = "tcp://192.168.1.3:61616"; //Corresponde al servidor, se asigna manualmente
    private final int numServer = 1; //Este numero se asigna manualmente
    private static final String USER = ActiveMQConnection.DEFAULT_USER; 
    private static final String PASSWORD = ActiveMQConnection.DEFAULT_PASSWORD;
    private static final boolean TRANSACTED_SESSION = false; 
    private static final String USERNAME="root";
    private static final String PASSWORDBD="0984287897";
    private static final String CONN_STRING="jdbc:mysql://localhost:3306/prueba"; //Ambiente de pruebas
    private String queue;
    public LinkedList<Integer> preguntas = new LinkedList();
    private int game;
    public void processMessages(String queue) throws JMSException, InterruptedException {
 
        final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(USER, PASSWORD, URL);
        final javax.jms.Connection connection = connectionFactory.createConnection();
 
        connection.start();
        this.queue=queue;
        this.game=1;
        final Session session = connection.createSession(TRANSACTED_SESSION, Session.AUTO_ACKNOWLEDGE);
        //final Destination destination = session.createQueue(DESTINATION_QUEUE);
        
 
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
            Thread.sleep(70);
            //System.out.println(i);
            queuePeek = session.createBrowser(session.createQueue(queue));
            Enumeration<?> messagesInQueue = queuePeek.getEnumeration();
            Message peek = (Message) messagesInQueue.nextElement();
            if (peek!=null){
                if (confirmMessage(peek)==1){
                    consumer= session.createConsumer(session.createQueue(queue));
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
    private int proccessMessage(Message message) throws JMSException, InterruptedException {
        producerTopicQueue ptq=new producerTopicQueue();
        int i;
        int valor=0;
        String concat="";
        if (message instanceof TextMessage) {
            final TextMessage textMessage = (TextMessage) message;
            String propietario = textMessage.getStringProperty("cliente");
            int peticion=textMessage.getIntProperty("tipoPeticion");
            int servidor=textMessage.getIntProperty("servidor");
            String topic = textMessage.getStringProperty("topic");
            final String text = textMessage.getText();
            java.sql.Connection con=null;
            switch(peticion){
                case 1:
                    concat=concat+"hs;";
                    for(i=0;i<mainSlave.hScores.get(topic).size();i++){
                        concat=concat+mainSlave.hScores.get(topic).get(i)+";";
                    }
                    ptq.sendMessages(queue, concat, propietario, servidor, 0, topic);
                    break;
                case 2:                    
                    //System.out.println("Juego");
                    game=1;
                    concat=concat+"game;";
                    ArrayList<Integer> indices=new ArrayList();
                    try{
                        con = DriverManager.getConnection(CONN_STRING, USERNAME, PASSWORDBD);
                        String query = "select * from Topico T inner join Preguntas P on T.ID_Topico=P.ID_Topico where T.nombre='"+topic+"'";
                        Statement st=(Statement) con.createStatement();

                        ResultSet rs = st.executeQuery(query);
                        while (rs.next()){
                            int id_pregunta=rs.getInt("ID_Preguntas");
                            indices.add(id_pregunta);
                            //System.out.println(id_pregunta);
                        }
                        /*Desordena el orden de las preguntas para que sean aleatorias*/
                        Integer[] arr = new Integer[indices.size()];
                        for (int j = 0; j < arr.length; j++) {
                            arr[j] = j;
                        }
                        Collections.shuffle(Arrays.asList(arr));
                        for (Integer arr1 : arr) {
                            preguntas.add(indices.get(arr1));
                        }
                        int idFirst=preguntas.removeFirst();
                        preguntas.add(idFirst);
                        
                        query = "SELECT * FROM Preguntas WHERE ID_Preguntas = "+idFirst;
                        
                        rs = st.executeQuery(query);
                        while (rs.next()){
                            String pregunta=rs.getString("Pregunta");
                            String opc1=rs.getString("opc1");
                            String opc2=rs.getString("opc2");
                            String opc3=rs.getString("opc3");
                            String opc4=rs.getString("opc4");
                            concat=concat+pregunta+";"+opc1+";"+opc2+";"+opc3+";"+opc4;
                        }
                        ptq.sendMessages(queue, concat, propietario, servidor, 0, topic);
                        //preguntas.stream().forEach((preg) -> {
                            //System.out.println(preg);
                        //});
                        //System.out.println(Arrays.toString(arr));
                    }catch (SQLException e){
                        System.err.println("Got an exception! ");
                        System.err.println(e.getMessage());
                    }
                     break;
                case 3:
                    if(game!=0){
                        concat=concat+"game;";
                        int idFirst=preguntas.removeFirst();
                        preguntas.add(idFirst);
                        try{                        
                            con = DriverManager.getConnection(CONN_STRING, USERNAME, PASSWORDBD);
                            String query = "SELECT * FROM Preguntas WHERE ID_Preguntas = "+idFirst;
                            Statement st=(Statement) con.createStatement();

                            ResultSet rs = st.executeQuery(query);                    

                            while (rs.next()){
                                String pregunta=rs.getString("Pregunta");
                                String opc1=rs.getString("opc1");
                                String opc2=rs.getString("opc2");
                                String opc3=rs.getString("opc3");
                                String opc4=rs.getString("opc4");
                                concat=concat+pregunta+";"+opc1+";"+opc2+";"+opc3+";"+opc4;
                            }
                            ptq.sendMessages(queue, concat, propietario, servidor, 0, topic);
                            //preguntas.stream().forEach((preg) -> {
                                //System.out.println(preg);
                            //});
                            //System.out.println(Arrays.toString(arr));
                        }catch (SQLException e){
                            System.err.println("Got an exception! ");
                            System.err.println(e.getMessage());
                        }
                    }else{
                        game=1;
                    }
                    break;
                case 4:
                    String[] topics=text.split(";");
                    String tmpProp=propietario;
                    String tmpPunt=topics[2];
                    while(isUsedGlobal()==1){
                        Thread.sleep(100);
                    }
                    concat=concat+"hs;";
                    for(i=0;i<mainSlave.hScores.get(topic).size();i++){
                        String puntaje=mainSlave.hScores.get(topic).get(i);
                        String[] up=puntaje.split("-");
                        if(Integer.parseInt(tmpPunt)>Integer.parseInt(up[1])){                                
                            mainSlave.hScores.get(topic).set(i, tmpProp+"-"+tmpPunt);
                            tmpProp=up[0];
                            tmpPunt=up[1];
                        }
                        concat=concat+mainSlave.hScores.get(topic).get(i)+";";
                    }
                    mainSlave.inUseGlobal=0;
                    
                    ptq.sendMessages(queue, concat, propietario, servidor, 0, topic);
                    break;
                case 5:
                    game=0;
                    break;
                case 6:
                    valor=1;
                    break;
                default:
                    System.out.println("No es peticion, es respuesta");
                    break;
            }
            System.out.println(propietario+" = Procesa text: "+text);
        }
        return valor;
    }
    private int isUsedGlobal(){
        int valor=mainSlave.inUseGlobal;
        mainSlave.inUseGlobal=1;
        return valor;
    }
}
