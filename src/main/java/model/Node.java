/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.BAD_REQUEST;
import static org.eclipse.californium.core.coap.CoAP.ResponseCode.CHANGED;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.TEXT_PLAIN;
import org.json.JSONArray;

/**
 *
 * @author asus
 */
public class Node extends CoapServer {

    private final int k = 10;
    private final int alpha = 3;
    private static int COAP_PORT = 5683;
    public static int postCount = 0;
    public static int getCount = 0;
    public static int pingCount = 0;
    public static int findNodeCount = 0;
    public static int subscribeCount = 0;
    public static long totalByteReceived = 0;
    
    
    public int thisPostCount = 0;
    public int thisGetCount = 0;
    public long byteReceived = 0;
    
    NodeInfo nodeInfo = null;
    NodeData data = null;
    KBucket kBuckets = null;
    ConcurrentHashMap kvs = null;

    public int getCoapPort() {
        return COAP_PORT;
    }

    public KBucket getKBuckets() {
        return kBuckets;
    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public long getByteReceived() {
        return byteReceived;
    }
    public int getThisPostCount() {
        return thisPostCount;
    }
    public int getThisGetCount() {
        return thisGetCount;
    }
    
    public NodeData getData() {
        return data;
    }

    public ConcurrentHashMap getKvs() {
        return kvs;
    }
    //public String

    public boolean isKeyExist(String key) {
        return kvs.containsKey(key);
    }

    public boolean isPayloadValid(String payload) {
        return !(payload.equals("-1"));
    }

    public boolean isContaining(ArrayList<NodeInfo> nodeInfos, NodeInfo nodeInfo) {
        for (NodeInfo n : nodeInfos) {
            if (n.getDhtId().equals(nodeInfo.getDhtId())) {
                return true;
            }
        }
        return false;
    }

    public boolean isContaining(ArrayList<NodeInfo> nodeInfos, String id) {
        for (NodeInfo n : nodeInfos) {
            if (n.getDhtId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    //Utility
    public ArrayList<NodeInfo> trimArray(ArrayList<NodeInfo> array, int n) {
        ArrayList<NodeInfo> result = new ArrayList<>();
        for (int i = 0; i < n && i < array.size(); i++) {
            result.add(array.get(i));
        }
        return result;
    }
    public void removeFrom(ArrayList<NodeInfo> nodeInfos, String address) {
        nodeInfos.removeIf(n->n.getAddress().equals(address));
    }
    public void sortNodeInfos(ArrayList<NodeInfo> nodeInfos, String key) {
        Collections.sort(nodeInfos, (a, b) -> distance(key, a) < distance(key, b) ? -1 : distance(key, a) == distance(key, b) ? 0 : 1);
    }
    public long distance(NodeInfo node1, NodeInfo node2) {
        return Long.parseLong(node1.getDhtId()) ^ Long.parseLong(node2.getDhtId());
    }

    public long distance(String id, NodeInfo node) {
        return Long.parseLong(id) ^ Long.parseLong(node.getDhtId());
    }

    public long distance(String id, String id2) {
        return Long.parseLong(id) ^ Long.parseLong(id2);
    }

    public ArrayList<NodeInfo> getAll(){
        ArrayList allBucket = new ArrayList<>();
        kBuckets.getBuckets().forEach((bucket) -> {
            allBucket.addAll(bucket.getLedger());
        });
        sortNodeInfos(allBucket, this.nodeInfo.getDhtId());
        return allBucket;
    }
    
    public void addEndpoints() {
//        for (InetAddress addr : EndpointManager.getEndpointManager().getNetworkInterfaces()) {
//            //System.out.println("address: " + addr.getHostAddress());
//             only binds to IPv4 addresses and localhost
//            if (addr instanceof Inet4Address || addr.isLoopbackAddress()) {
//                InetSocketAddress bindToAddress = new InetSocketAddress(addr, COAP_PORT);
//                //System.out.println("TIS" + bindToAddress.toString());
//                addEndpoint(new CoapEndpoint(bindToAddress));
//            }
//        }
    }

    //CONSTRUCTORS
    public Node() throws SocketException, URISyntaxException, UnknownHostException {
        this("XX003", COAP_PORT);
    }

    public Node(final int port) throws SocketException, URISyntaxException, UnknownHostException {
        this("XX003", port);
    }

    public Node(String id, final int port) throws SocketException, URISyntaxException, UnknownHostException {
        this(id, port, "None");
    }

    public Node(String id, final int port, String bootstrap) throws SocketException, URISyntaxException, UnknownHostException {
        super(port);
        COAP_PORT = port;
        InetAddress inetAddress = InetAddress.getLocalHost();
        System.out.println("IP Address:- " + inetAddress.getHostAddress());
        System.out.println("Host Name:- " + inetAddress.getHostName());
        this.nodeInfo = new NodeInfo(id, inetAddress.getHostAddress() + ":" + COAP_PORT);
        this.data = new NodeData();
        this.kBuckets = new KBucket();
        this.kvs = new ConcurrentHashMap();
        add(new DataResource());
        add(new DHTResource());
        //ADD JOIN NETWORK
//        this.addEndpoints();
//        this.start();
        if (!bootstrap.equals("None")) {
            this.sendPing(bootstrap);
            for (NodeInfo n : this.nodeLookup(this.getNodeInfo().getDhtId())) {
                //System.out.println("testping " + n.getAddress());
                this.sendPing(n.getAddress());
            };
        }
    }

    public String pushData(JSONObject obj) throws URISyntaxException {
        String response = this.data.push(obj.get("id").toString(),
                obj.get("key").toString(),
                obj.get("timestamp").toString(),
                obj.get("value").toString());
        System.out.println(response);
        System.out.println(obj.toString(4));
        if (response.equals("new") && !obj.has("soft")){
            long temp = obj.get("key").toString().hashCode() + 2147483647;
            ArrayList<NodeInfo> candidate = nodeLookup(Long.toString(temp));
            System.out.println("Default Subscribe " + obj.get("key").toString() + " to:");
            //trimArray(candidate, alpha);
            for (NodeInfo n : candidate){
                this.getData().subscribe(obj.get("key").toString(), n.getAddress());
                System.out.println("    " + n.getAddress() + " " + n.getDhtId() + " " + Long.toString(distance(Long.toString(temp), n)));
            }
        } else {
            obj.put("soft", true);
            ExecutorService executor = Executors.newFixedThreadPool(5);
            for (String destination : this.data.getSubscriber(obj.get("key").toString())) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            coapPost("coap://" + destination + "/data", obj.toString());
                        } catch (URISyntaxException ex) {
                            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
            }
            executor.shutdown();
        }
        return response;
    }

    /* **********************************************************************/
    //KADEMLIA RPC SEND PROTOCOL
    /* **********************************************************************/
    //COAP
    public String coapGet(String target) throws URISyntaxException {
        String response;
        URI uri = new URI(target);
        CoapClient client = new CoapClient(uri);
        long start = System.nanoTime();
        CoapResponse coapResponse = client.get();
        long elapsedTime = System.nanoTime() - start;
        //System.out.println("get " + elapsedTime + " ns");
        if (coapResponse != null) {
            response = Utils.prettyPrint(coapResponse);
        } else {
            if (target.split("/",0).length > 2){
                kBuckets.removeAddress(target.split("/",0)[2]);
            }
            response = "No response received.";
            kBuckets.removeAddress(target.split("/",0)[2]);
        }
        getCount++;
        thisGetCount++;
        //System.out.println("GET " + getCount);
        byteReceived += coapResponse.advanced().getPayloadSize();
        totalByteReceived += coapResponse.advanced().getPayloadSize();
        //System.out.println("byte " + byteReceived);
        return response;
    }
    
    public String coapPost(String target, String payload) throws URISyntaxException {
        URI uri = new URI(target); // URI parameter of the request
        CoapClient client = new CoapClient(uri);
        long start = System.nanoTime();
        CoapResponse coapResponse = client.post(payload, 0);
        long elapsedTime = System.nanoTime() - start;
        //System.out.println("post " + payload + " : " + elapsedTime + " ns");
        postCount++;
        thisPostCount++;
        //System.out.println("POST " + postCount);
        if (coapResponse != null) {
            byteReceived += coapResponse.advanced().getPayloadSize();
            totalByteReceived += coapResponse.advanced().getPayloadSize();
            //System.out.println("byte " + byteReceived);
            return new String(coapResponse.getPayload());
        } else {
            if (target.split("/",0).length > 2){
                kBuckets.removeAddress(target.split("/",0)[2]);
                data.removeSubscribe(target.split("/",0)[2]);
            }
            return "-1";
        }
    }
    
    public String sendPing(String destination) throws URISyntaxException {
        String response = "";
        if (this.getNodeInfo().getAddress().equals(destination)) {
            response = "Self ping is prohibited";
        } else {
            String payload = "{ \"type\":\"ping\",\"id\":\"" + this.getNodeInfo().getId()
                    + "\",\"dhtId\":\"" + this.getNodeInfo().getDhtId()
                    + "\",\"address\":\"" + this.getNodeInfo().getAddress() + "\"}";
            //System.out.println("PING TO " + destination);
            response = coapPost("coap://" + destination + "/dht", payload);
            if (isPayloadValid(response) && !response.equals("Self ping is prohibited")) {
                JSONObject obj = new JSONObject(response);
                NodeInfo n = new NodeInfo(obj.get("id").toString(), obj.get("address").toString());
                kBuckets.update(n);
            };
        }
        pingCount++;
        //System.out.println("Ping " + pingCount);
        return response;
    }

    public String sendStore(String destination, String key, String value) throws URISyntaxException {
        String payload = "{ \"type\":\"store\",\"key\":\"" + key
                + "\",\"value\":\"" + value + "\"}";
        return coapPost("coap://" + destination + "/dht", payload);
    }

    public String sendFindNode(String destination, String key) throws URISyntaxException {
        String payload = "{ \"type\":\"findNode\",\"key\":\"" + key + "\"}";
        findNodeCount++;
        //System.out.println("FindNode " + findNodeCount);
        return coapPost("coap://" + destination + "/dht", payload);
    }

    public String sendFindValue(String destination, String key) throws URISyntaxException {
        String payload = "{ \"type\":\"findValue\",\"key\":\"" + key + "\"}";
        return coapPost("coap://" + destination + "/dht", payload);
    }

    public String sendSubscribe(String destination, String address, String key) throws URISyntaxException {
        String payload = "{ \"type\":\"subscribe\",\"key\":\"" + key
                + "\",\"address\":\"" + address + "\"}";
        subscribeCount++;
        //System.out.println("SUB " + subscribeCount);
        return coapPost("coap://" + destination + "/dht", payload);
    }

    //KADEMLIA ALGORITHM
    public ArrayList<NodeInfo> nodeLookup(String key) throws URISyntaxException { //GET K closest from network
        //long startTime = System.nanoTime();

        //System.out.println("START NODELOOKUP");
        ArrayList<NodeInfo> shortlist = kBuckets.getClosest(key, alpha);
        ArrayList<NodeInfo> newNodes = new ArrayList<>();
        ArrayList<NodeInfo> iterate = new ArrayList<>();
        ArrayList<NodeInfo> probed = new ArrayList<>();
        if (shortlist.size() > 0) {
            NodeInfo closestNode = shortlist.get(0);
            do {
                closestNode = shortlist.get(0);
                newNodes.clear();
                removeFrom(shortlist, this.nodeInfo.getAddress());
                //System.out.println("+" + this.nodeInfo.getAddress());
                iterate = trimArray(shortlist, alpha);
//                for(NodeInfo it : iterate){
//                    System.out.println(" " + it.getAddress());
//                }
                //ExecutorService executor = Executors.newFixedThreadPool(5);
                for (NodeInfo n : iterate) {
                    //System.out.println("SFN " + n.getAddress());
                    try {
                        String response = sendFindNode(n.getAddress(), key);
                        if (isPayloadValid(response)) {
                            probed.add(n);
                            JSONArray arr = new JSONArray(response);
                            for (int i = 0; i < arr.length(); i++) {
                                if (!isContaining(shortlist, arr.getJSONObject(i).get("dhtId").toString()) 
                                        && !isContaining(newNodes, arr.getJSONObject(i).get("dhtId").toString())
                                        && !arr.getJSONObject(i).get("address").toString().equals(this.nodeInfo.getAddress())
                                        ) {
                                    NodeInfo temp = new NodeInfo(arr.getJSONObject(i).get("id").toString(), arr.getJSONObject(i).get("address").toString());
                                    newNodes.add(temp);
                                }
                            }
                        } else {
                            Node.this.getKBuckets().removeNode(n);
                        }
                    } catch (URISyntaxException ex) {
                        Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                shortlist.addAll(newNodes);
                sortNodeInfos(shortlist, key);
                shortlist = trimArray(shortlist, k);
//                for (NodeInfo n : shortlist) {
//                    System.out.println("            " + n.getJson() + distance(key,n));
//                }
                //System.out.println(distance(key, shortlist.get(0)) + " VS " + distance(key, closestNode));
            } while (probed.size() < k && distance(key, closestNode) != 0);
        }
        //System.out.println("NODE LOOKUP RESULT ");
//        for (NodeInfo n : shortlist) {
//            System.out.println("    " + n.getJson());
//        }

//        long stopTime = System.nanoTime();
//        long elapsedTime = stopTime - startTime;
//        System.out.println("TIME : " + elapsedTime);

        return shortlist;
    }



    public void iterativeStore(String key) throws URISyntaxException {
        //System.out.println("store " + key + " " + key.hashCode());
        if (data.hasKey(key)) {
            for (NodeInfo n : nodeLookup(Long.toString(key.hashCode()))) {
                sendStore(n.getAddress(), Long.toString(key.hashCode()), this.getNodeInfo().getAddress());
            }
        }
    }

    public void iterativeFind() {

    }

    public String subscribe(String key) throws URISyntaxException {
        long temp = key.hashCode() + 2147483647;
        for (NodeInfo data : this.getAll()) {
            if (this.isPayloadValid(this.sendSubscribe(data.getAddress(), this.getNodeInfo().getAddress(), key))) {
                return "subscribed";
            }
        }
        System.out.println("Not Found in bucket");
        ArrayList<NodeInfo> candidate = nodeLookup(Long.toString(temp));
//        for (NodeInfo data : candidate) {
//            System.out.println(data.getJson());
//        }
        for (NodeInfo data : candidate) {
            if (this.isPayloadValid(this.sendSubscribe(data.getAddress(), this.getNodeInfo().getAddress(), key))) {
                return "subscribed";
            }
        }
        return "-1";
    }

    /**
     * **********************************************************************
     */
    //KADEMLIA RPC RECEIVE PROTOCOLS
    /**
     * **********************************************************************
     */
    public String ping(NodeInfo n) {
        if (n.getAddress().equals(this.nodeInfo.getAddress())) {
            return "Self ping is prohibited";
        } else {
            //System.out.println(this.nodeInfo.getId() + " PINGED BY" + n.getId());
            kBuckets.update(n);
            return this.nodeInfo.getJson();
        }
    }

    public String store(String key, String value) {
        kvs.put(key, value);
        String response = "<k,v> : " + key + "," + value + " stored";
        return response;
    }

    public ArrayList<NodeInfo> findNode(String key) { //XORs
        ArrayList<NodeInfo> nodes = this.kBuckets.getClosest(key, k);
        return nodes;
    }

    public String findValue(String key) {
        return kvs.get(key).toString();
    }

    //RESOURCES
    class DataResource extends CoapResource {

        public DataResource() {
            super("data");
            getAttributes().setTitle("Data Resource");
        }

        @Override
        public void handleGET(CoapExchange exchange) {
            exchange.respond(Node.this.data.getJson());
        }

        @Override
        public void handlePOST(CoapExchange exchange) {
            if (exchange.getRequestOptions().hasContentFormat()) {
                JSONObject obj = new JSONObject(exchange.getRequestText());
                //System.out.println("JSON: " + obj.toString());
                String response = null;
                try {
                    response = Node.this.pushData(obj);
                } catch (URISyntaxException ex) {
                    Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
                }
                exchange.respond(CHANGED, response, TEXT_PLAIN);
            } else {
                exchange.respond(BAD_REQUEST, "Content-Format not set");
            }
        }
    }

    class DHTResource extends CoapResource {

        public DHTResource() {
            super("dht");
            getAttributes().setTitle("DHT Resource");
        }

        @Override
        public void handleGET(CoapExchange exchange) {
            exchange.respond(Node.this.nodeInfo.getAddress() + " " + Node.this.nodeInfo.getDhtId());
        }

        @Override
        public void handlePOST(CoapExchange exchange) {
            if (exchange.getRequestOptions().hasContentFormat()) {
                JSONObject obj = new JSONObject(exchange.getRequestText());
                //System.out.println("DHT POST JSON: " + obj.toString());
                String response;
                switch (obj.get("type").toString()) {
                    case "ping":
                        NodeInfo n = new NodeInfo(obj.get("id").toString(), obj.get("address").toString());
                        response = ping(n);
                        exchange.respond(CHANGED, response, TEXT_PLAIN);
                        break;
                    case "store":
                        response = store(obj.get("key").toString(), obj.get("value").toString());
                        exchange.respond(CHANGED, response, TEXT_PLAIN);
                        break;
                    case "findNode":
                        String keyn = obj.get("key").toString();
                        final StringBuilder json = new StringBuilder();
                        StringJoiner sj = new StringJoiner(",", "[", "]");
                        for (NodeInfo nodeinfo : findNode(keyn)) {
                            json.append(nodeinfo.getJson());
                            sj.add(json.toString());
                            json.setLength(0);
                        }
                        ;
                        response = sj.toString();
                        exchange.respond(CHANGED, response, TEXT_PLAIN);
                        break;
                    case "findValue":
                        String keyv = obj.get("key").toString();
                        if (kvs.containsKey(keyv)) {
                            findValue(keyv);
                        } else {
                            findNode(keyv);
                        }
                        exchange.respond(CHANGED, exchange.getRequestText(), TEXT_PLAIN);
                        break;
                    case "subscribe":
                        String key = obj.get("key").toString();
                        if (Node.this.getData().hasKey(key)) {
                            response = Node.this.getData().subscribe(key, obj.get("address").toString());
                        } else {
                            response = "-1";
                        }
                        exchange.respond(CHANGED, response, TEXT_PLAIN);
                        break;
                    default:
                        break;
                }
            } else {
                exchange.respond(BAD_REQUEST, "Content-Format not set");
            }
        }
    }

}
