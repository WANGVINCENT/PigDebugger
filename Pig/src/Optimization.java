import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import nanwang.pig.entity.AVGCounter;
import nanwang.pig.entity.DbHandler;
import nanwang.pig.socket.Client;

public class Optimization {
	
	private DbHandler dbHandler = DbHandler.getInstance();
	private List<AVGCounter> list;
	private int predictionFileSize;
	private int sampleFileSize;
	private int sampleShuffleBytes;
	private int predictionMapCPUTime;
	private int predictionReduceCPUTime;
	private Client client;
	
	public Optimization(Client client, String scriptName, int jobRankNum, int predictionFileSize, int sampleFileSize, int sampleShuffleBytes) {
		list = dbHandler.readAVGCounter(scriptName, jobRankNum);
		this.predictionFileSize = predictionFileSize;
		this.sampleFileSize = sampleFileSize;
		this.sampleShuffleBytes = sampleShuffleBytes;
		this.client = client;
	}
	
	/**
	 * The method is used for the optimization
	 */
	public void Optimize(){
		
		double predictionShuffleBytes = predictionFileSize * sampleShuffleBytes / sampleFileSize;
		int predictionReduceNum = (int) Math.round(predictionShuffleBytes / 15);
		
		double reduceRate = 0;
		int count = 0;
		double MapCPUTime = 0;
		
		for(AVGCounter counter : list){
			reduceRate += (double)counter.getReduceCPUTime() / counter.getShuffleBytes();
			MapCPUTime += counter.getMapCPUTime();
			count++;
		}
		reduceRate /= count;
		predictionMapCPUTime = (int)Math.round(MapCPUTime / count);
		predictionReduceCPUTime = (int)Math.round(reduceRate * predictionShuffleBytes / predictionReduceNum);
		
		//Send job finish notification
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("notification", "Optimization");
			jsonObject.put("ReduceNum", predictionReduceNum);
			jsonObject.put("MapCPUTime", predictionMapCPUTime);
			jsonObject.put("ReduceCPUTime", predictionReduceCPUTime);
			jsonObject.put("TotalCPUTime", predictionMapCPUTime+predictionReduceCPUTime);
			client.write(jsonObject.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		
		String scriptName = args[0];
        int jobRankNum = Integer.valueOf(args[1]);
        int predictionFileSize = Integer.valueOf(args[2]);
        int sampleFileSize = Integer.valueOf(args[3]);
        int sampleShuffleBytes = Integer.valueOf(args[4]);
        String ip = args[5];
		int port = Integer.valueOf(args[6]);
        
        Client client = new Client(ip, port);
        
        Optimization optimization = new Optimization(client, scriptName, jobRankNum, predictionFileSize, sampleFileSize, sampleShuffleBytes);
        optimization.Optimize();		
	}
}
