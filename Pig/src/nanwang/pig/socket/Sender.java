package nanwang.pig.socket;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import nanwang.pig.entity.DbHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Sender {
	
	//Messages queue
	private final Queue<String> messages = new LinkedList<String>();
	private final Timer timer = new Timer();
	//Execution complete flag
	private boolean completed = false;
	private final int periodicTime = 900;
	private final Client client;
	
	public Sender(Client client){
		this.client = client;
		
		startTimer(timer);
	}
	
	/**
	 * This method is used for the start of the timer and send message to node.js server periodically
	 * @param timer
	 */
	public void startTimer(Timer timer){
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				while(!messages.isEmpty())
				{
					try {
						JSONTokener tokener = new JSONTokener(messages.peek());
						JSONObject root = new JSONObject(tokener);
						if(root.get("notification").equals("success") && completed){
							client.write(messages.poll() + "\n");
							client.close();
							DbHandler.close();
							System.exit(1);
						}else if(!root.get("notification").equals("success")){
							client.write(messages.poll() + "\n");
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
	 * This method is used for adding messages in the queue
	 * @param message
	 * @throws IOException
	 */
	public void addMessage(String message) throws IOException{
		messages.add(message);
	}
	
	/**
	 * Get client object
	 * @return
	 */
	public Client getClient(){
		return client;
	}
}
