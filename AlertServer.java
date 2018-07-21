import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.Runtime;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DatabaseMetaData;

import org.sqlite.JDBC;

/*
* Nick Bild
* 2018-03-09
* Listen for remote commands from client
* and take appropriate actions.
*/
public class AlertServer {

	public static void main(String[] args) throws IOException {
        	// Check input.
		if (args.length != 1) {
			System.err.println("Usage: java AlertServer <port>");
			System.exit(1);
		}
        
        	int port = Integer.parseInt(args[0]);

		// Set listening port.
		ServerSocket serverSocket = new ServerSocket(port);
		Socket clientSocket = null;

		System.out.println("Starting Server on port: " + port);

		// Infinite loop to accept new connections.
		while (true) {
	        	try {
				// Open a new client connection.
				clientSocket = serverSocket.accept();

				// Spawn new thread to handle client.
				Runnable clientHandler = new ClientHandler(clientSocket);
				Thread t = new ClientHandler(clientSocket);
				t.start();

			} catch (Exception e) { // Catch errors and clean up.
				clientSocket.close();
	                	e.printStackTrace();
			}
		}
	}
}

/*
* Nick Bild
* 2018-03-02
* ClientHandler is ran in a thread for each individual client connection.
* It handles all server interaction with the clients.
*/
class ClientHandler extends Thread {
	// Open database connection.
        public static Connection conn = connectDB();

	private final Socket clientSocket;

	public ClientHandler(Socket clientSocket) {
	        this.clientSocket = clientSocket;
	}

	public void run() {
		try {
			// Open readers and writers on socket.
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                        // Listen for 1 line of text.
                        String data = in.readLine();
			String[] parts = data.split(" ");

			//System.out.println(data);
                        
			if (parts[0].equals("ALERT")) {
				System.out.println("Report of shots fired by client: " + parts[1]);

				// Current date/time.
				Date date = new Date();

				// Insert incident in database.
				String sql = "INSERT OR IGNORE INTO event (institutionid, clientid, event, ts, active) VALUES ('" + parts[2] + "', '" + parts[1] + "', 'gunshot', '" + date + "', '1');";
                                Statement stmt = conn.createStatement();
                                stmt.executeUpdate(sql);

			} else if (parts[0].equals("QUERY")) {
				System.out.println("Responding to query from client at : " + parts[2]);

				// Check database for location requested.
				String sql = "SELECT * FROM event WHERE institutionid='" + parts[2] + "' AND active='1';";
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);

                        	// Send each active alert.
                        	while (rs.next()) {
                                	out.println("Gunfire detected in " + rs.getString("clientid") + " at: " + rs.getString("ts"));
                        	}

				out.println("END-MESSAGE");

			}

			clientSocket.close();

		} catch (Exception e) {
			System.out.println(e.getMessage());

		}

	}

	// SQLite connection creation.
        private static Connection connectDB() {
                String url = "jdbc:sqlite:alert_server.db";
                Connection conn = null;
                try {
                        Class.forName("org.sqlite.JDBC");
                        conn = DriverManager.getConnection(url);

                } catch (Exception e) {
                        System.out.println(e.getMessage());

                }

                return conn;
        }

}

