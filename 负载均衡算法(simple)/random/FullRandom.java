package loadAl.random;

import java.util.Random;

/**
 * @author: Victor
 * @create: 2020-02-07 21:27
 * 随机算法
 **/

public class FullRandom {
    static Server server = new Server();
    static Random random = new Random();
    static int[] array = new int[4];

    public static void go() {
        int number = random.nextInt(server.list.size());
        array[number]++;
    }
    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            go();
        }
        for (int i = 0; i < array.length; i++) {
            System.out.println("192.168.0." +(i+1) + ":" + array[i]);
        }
    }
}
