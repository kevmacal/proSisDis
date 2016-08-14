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
    
    public static void main(String[] args)  throws JMSException, InterruptedException {
        int op;
        producerClient pc=new producerClient();
        consumerMasterClient cmc=new consumerMasterClient();
        Scanner input = new Scanner(System.in);
        System.out.println("Ingresar usuario con el que sera conocido en el juego: ");
        
        String clientName = input.nextLine();
        //System.out.println(clientName);
        pc.sendMessages(MASTER_QUEUE, "Usuario Creado, Solicita Topicos", clientName, 0, 1); //Si el servidor va con cero, es para el master (Es del cliente)
        topicos=new ArrayList<>();
        System.out.println(topicos.size());
        cmc.processMessages(MASTER_QUEUE, clientName);
        op=Menu();
        if(op<topicos.size() && op>=0){
            pc.sendMessages(MASTER_QUEUE, "El usuario acepta un Topico;"+topicos.get(op).split(";")[1]+";"+topicos.get(op).split(";")[0], clientName, 0, 2); //Solicitud a master 2 es de aceptacion de cola
        }
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
        System.out.println("\n\tEscoger el numero de una opcion: ");
        i=input.nextInt();
        return i-1;
    }
}
