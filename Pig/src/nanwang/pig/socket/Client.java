package nanwang.pig.socket;

import java.io.BufferedWriter;
import java.io.IOException;
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
		bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
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
		bw.close();
		socket.close();
	}
}
