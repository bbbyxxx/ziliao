package loadAl.random;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Victor
 * @create: 2020-02-07 21:26
 **/

public class Server {
    public List<String> list = new ArrayList<String>() {
        {
            add("192.168.0.1");
            add("192.168.0.2");
            add("192.168.0.3");
            add("192.168.0.4");
        }
    };
}
