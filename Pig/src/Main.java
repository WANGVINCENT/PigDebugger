import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

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

		List<String> parameters =  new LinkedList<String>();
		parameters.add("-x");
		parameters.add(mode);
		parameters.add("-P");
		parameters.add(properties);
		parameters.add(pigname);
		
		String[] commands = parameters.toArray(new String[0]);
		
		Client client = new Client(ip, port);
		
		DbHandler dbHandler = DbHandler.getInstance();
		Sender sender = new Sender(client, dbHandler);
		
		long start = System.currentTimeMillis();
		
		if(type.equals("script")){
			//run script
			PigStats stats = PigRunner.run(commands, new ProgressNotification(sender, dbHandler, pigname, mode, type, start));
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
	        }
	        
		} else if(type.equals("explain")){
			//explain
			PigRunner.run(commands, new ProgressNotification(sender, dbHandler, pigname, mode, type, start));
		}
	}
}


