import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;

import nanwang.pig.entity.DbHandler;
import nanwang.pig.socket.Client;
import nanwang.pig.socket.Sender;
import nanwang.pig.utils.Tool;

import org.apache.pig.PigRunner;
import org.apache.pig.tools.pigstats.PigStats;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This is the main class the accepts parameters from shell and launches the Pig script runner
 * @author wangnan
 * @since 2014-05-01
 */

public class Main {

	public static void main(String[] args) throws JSONException, UnknownHostException, IOException {
		
		String mode = args[0];
		String pigname = args[1];
		String ip = args[2];
		int port = Integer.valueOf(args[3]);
		String type = args[4];
		String properties = args[5];
		
		/*String mode = "local";
		String pigname = "test.pig";
		String ip = "localhost";
		int port = 6969;
		String type = "script";*/
		
		LinkedList<String> parameters =  new LinkedList<String>();
		parameters.add("-x");
		parameters.add(mode);
		parameters.add("-P");
		parameters.add(properties);
		parameters.add(pigname);
		
		String[] commands = parameters.toArray(new String[0]);
		
		Client client = new Client(ip, port);
		Sender sender = new Sender(client);
		
		DbHandler dbHandler = DbHandler.getInstance();
		
		if(type.equals("script")){
			//run script
			PigStats stats = PigRunner.run(commands, new ProgressNotification(sender, dbHandler, pigname, mode, type));
	        if(!stats.isSuccessful()){
	        	//Send fail message
				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("notification", "fail");
					jsonObject.put("error", stats.getErrorMessage());
					sender.addMessage(jsonObject.toString());
					
					pigname = Tool.extractPigName(pigname);
					String time = Tool.getCurrentTime();
					//Store output into db
					dbHandler.insertOutput(stats.getScriptId(), new StringBuilder(stats.getErrorMessage()), pigname, "Fail", time);
					
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }else{
	        	//Send success message
				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("notification", "success");
					jsonObject.put("duration", stats.getDuration());
					sender.addMessage(jsonObject.toString());
					//Store job into database
					dbHandler.insertJob(stats.getScriptId(), jsonObject.toString());
					
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
	        
		} else if(type.equals("explain")){
			//explain
			PigRunner.run(commands, new ProgressNotification(sender, dbHandler, pigname, mode, type));
		}
	}
}


