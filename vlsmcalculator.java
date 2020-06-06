import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class VLSM {
    public static void main(String[] args) {
        String majorNetwork = "192.168.1.0/24";
        Map<String, Integer> subnets = new HashMap<>(); 
        subnets.put("A", 50);
        subnets.put("B", 30);
        subnets.put("C", 62);
        subnets.put("D", 10);
        
        List<Subnet> output = calcVLSM(majorNetwork, subnets);
        
        for (Subnet subnet : output) {
            System.out.println(subnet.name + "\t" +
                                subnet.neededSize + "\t" +
                                subnet.allocatedSize + "\t" +
                                subnet.address + "\t" +
                                subnet.mask + "\t" +
                                subnet.decMask + "\t" +
                                subnet.range + "\t" +
                                subnet.broadcast);
        }
    }
    
   
    private static List<Subnet> calcVLSM(String majorNetwork, Map<String, Integer> subnets) {
        Map<String, Integer> sortedSubnets = sortMap(subnets);
        List<Subnet> output = new ArrayList<>();
        int currentIp = findFirstIp(majorNetwork);
        
        for (String key : sortedSubnets.keySet()) {  
            Subnet subnet = new Subnet();
            
            subnet.name = key;
            subnet.address = convertIpToQuartet(currentIp);
            
            int neededSize = sortedSubnets.get(key);
            subnet.neededSize = neededSize;
            
            int mask = calcMask(neededSize);
            subnet.mask = "/" + mask;
            subnet.decMask = toDecMask(mask);
            
            int allocatedSize = findUsableHosts(mask);
            subnet.allocatedSize = allocatedSize;
            subnet.broadcast = convertIpToQuartet(currentIp + allocatedSize + 1);
            
            String firstUsableHost = convertIpToQuartet(currentIp + 1);
            String lastUsableHost = convertIpToQuartet(currentIp + allocatedSize);
            subnet.range = firstUsableHost + " - " + lastUsableHost;
            
            output.add(subnet);
            
            currentIp += allocatedSize + 2;
        }
        
        return output;
    }
    
    
    private static Map<String, Integer> sortMap(Map<String, Integer> map) {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(map.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());  
            }
        });
        
        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : entries) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        
        return sortedMap;
    }
    
  
    private static int convertQuartetToBinaryString(String ipAddress) {
        String[] ip = ipAddress.split("\\.|/");
        
        int octet1 = Integer.parseInt(ip[0]);
        int octet2 = Integer.parseInt(ip[1]);
        int octet3 = Integer.parseInt(ip[2]);
        int octet4 = Integer.parseInt(ip[3]);
        
        int output = octet1;
        output = (output << 8) + octet2;
        output = (output << 8) + octet3;
        output = (output << 8) + octet4;
        
        return output;
    }
    
   
    private static String convertIpToQuartet(int ipAddress) {
        int octet1 = (ipAddress >> 24) & 255;
        int octet2 = (ipAddress >> 16) & 255;
        int octet3 = (ipAddress >> 8) & 255;
        int octet4 = ipAddress & 255;
        
        return octet1 + "." + octet2 + "." + octet3 + "." + octet4;
    }
    
    
    private static int findFirstIp(String majorNetwork) {
        String[] ip = majorNetwork.split("/");
        int mask = Integer.parseInt(ip[1]); 
        int offset = Integer.SIZE - mask;
        
        int majorAddress = convertQuartetToBinaryString(majorNetwork);
        int firstIp = (majorAddress >> offset) << offset;
        
        return firstIp;
    }
    
    
    private static int calcMask(int neededSize) {
        int highestBit = Integer.highestOneBit(neededSize);
        int position = (int) (Math.log(highestBit) / Math.log(2));
        return Integer.SIZE - (position + 1);   
    }
    
   
    private static int findUsableHosts(int mask) {
        return (int) Math.pow(2, Integer.SIZE - mask) - 2;
    }
    
    
    private static String toDecMask(int mask) {
        if (mask == 0) {
            return "0.0.0.0";
        }
        int allOne = -1;    // '255.255.255.255'
        int shifted = allOne << (Integer.SIZE - mask);
        
        return convertIpToQuartet(shifted);
    }
    
    private static class Subnet {
        public String name;
        public int neededSize;
        public int allocatedSize;
        public String address;
        public String mask;
        public String decMask;
        public String range;
        public String broadcast;
    }
}