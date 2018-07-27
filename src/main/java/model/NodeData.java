/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.ArrayList;
import java.util.StringJoiner;

/**
 *
 * @author asus
 */
public class NodeData {
    ArrayList<DataEntry> entries;
    private ArrayList<KeySubscribe> keys;
    
    public NodeData(){
        entries = new ArrayList<>();
        keys = new ArrayList<>();
    }
    
    public ArrayList<DataEntry> getEntries() {
        return entries;
    }

    public String addKey(String key){
        keys.add(new KeySubscribe(key));
        return "addKey";
    }
    
    public ArrayList<String> getKeys() {
        ArrayList<String> temp = new ArrayList<>();
        for(KeySubscribe ks : this.keys){
            temp.add(ks.key);
        }
        return temp;
    }
    
    public ArrayList<String> getSubscriber(String key){
        ArrayList<String> temp = new ArrayList<>();
        for(KeySubscribe ks : this.keys){
            if (ks.key.equals(key)){
                temp = ks.getAddress();
            }
        }
        return temp;
    }
    
    public ArrayList<DataEntry> getEntries(String key) {
        ArrayList<DataEntry> filteredEntries = new ArrayList<>();
        entries.forEach((DataEntry entry) -> {
            if (entry.getKey().equals(key)){
                filteredEntries.add(entry);
            }}
        );
        return filteredEntries;
    }
    
    public boolean hasKey(String key){
        return this.getKeys().contains(key);
    }
    
    public String push(String id, String key, String timestamp, String value){
        entries.add(new DataEntry(id,key,timestamp,value));
        if(!this.getKeys().contains(key)){
            this.addKey(key);
            return "new";
        } else {
            return "pushed";
        }
    }
    
    public String subscribe(String key, String address){
        String response = "-1";
        for(KeySubscribe ks : this.keys){
            if (ks.key.equals(key) && !ks.address.contains(address)){
                ks.address.add(address);
            }
            response = "subscribed";
        }
        return response;
    }
    
    public String getJson(){
        return getJson("");
    }
    
    public String getJson(String key){
        final StringBuilder json = new StringBuilder();
        StringJoiner sj = new StringJoiner(",","[","]");
        entries.forEach((DataEntry entry) -> {
            if (key.equals("") || entry.getKey().equals(key)){
                json.append("{\"id\":\"").append(entry.getId())
                        .append("\",\"key\":\"").append(entry.getKey())
                        .append("\",\"timestamp\":\"").append(entry.getTimestamp())
                        .append("\",\"value\":\"").append(entry.getValue())
                        .append("\"}");    
                sj.add(json.toString());
                json.setLength(0);
            }
        });
        return sj.toString();
    }
}
