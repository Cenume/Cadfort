/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author ASUS
 */
public class DataEntry {
    public final String id; 
    public final String key;
    public final String timestamp;
    public final String value; 

    public DataEntry(String id, String key, String timestamp, String value) {
        this.id = id;
        this.key = key;
        this.timestamp = timestamp;
        this.value = value;
    }
    public String getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getValue() {
        return value;
    }
    
}
