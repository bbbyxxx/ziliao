package loadAl.random_weight;

import java.util.HashMap;

/**
 * @author: Victor
 * @create: 2020-02-07 21:38
 **/

public class Server {
    public HashMap<String,Integer> map = new HashMap<String,Integer>(){
        {
            put("192.168.0.1",2);
            put("192.168.0.2",9);
            put("192.168.0.3",3);
            put("192.168.0.4",15);
        }
    };
}
