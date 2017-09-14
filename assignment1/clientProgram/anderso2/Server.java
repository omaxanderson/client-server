package anderso2;
import java.util.*;
import java.net.*;
import java.io.*;
import java.util.logging.*;

/*  O Maxwell Anderson
 *  9/2/17
 *  CSE383 Client/Server Prog.
 *  Filename: server.java
 *
 *  Simple server that will allow a client to connect,
 *  receive values from it, and send back a sum.
 *
 *  This code was written by O Maxwell Anderson
 */

enum Status {
        NO_CONNECTION,
        GOOD,
        ERROR
}

public class Server {

    // Class variables
    int port;
    DataInputStream inStream = null;
    DataOutputStream outStream = null;
    private static Logger LOGGER = Logger.getLogger("info");
    FileHandler fh = null;
    Status connectionStatus;


    public static void main(String args[]) throws IOException {
        Server s = new Server();
        s.Main();
    }

    // Constructor
    public Server() {
        port = 3333;

        try {
            fh = new FileHandler("server.log");
            LOGGER.addHandler(fh);
            LOGGER.setUseParentHandlers(false);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (IOException e) {
            System.err.println("ERROR - Unable to open/create log file");
        }
        LOGGER.info("Server port: " + port);
        connectionStatus = Status.NO_CONNECTION;
    }

    // Main server function
    public void Main() throws IOException {
        ServerSocket listener = new ServerSocket(port);

        while (true) {
            try {
                System.out.println("\nwaiting for connection, listening from port 3333");
                Socket socket = listener.accept();
                System.out.println("client connected");
                connect(socket);        // create IO streams
                sendGreeting();         // send greeting to client

                double sum = receiveValues();   // receive values from client

                sendSum(sum);           // send sum to client
                socket.close();
                System.out.println("client disconnected");
            } catch (Exception e) {
                System.err.println("Connection Error");
                connectionStatus = Status.ERROR;
                listener.close();

                listener = new ServerSocket(port);
            }
        }
    }

    // Creates the data input and output streams with client socket
    public void connect(Socket s) throws IOException {
        try {
            inStream = new DataInputStream(s.getInputStream());
            outStream = new DataOutputStream(s.getOutputStream());
            s.setSoTimeout(10000);
            LOGGER.info("Connected to client");
            connectionStatus = Status.GOOD;
        } catch (IOException e) {
            System.err.println("ERROR - IO Exception creating input " +
                    "and output streams from client socket");
            LOGGER.log(Level.SEVERE, "ERROR - Could not create Data Input/Output Streams", e);
            connectionStatus = Status.ERROR;
        }
    }

    // Writes the sum to the output stream
    public void sendSum(double sum) throws IOException {
        //System.out.println("Sending sum");
        if (connectionStatus == Status.GOOD) {
            try {
                outStream.writeUTF("OK");
                outStream.writeDouble(sum);
                outStream.flush();
            } catch (IOException e) {
                System.err.println("ERROR - IO Exception sending sum");
                LOGGER.log(Level.SEVERE, "ERROR - IOException sending sum", e);
                connectionStatus = Status.ERROR;
            }
        } else {
            System.err.println("Not sending sum, bad connection");
            LOGGER.log(Level.SEVERE, "Bad connection, cleaning up and preparing for new connection");
        }
    }

    // Parses the values from the client and calculates the sum
    // Long method, but most of it is error catching/handling
    public double receiveValues() throws IOException, EOFException {
        double sum = 0.0;

        boolean readData = false;
        long timer = System.currentTimeMillis();
        while (true) {
            try {
                int type = inStream.readInt();
                if (type == 1) {
                    sum += inStream.readInt();
                } else if (type == 2) {
                    sum += inStream.readDouble();
                } else if (type == 0) {
                    readData = true;
                    break;
                }
            } catch (EOFException e) {
                System.err.println("ERROR - Incorrect messege protocol " + e.toString());
                LOGGER.log(Level.SEVERE, "ERROR - EOF Exception when receiving values", e);
                connectionStatus = Status.ERROR;
                break;
            } catch (IOException e) {
                System.err.println("ERROR - " + e.toString());
                LOGGER.log(Level.SEVERE, "ERROR - IOException when receiving values", e);
                connectionStatus = Status.ERROR;
                break;
            }
        }

        return sum;
    }

    // Sends greeting to output stream -- greeting is my uniqueID
    public void sendGreeting() throws IOException {
        try {
            //System.out.println("Sending greeting");
            String greeting = "anderso2";
            outStream.writeUTF(greeting);
            outStream.flush();
        } catch (IOException e) {
            System.err.println("ERROR - IO Exception sending the greeting");
            LOGGER.log(Level.SEVERE, "ERROR - IOException while sending greeting", e);
        }
    }
}
