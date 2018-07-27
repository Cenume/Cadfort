/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringJoiner;

/**
 *
 * @author ASUS
 */
public class Bucket {
    
    ArrayList<NodeInfo> ledger;

    public Bucket() {
        this.ledger = new ArrayList<>();
    }
    
    public Bucket(NodeInfo nodeinfo) {
        this.ledger = new ArrayList<>();
        this.ledger.add(nodeinfo);
    }
    
    public void remove(NodeInfo nodeinfo){
        Iterator<NodeInfo> itr = this.ledger.iterator();
        while (itr.hasNext()) {
            if (itr.next().getAddress().equals(nodeinfo.getAddress())) {
                itr.remove();
            }
        }
    }
    
    public void remove(String address){
        Iterator<NodeInfo> itr = this.ledger.iterator();
        while (itr.hasNext()) {
            if (itr.next().getAddress().equals(address)) {
                itr.remove();
            }
        }
    }
    
    public ArrayList<NodeInfo> getLedger() {
        return this.ledger;
    }
    
    public void addKnownNode(NodeInfo n){
        boolean exist = false;
        for (NodeInfo node: this.ledger){
            if (node.getAddress().equals(n.getAddress())){ 
                System.out.println("ges aya");
                exist = true;
            }
        }
        if (!exist){
            this.ledger.add(n);
        }
    }
    
    public String getJsonLedger(){
        final StringBuilder json = new StringBuilder();
        StringJoiner sj = new StringJoiner(",","[","]");
        ledger.forEach((NodeInfo info) -> {
            json.append(info.getJson());    
            sj.add(json.toString());
            json.setLength(0);
        });
        return sj.toString();
    }
}
