/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author ASUS
 */
public class KBucket {

    int k = 0;
    ArrayList<Bucket> buckets;

    public KBucket() {
        this.buckets = new ArrayList<>();
        buckets.add(new Bucket());
    }
    
    public void removeNode(NodeInfo nodeinfo){
        for(Bucket b : this.buckets){
            b.remove(nodeinfo);
        }
    }
    
    public void removeAddress(String address){
        for(Bucket b : this.buckets){
            b.remove(address);
        }
    }
    
    public boolean isFull() {
        return (buckets.get(0).getLedger().size() >= k);
    }

    public boolean update(NodeInfo nodeinfo) {
        buckets.get(0).addKnownNode(nodeinfo);
        return true;
    }

    public ArrayList<Bucket> getBuckets() {
        return buckets;
    }

    public Bucket getBucket(int i) {
        return buckets.get(i);
    }
    
    //get n closest node from ALL buckets
    public ArrayList<NodeInfo> getClosest(String key, int n) {
        ArrayList shortlist = new ArrayList<>();
        ArrayList allBucket = new ArrayList<>();
        buckets.forEach((bucket) -> {
            allBucket.addAll(bucket.getLedger());
        });
        for (int i = n ; i > 0 && !allBucket.isEmpty() ; i--){
            NodeInfo node = getClosest(key, allBucket);
            shortlist.add(node);
            allBucket.remove(node);
        }
        return shortlist;
    }

    public NodeInfo getClosest(String key) {
        ArrayList allBucket = new ArrayList<>();
        buckets.forEach((bucket) -> {
            allBucket.addAll(bucket.getLedger());
        });
        return getClosest(key, allBucket);
    }
    
    public NodeInfo getClosest(String key, ArrayList<NodeInfo> nodes){
        NodeInfo node = null;
        long distance = 0;
        //System.out.println("        FIND CLOSEST NODE BY XOR" + nodes.toString());
        for (NodeInfo nodeInfo : nodes) {
            if ( node == null || (Long.parseLong(nodeInfo.getDhtId()) ^ Long.parseLong(key)) < distance) {
                node = nodeInfo;
                distance = Long.parseLong(nodeInfo.getDhtId()) ^ Long.parseLong(key);
                //System.out.println("        " + nodeInfo.getDhtId() + " XOR " + key + " = " + distance);
            }
        }
        //System.out.println("        CLOSEST NODE: " + node.getId());
        return node;
    }
    
}
