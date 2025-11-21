import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class BaseServer {
    private BaseServer() {

    }

    public static HttpServer makeServer() throws IOException {
        String host = "localhost";
        InetSocketAddress address = new InetSocketAddress(host,8089);

        System.out.printf("server started at http://%s:%s.%n",address.getHostName(),address.getPort());

        HttpServer server = HttpServer.create(address,50);
        System.out.println("    Done!");
        return server;
    }
}
