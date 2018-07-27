/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author asus
 */
public class NodeInfo {
    private String id;
    private String dhtId;
    private String address;
    
    public NodeInfo(){
        this("");
    }
    
    public NodeInfo(String id){
        this.id = id;
        this.dhtId = "";
        this.address = "";
    }
    
    public NodeInfo(String id, String address){
        this.id = id;
        this.address = address;
        long temp = (id + address).hashCode();
        this.dhtId = Long.toString(temp+2147483647);
        //this.dhtId = id;
    }
    
    public String getId(){
        return this.id;
    }

    public String getDhtId() {
        return dhtId;
    }

    public String getAddress() {
        return address;
    }
    
    public String getJson(){
        return "{\"id\":\"" + getId() + "\",\"dhtId\":\"" + getDhtId() + "\",\"address\":\"" + getAddress() + "\"}";
    }
}
