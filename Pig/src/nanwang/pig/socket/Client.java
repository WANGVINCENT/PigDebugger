package nanwang.pig.socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import nanwang.pig.entity.DbHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * 
 * @author wangnan
 * @since 2014-05-01
 */

public class Client{
	
	private Socket socket;
	private BufferedReader br;
	private static BufferedWriter bw;
	//Messages queue
	private LinkedList<String> messages = new LinkedList<String>();
	//Execution complete flag
	private boolean completed = false;
	private final int periodicTime = 900;
	
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
		
		/*Start the timer and send message to node.js server periodically*/
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				for(int i=0;i<messages.size();i++){
					try {
						JSONTokener tokener = new JSONTokener(messages.get(i));
						JSONObject root = new JSONObject(tokener);
						if(root.get("notification").equals("success") && completed){
							write(messages.get(i) + "\n");
							messages.remove(i);
							close();
							DbHandler.close();
							System.exit(1);
						}else if(!root.get("notification").equals("success")){
							write(messages.get(i)+"\n");
							messages.remove(i);
						}
						
						if(root.get("notification").equals("complete")){
							completed = true;
						}
					} catch (IOException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}, 0, periodicTime);
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
	 * This method is used for adding messages in the queue
	 * @param message
	 * @throws IOException
	 */
	public void addMessage(String message) throws IOException{
		messages.add(message);
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
