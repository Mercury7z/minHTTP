import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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

    public static void initRoutes(HttpServer server) {
        server.createContext("/apps/profile",BaseServer::handleRequestProfile);
        server.createContext("/apps",BaseServer::handleRequestApplication);
        server.createContext("/",BaseServer::handleHtml);

    }

    private static BufferedReader gerReader(HttpExchange exchange) {
        InputStream inputStream = exchange.getRequestBody();
        Charset charset = StandardCharsets.UTF_8;
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream,charset);
        return new BufferedReader(inputStreamReader);
    }

    private static PrintWriter getWriterFrom(HttpExchange exchange) {
        OutputStream outputStream  = exchange.getResponseBody();
        Charset charset = StandardCharsets.UTF_8;
        return new PrintWriter(outputStream,false,charset);
    }

    private static void writeData(Writer writer,HttpExchange exchange) {
        try (BufferedReader reader = gerReader(exchange)){
            if(!reader.ready()) return;
            write(writer,"data","");
            reader.lines().forEach(v -> write(writer,"\t", v));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleHtml(HttpExchange exchange) throws IOException {

        String filePath = "files" + exchange.getRequestURI().getPath();
        if (filePath.equals("/")) {
            filePath = "/index.html";
        }
        Path path = Path.of(filePath);
        if (!Files.exists(path)) {
            error404(exchange);

        } else{
            byte[] data = Files.readAllBytes(path);
            System.out.println(exchange.getRequestURI().getPath());
            System.out.println(filePath);

            exchange.getResponseHeaders().add("Content-Type", getContentType(filePath));
            int responseCode = 200;
            exchange.sendResponseHeaders(responseCode,0);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(data);
            }
        }
    }

    private static String getContentType(String filePath) {
        if(filePath.endsWith(".html")) return "text/html;charset=UTF-8";
        if(filePath.endsWith(".css")) return "text/css;charset=UTF-8";
        if(filePath.endsWith(".js")) return "application/javascript";
        if(filePath.endsWith(".png")) return "image/png";
        if(filePath.endsWith(".jpg")) return "image/jpeg";
        if(filePath.endsWith(".jpeg")) return "image/jpeg";
        return "text/plain;charset=UTF-8";
    }

    private static void handleRequestApplication(HttpExchange exchange) {
        try {
            exchange.getResponseHeaders().add("content-Type", "text/plain;charset=UTF-8");
            int responseCode = 202;
            int length = 0;
            exchange.sendResponseHeaders(responseCode,length);
            try (PrintWriter writer = getWriterFrom(exchange)){
                String method = exchange.getRequestMethod();
                URI uri = exchange.getRequestURI();
                String ctxPath = exchange.getHttpContext().getPath();
                write(writer,"HTTP method",method);
                write(writer,"Request",uri.toString());
                write(writer,"Handler",ctxPath);
                writeHeaders(writer, "Request headers", exchange.getRequestHeaders());
                writeData(writer,exchange);
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleRequestProfile(HttpExchange exchange) {
        try {
            exchange.getResponseHeaders().add("content-Type", "text/plain;charset=UTF-8");
            int responseCode = 203;
            int length = 0;
            exchange.sendResponseHeaders(responseCode,length);
            try (PrintWriter writer = getWriterFrom(exchange)){
                String method = exchange.getRequestMethod();
                URI uri = exchange.getRequestURI();
                String ctxPath = exchange.getHttpContext().getPath();
                write(writer,"HTTP method",method);
                write(writer,"Request",uri.toString());
                write(writer,"Handler",ctxPath);
                writeHeaders(writer, "Request headers", exchange.getRequestHeaders());
                writeData(writer,exchange);
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeHeaders(PrintWriter writer, String type, Headers headers) {
        write(writer,type,"");
        headers.forEach((k,v) -> write(writer,"\t" + k ,v.toString()));
    }

    private static void write(Writer writer, String msg, String method) {
        String data = String.format("%s: %s%n%n",msg,method);
        try {
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void error404(HttpExchange exchange) {
        String msg = "Такого документа нет 404";
        try {
            exchange.getResponseHeaders().add("content-Type", "text/plain;charset=UTF-8");
            int responseCode = 404;
            int length = 0;
            exchange.sendResponseHeaders(responseCode,length);
            try (PrintWriter writer = getWriterFrom(exchange)){
                writer.write(msg );
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
