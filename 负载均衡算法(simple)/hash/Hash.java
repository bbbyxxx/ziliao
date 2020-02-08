package loadAl.hash;


import loadAl.random.Server;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author: Victor
 * @create: 2020-02-07 22:20
 * 哈希算法
 **/

public class Hash {
    public static String go(String client) {
        int count = 20;
        TreeMap<Integer,String> treeMap = new TreeMap<Integer,String>();
        for (String s : new Server().list) {
            for (int i = 0; i < count; i++) {
                treeMap.put((s + "--服务器--" + i).hashCode() , s);
            }
        }
        int clientHash = client.hashCode();
        SortedMap<Integer,String> sortedMap = treeMap.tailMap(clientHash);
        Integer firstHash;
        if (sortedMap.size() > 0) {
            firstHash = sortedMap.firstKey();
        }else {
            firstHash = treeMap.firstKey();
        }
        String s = treeMap.get(firstHash);
        return s;
    }

    public static void main(String[] args) {
        System.out.println(go("为啥要吃蝙蝠？"));
        System.out.println(go("为什么"));
        System.out.println(go("0"));
        System.out.println(go("-110000"));
        System.out.println(go("风雨交加"));
    }
}
