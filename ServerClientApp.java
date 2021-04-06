package com.mathslove.server_client;


import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import static com.mathslove.server_client.ServerClientApp.Fibonacci;


class Server{
    private final ServerSocket serverSocket;

    public static Server create(String host, int port) throws IOException {
        return new Server(new ServerSocket(port, 100, InetAddress.getByName(host)));
    }

    private Server(ServerSocket ss){
        serverSocket = ss;
    }

    public void start() throws IOException {
        try {
            while (true){
                Socket socket = serverSocket.accept();
                try {
                    var cl = new ClientHandler(socket);
                    System.out.println("Client connected " + socket.getRemoteSocketAddress().toString());
                    cl.start();
                } catch (IOException e){
                    socket.close();
                    System.err.println("Client connection was failed" + socket.toString());
                    e.printStackTrace(System.err);
                }
            }
        } finally {
            serverSocket.close();
        }

    }

    public void stop() throws IOException {
        serverSocket.close();
    }
}

class ClientHandler extends Thread{
    private final Socket clientSocket;
    private final PrintWriter out;
    private final BufferedReader in;

    public ClientHandler(Socket socket) throws IOException {
        clientSocket = socket;
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream())), true);
    }

    @Override
    public void run() {
        try {

            String line;
            while ((line = in.readLine()) != null) {

                try {
                    var n = Integer.parseInt(line);
                    out.println(Fibonacci(n));

                } catch (NumberFormatException e) {
                    out.println("You send a wrong formation");
                }
            }

        } catch (IOException e){
            e.printStackTrace();
            this.closeHandler();
        }
        finally {
            System.out.println("Client disconnected " + clientSocket.getRemoteSocketAddress().toString());
            this.closeHandler();
        }
    }

    private void closeHandler() {
        try {
            if(!clientSocket.isClosed()) {
                clientSocket.close();
                in.close();
                out.close();
                this.interrupt();
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.interrupt();
        }
    }

}

class Client {
    private final Socket clientSocket;
    private final PrintWriter out;
    private final BufferedReader in;

    public Client(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream())), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public String getFibonacciNum(String msg) throws IOException {
        out.println(msg);
        return in.readLine();
    }

    public void stopConnection() throws IOException {
        try {
            if (!clientSocket.isClosed()) {
                clientSocket.close();
                in.close();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class ServerClientApp {

    // Метод Фиббоначи
    // 0,1,1,2,3,5,8,...
    static BigInteger Fibonacci(Integer n){
        BigInteger a = BigInteger.valueOf(0);
        BigInteger b = BigInteger.valueOf(1);
        for (int i = 1; i <= n; i++) {
            var tmp_a = a;
            a = a.add(b);
            b = tmp_a;
        }
        return a;

    }

//    -<s or c> --host <ip> --port <number>
    static final int TEMPLATE_NUM_OF_ARGS = 5;

    public static void main(String[] args) {
        try {
            if (args.length != TEMPLATE_NUM_OF_ARGS) {
                throw new IllegalArgumentException();
            }

            if (args[0].equals("-s") && args[1].equals("--host") && args[3].equals("--port")) {

                try {
                    var s = Server.create(args[2], Integer.parseInt(args[4]));
                    s.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            } else if (args[0].equals("-c") && args[1].equals("--host") && args[3].equals("--port")) {

                try {
                    var client = new Client(args[2], Integer.parseInt(args[4]));
                    var scanner = new Scanner(System.in);

                    while (true){
                        var line = scanner.nextLine();

                        if (line.equals("")){
                            client.stopConnection();
                            break;
                        }
                        System.out.println(client.getFibonacciNum(line));
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                throw new IllegalArgumentException();
            }

        } catch (IllegalArgumentException e) {
            System.out.println("""
                        Please choose the option:
                        \t-s --host <ip> --port <number> : start a server
                        \t-c --host <ip> --port <number> : start a client
                        """);
        }
    }
}
