package nanwang.pig.socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	private ServerSocket server = null;
	private Socket socket = null;
	
	public Server(int port) throws IOException {
		 server = new ServerSocket(port);
	}
	
	public void accept() throws IOException {
		socket = server.accept();
	}
	
	public String read() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		StringBuilder sb = new StringBuilder();
        String line = null;

        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
		br.close();
		return sb.toString();
	}
	
	public void write(String content) throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		bw.write(content);
		bw.close();
	}
	
	public void close() throws IOException {
		socket.close();
	}
	
	public static void main(String[] args) {
		try {
			Server server = new Server(6960);
			while(true){
				server.accept();
				//server.write("receive!");
				System.out.println(server.read());
				server.write("receqsdqsdqsd");
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
