/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import javax.jms.*;
import java.util.Scanner;
/**
 *
 * @author marlon
 */
public class mainGame {
    private static final String MASTER_QUEUE = "MasterClient.Queue"; 
    public static void main(String[] args)  throws JMSException {
        producerClient pc=new producerClient();
        Scanner input = new Scanner(System.in);
        System.out.println("Ingresar usuario con el que sera conocido en el juego: ");
        
        String clientName = input.nextLine();
        System.out.println(clientName);
        pc.sendMessages(MASTER_QUEUE, "Usuario Creado, Solicita Topicos", clientName, 0, 1); //Si el servidor va con cero, es para el master (Es del cliente)
    }
}
