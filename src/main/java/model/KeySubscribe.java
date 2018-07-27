/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.ArrayList;

/**
 *
 * @author asus
 */
public class KeySubscribe {
    String key;
    ArrayList<String> address;
    
    public KeySubscribe(String key){
        this.key = key;
        this.address = new ArrayList<>();
    }

    public ArrayList<String> getAddress() {
        return address;
    }
    
}
