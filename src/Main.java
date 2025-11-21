import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        try{
            HttpServer server = BaseServer.makeServer();
            initRoutes(server);
            server.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Создание путей
    public static void initRoutes(HttpServer server) {
        server.createContext("/",(Main::handleRequest));
        server.createContext("/apps",(Main::handleRequest));
        server.createContext("/apps/profile",(Main::handleRequestProfile));

    }

    //считывает файлы все как в прощлой вместо сокета тут эксчендж только тут чарсет указать надо только он еще принимает тело запроса
    // от чего когда учитель ввел json он в ответе появаилось тело хттп ответа вроде
    private static BufferedReader gerReader(HttpExchange exchange) {
        InputStream inputStream = exchange.getRequestBody();
        Charset charset = StandardCharsets.UTF_8;
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream,charset);
        return new BufferedReader(inputStreamReader);
    }
    //записывает файлы тела хттп в стрим из него в принтврайтер как в прошлой домашке
    private static PrintWriter getWriterFrom(HttpExchange exchange) {
        OutputStream outputStream  = exchange.getResponseBody();
        Charset charset = StandardCharsets.UTF_8;
        return new PrintWriter(outputStream,false,charset);
    }
    //записывает в ответ то что ввели в теле запроса
    private static void writeData(Writer writer,HttpExchange exchange) {
        try (BufferedReader reader = gerReader(exchange)){
            if(!reader.ready()) return;
            write(writer,"data","");
            reader.lines().forEach(v -> write(writer,"\t", v));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleRequest(HttpExchange exchange) {
        try {
            //тут первая часть запроса не забыть спросить про ленч сапа потом на скрине помечена как статус лайн
            exchange.getResponseHeaders().add("content-Type", "text/plain;charset=UTF-8");
            int responseCode = 200;
            int length = 0;
            exchange.sendResponseHeaders(responseCode,length);
            //другой хедер
            try (PrintWriter writer = getWriterFrom(exchange)){
                String method = exchange.getRequestMethod();
                URI uri = exchange.getRequestURI();
                String ctxPath = exchange.getHttpContext().getPath();
                write(writer,"HTTP method",method);
                write(writer,"Request",uri.toString());
                write(writer,"Handler",ctxPath);
                writeHeaders(writer, "Request headers", exchange.getRequestHeaders());
                //тут тело метода врайт дата пишет
                writeData(writer,exchange);
                writer.flush();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleRequestProfile(HttpExchange exchange) throws IOException {
        error404(exchange);
        return;
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