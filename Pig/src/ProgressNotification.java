import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import nanwang.pig.entity.DbHandler;
import nanwang.pig.socket.Client;
import nanwang.pig.utils.Tool;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobID;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.MapReduceOper;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.plans.MROperPlan;
import org.apache.pig.data.Tuple;
import org.apache.pig.impl.plan.OperatorKey;
import org.apache.pig.tools.pigstats.JobStats;
import org.apache.pig.tools.pigstats.OutputStats;
import org.apache.pig.tools.pigstats.PigProgressNotificationListener;
import org.apache.pig.tools.pigstats.ScriptState;
import org.json.JSONException;
import org.json.JSONObject;
import org.stringtemplate.v4.compiler.STParser.ifstat_return;

/**
 * This class implements a Pig API PigProgressNotificationListener class which feeds back
 * MapReduce progressions while executing jobs
 * @author wangnan
 * @since 2014-05-01
 */

public class ProgressNotification implements PigProgressNotificationListener{
	
	private int jobCounter;
	private Client client;
	private String scriptName;
	private String currentJobId;
	private JobClient jobClient;
	private float mapProgress;
	private float reduceProgress;
	private boolean mapDone;
	private boolean reduceDone;
	//Pig execution mode
	private String mode ;
	private String type;
	private DbHandler dbHandler;
	private final int CheckMapReduceTime  = 500;

	public ProgressNotification(Client client, DbHandler dbHandler, String scriptName, String mode, String type) throws UnknownHostException, IOException{
		this.client = client;
		this.scriptName = scriptName;
		this.mode = mode;
		this.type = type;
		this.dbHandler = dbHandler;
		
		Configuration conf = new Configuration();
		jobClient = new JobClient(new JobConf(conf)); 
		
		if(mode.equals("mapreduce")){
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				public void run() {
					if(currentJobId != null){
						try {
							mapreduceProgress(currentJobId);
						} catch (SecurityException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (NoSuchFieldException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					}
				}
			}, 0, CheckMapReduceTime);
		}
	}
	
	@Override
	public void initialPlanNotification(String uuid, MROperPlan plan) {
		
		//Get and construct alias string
		LinkedList<String> aliasList = new LinkedList<String>();
		
		Map<OperatorKey, MapReduceOper> planKeys = plan.getKeys();
		for(Map.Entry<OperatorKey, MapReduceOper> entry : planKeys.entrySet()) {
			MapReduceOper mrOper = entry.getValue();
			String[] aliases = Tool.toArray(ScriptState.get().getAlias(mrOper).trim());
			aliasList.add(StringUtils.join(aliases, ","));
		}
		aliasList.add(aliasList.pop());
		
		//Construct the operation string
		String[] operations = { Tool.getCurrentTime(), 
								" Launch Pig script [", 
								uuid, 
								"]" };
		StringBuilder operation = Tool.join(operations);

		//Store job fail notification to db
		dbHandler.insertJob(uuid, operation.toString());

		//Send launch script  
		JSONObject jsonObject = new JSONObject();
		try {
			if(type.equals("script")){
				jsonObject.put("notification", "launch");
				jsonObject.put("uuid", uuid);
				jsonObject.put("operation", operation);
				jsonObject.put("mode", mode);
				jsonObject.put("plan", plan.toString());
				jsonObject.put("alias", StringUtils.join(aliasList, "+"));
			}else{
				jsonObject.put("notification", "explain");
				jsonObject.put("plan", plan.toString());
			}
			client.write(jsonObject.toString());
			
			//insert mrplan into db
			dbHandler.insertMRPlan(uuid, plan.toString());
			
			if(type.equals("explain")){
				System.exit(1);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void jobFailedNotification(String uuid, JobStats stats) {
		
		//Construct the operation string
		String[] operations = { Tool.getCurrentTime(),
								" JOB FAILS ",
								stats.getJobId(),
								" ALIAS ",
								stats.getAlias() };
		StringBuilder operation = Tool.join(operations);

		//Store job fail notification to db
		dbHandler.insertJob(uuid, operation.toString());
		
		//Send job fail notification 
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("notification", "jobfail");
			jsonObject.put("uuid", uuid);
			jsonObject.put("operation", operation);
			client.addMessage(jsonObject.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void jobFinishedNotification(String uuid, JobStats stats) {
		
		//Construct the operation string
		String[] operations = { Tool.getCurrentTime(),
								" FINISH JOB ",
								stats.getJobId(),
								" ALIAS ",
								stats.getAlias(),
								" FEATURES ",
								stats.getFeature(),
								"Mappers:",
								String.valueOf(stats.getNumberMaps()),
								" Reducers:",
								String.valueOf(stats.getNumberReduces()) };
		StringBuilder operation = Tool.join(operations);
		
		//Store job finish notification to db
		dbHandler.insertJob(uuid, operation.toString());
		
		//Send job finish notification
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("notification", "jobfinish");
			jsonObject.put("uuid", uuid);
			jsonObject.put("jobid", stats.getJobId());
			jsonObject.put("maptime", stats.getAvgMapTime());
			jsonObject.put("reducetime", stats.getAvgREduceTime());
			jsonObject.put("mapNumber", stats.getNumberMaps());
			jsonObject.put("reduceNumber", stats.getNumberReduces());
			jsonObject.put("operation", operation);
			jsonObject.put("mode", mode);
			client.addMessage(jsonObject.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void jobStartedNotification(String uuid, String jobId) {
		
		if(currentJobId != null && currentJobId != jobId){
			mapProgress = 0;
			reduceProgress = 0;
			mapDone = false;
			reduceDone = false;
		}
		currentJobId = jobId;
		
		//Construct the operation string
		String[] operations = { Tool.getCurrentTime(),
							   " START JOB ",
							   jobId
		};
		StringBuilder operation = Tool.join(operations);
		
		//Store job start notification to db
		dbHandler.insertJob(uuid, operation.toString());
		
		//Send job start and running notification
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("notification", "jobstart");
			jsonObject.put("uuid", uuid);
			jsonObject.put("jobid", jobId);
			jsonObject.put("operation", operation);
			jsonObject.put("mode", mode);
			client.addMessage(jsonObject.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void jobsSubmittedNotification(String uuid, int number) {
		
		String jobOrder = ""; 
		jobCounter += number;
		if(jobCounter == 1) {
			jobOrder = jobCounter + "st";
		} else if(jobCounter == 2) {
			jobOrder = jobCounter + "nd";
		} else if(jobCounter == 3) {
			jobOrder = jobCounter + "rd";
		} else {
			jobOrder = jobCounter + "th";
		}
		
		//Construct the operation string
		String[] operations = { Tool.getCurrentTime(),
							    " SUBMIT the ",
							    jobOrder,
							    " JOB" };
		StringBuilder operation = Tool.join(operations);
		
		//Store job submit notification
		dbHandler.insertJob(uuid, operation.toString());
		
		//Send job finish notification
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("notification", "jobsubmit");
			jsonObject.put("uuid", uuid);
			jsonObject.put("operation", operation);
			client.addMessage(jsonObject.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void launchCompletedNotification(String uuid, int jobsNumber) {
		
		//Construct the operation string
		String[] operations = { Tool.getCurrentTime(),
								" COMPLETE ",
								String.valueOf(jobsNumber),
								" JOBS" };
		StringBuilder operation = Tool.join(operations);
		
		//Store complete notification to db
		dbHandler.insertJob(uuid, operation.toString());
		
		//Send complete notification
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("notification", "complete");
			jsonObject.put("uuid", uuid);
			jsonObject.put("operation", operation);
			client.addMessage(jsonObject.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void launchStartedNotification(String uuid, int jobsNumber) {
		
		//Construct the operation string
		String[] operations = { Tool.getCurrentTime(),
								" LAUNCH ",
								String.valueOf(jobsNumber),
								" JOBS"
		}; 
		StringBuilder operation = Tool.join(operations);
		
		//Store start notification
		dbHandler.insertJob(uuid, operation.toString());
		
		//Send complete notification
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("notification", "start");
			jsonObject.put("uuid", uuid);
			jsonObject.put("jobsNumber", jobsNumber);
			jsonObject.put("operation", operation);
			jsonObject.put("mode", mode);
			client.addMessage(jsonObject.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void outputCompletedNotification(String uuid, OutputStats outputStats) {
		
		String currentTime = Tool.getCurrentTime();
		
		//Construct the operation string
		String[] operations = new String[5];
		operations[0] = currentTime;
		operations[1] = " COMPLETE OUTPUT";

		if(mode.equals("mapreduce")){
			operations[2] = " WITH ";
			operations[3] = String.valueOf(outputStats.getNumberRecords());
			operations[4] = " RECORDS";
		}
		StringBuilder operation = Tool.join(operations);
		
		//Store output complete notification to db
		dbHandler.insertJob(uuid, operation.toString());
		
		//Send output complete notification
		JSONObject jsonObject1 = new JSONObject();
		try {
			jsonObject1.put("notification", "outputcomplete");
			jsonObject1.put("uuid", uuid);
			jsonObject1.put("operation", operation);
			client.addMessage(jsonObject1.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Send output result
		JSONObject jsonObject2 = new JSONObject();
		Iterator<Tuple> it;
		StringBuilder result = new StringBuilder();
		try {
			it = outputStats.iterator();
			jsonObject2.put("notification", "output");
			jsonObject2.put("uuid", uuid);
			while(it.hasNext()){
		        Tuple t = it.next();
		        if(t.getAll() != null){
		        	result.append(t.getAll().toString()).append("-");
		        }
		    }
			jsonObject2.put("result", result);
			client.addMessage(jsonObject2.toString());
			
			scriptName = Tool.extractPigName(scriptName);
			//Store output into db
			dbHandler.insertOutput(uuid, result, scriptName, "Succeed", currentTime);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void progressUpdatedNotification(String uuid, int number) {
	
		//Send output complete notification
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("notification", "progress");
			jsonObject.put("uuid", uuid);
			jsonObject.put("progress", number);
			client.addMessage(jsonObject.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the progression of map and reduce tasks for each job and add message in the queue
	 * @param id
	 * @throws IOException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public void mapreduceProgress(String id) throws IOException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		 
		  JobID jobID = JobID.forName(id);                     
		  RunningJob runningJob = jobClient.getJob(jobID);
		  mapProgress = runningJob.mapProgress();
		  reduceProgress = runningJob.reduceProgress();
		  
		  if(mapProgress == 1 && reduceProgress == 0 && !mapDone){
			  mapDone = true;
			  //Send output complete notification
			  try {
				  JSONObject jsonObject = new JSONObject();
				  jsonObject.put("notification", "mapreduceprogress");
				  jsonObject.put("jobid", id);
				  jsonObject.put("mapprogress", runningJob.mapProgress());
				  jsonObject.put("reduceprogress", runningJob.reduceProgress());
				
				  client.addMessage(jsonObject.toString());
			  } catch (JSONException e) {
				  e.printStackTrace();
			  } catch (IOException e) {
				  e.printStackTrace();
			  }
		  }else if(mapProgress == 1 && reduceProgress == 1 && !reduceDone){
			  //Send output complete notification
			  reduceDone = true;
			  try {
				  JSONObject jsonObject = new JSONObject();
				  jsonObject.put("notification", "mapreduceprogress");
				  jsonObject.put("jobid", id);
				  jsonObject.put("mapprogress", runningJob.mapProgress());
				  jsonObject.put("reduceprogress", runningJob.reduceProgress());
				
				  client.addMessage(jsonObject.toString());
			  } catch (JSONException e) {
				  e.printStackTrace();
			  } catch (IOException e) {
				  e.printStackTrace();
			  }
		  }
	 }
}
