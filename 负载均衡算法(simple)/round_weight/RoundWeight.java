package loadAl.round_weight;

import loadAl.random_weight.Server;

import java.util.Map;

/**
 * @author: Victor
 * @create: 2020-02-07 22:07
 * 加权轮询
 **/

public class RoundWeight {
    static Server server = new Server();
    static int index;

    public static String go() {
        int sum_weight = server.map.values().stream().mapToInt((a -> a)).sum();
        int number = (index++) % sum_weight;
        for (Map.Entry<String,Integer> item : server.map.entrySet()) {
            if (item.getValue() > number) {
                return item.getKey();
            }
            number -= item.getValue();
        }
        return "";
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            System.out.println(go());
        }
    }

}
