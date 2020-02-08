package loadAl.random_weight;

import java.util.Map;
import java.util.Random;

/**
 * @author: Victor
 * @create: 2020-02-07 21:40
 * 随机加权算法
 **/

public class RandomWeight {
    static Server server = new Server();
    static Random random = new Random();

    public static String go() {
        int weight_sum = server.map.values().stream().mapToInt(a -> a).sum();
        int number = random.nextInt(weight_sum);
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
