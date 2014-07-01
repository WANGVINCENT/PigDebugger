package nanwang.pig.socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 
 * @author wangnan
 * @since 2014-05-01
 */

public class Client{
	
	private Socket socket;
	private BufferedReader br;
	private static BufferedWriter bw;
	
	/**
	 * Constructor
	 * @param ip
	 * @param port
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public Client(String ip, int port) throws UnknownHostException, IOException {
		socket = new Socket(ip, port);
		br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
	}
	
	/**
	 * This method is used for reading from socket
	 * @return
	 * @throws IOException
	 */
	public String read() throws IOException {
		StringBuilder sb = new StringBuilder();
        String line = null;
        line = br.readLine();
        sb.append(line);
        return sb.toString();
	}
	
	/**
	 * This method is used for writing to socket
	 * @param content
	 * @throws IOException
	 */
	public void write(String content) throws IOException {
		bw.write(content);
		bw.flush();
	}
	
	/**
	 * This method is used for closing write/read stream and socket
	 * @throws IOException
	 */
	public void close() throws IOException {
		br.close();
		bw.close();
		socket.close();
	}
}
