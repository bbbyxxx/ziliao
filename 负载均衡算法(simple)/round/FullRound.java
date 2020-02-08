package loadAl.round;

import loadAl.random.Server;

/**
 * @author: Victor
 * @create: 2020-02-07 21:54
 * 轮询算法
 **/

public class FullRound {
    static Server server = new Server();
    static int index;

    /**
     * 1->2->3->4->1->2....
     * @return
     */
    public static String go() {
        if (index == server.list.size()) {
            index = 0;
        }
        return server.list.get(index++);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            System.out.println(go());
        }
    }
}
