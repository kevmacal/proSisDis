/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import java.util.ArrayList;
import javax.jms.*;
import java.util.Scanner;
/**
 *
 * @author marlon
 */
public class mainGame {
    private static final String MASTER_QUEUE = "MasterClient.Queue";
    public static ArrayList<String> topicos;
    public static String topicQueueName;
    public static String topicQueueIP;
    
    public static void main(String[] args)  throws JMSException, InterruptedException {
        int retry=0;
        int op=0;
        Scanner input = new Scanner(System.in);
        System.out.println("Ingresar usuario con el que sera conocido en el juego: ");

        String clientName = input.nextLine();
        do{        
            String topicGame="No elegido";
            producerClient pc=new producerClient();
            consumerMasterClient cmc=new consumerMasterClient();            
            //System.out.println(clientName);
            pc.sendMessages(MASTER_QUEUE, "Usuario Creado, Solicita Topicos", clientName, 0, 1); //Si el servidor va con cero, es para el master (Es del cliente)
            topicos=new ArrayList<>();
            //System.out.println(topicos.size());
            cmc.processMessages(MASTER_QUEUE, clientName);
            if(!topicos.isEmpty()){
                retry=0;
                op=Menu();
                //System.out.println(op);
                if(op<topicos.size() && op>=0){
                    topicGame=topicos.get(op).split(";")[0];
                    pc.sendMessages(MASTER_QUEUE, "El usuario acepta un Topico;"+topicos.get(op).split(";")[1]+";"+topicGame, clientName, 0, 2); //Solicitud a master 2 es de aceptacion de cola
                    cmc.processMessages(MASTER_QUEUE, clientName);
                    consumerSlaveClient csc=new consumerSlaveClient();
                    System.out.println("Empezara el juego con topico: "+topicGame);
                    csc.processMessages(clientName);
                }                
            }else{
                retry++;
                if(retry<4){
                    System.out.println("Hubo un problema en la comunicacion, no se recibieron topicos de preguntas\nIntento: "+retry);
                }
            }
        }while(op>=0&&retry<4);
        //System.out.println(topicQueueIP+";"+topicQueueName);
        //System.out.println(op);
        //System.out.println(topicos.size());        
    }
    public static int Menu(){
        int i;
        Scanner input = new Scanner(System.in);
        System.out.println("\t\tSe presentan los topicos, escoger uno para empezar el juego\n");
        for(i=0;i<topicos.size();i++){
            System.out.println((i+1)+". "+topicos.get(i).split(";")[0]);
        }
        System.out.println("\n\tEscoger el numero de una opcion (Para salir ingrese 0): ");
        i=input.nextInt();
        return i-1;
    }
}
