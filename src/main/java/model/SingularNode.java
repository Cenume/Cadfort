/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

/**
 *
 * @author ASUS
 */
public class SingularNode {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SocketException, URISyntaxException, UnknownHostException {
        // TODO code application logic here
        if (args.length == 3){
            String name = args[0];
            String port = args[1];
            String bootstrap = args[2];
            Node node = new Node(name, Integer.parseInt(port), bootstrap);
            node.addEndpoints();
            node.start();
            System.out.println("LALLALA");
        } else if (args.length == 2){
            String name = args[0];
            String port = args[1];
            Node node = new Node(name, Integer.parseInt(port));
            node.addEndpoints();
            node.start();
            System.out.println("LALLALddddA");
        } else {
            System.out.println("INVALID COMMAND");
        }
    }
    
}
