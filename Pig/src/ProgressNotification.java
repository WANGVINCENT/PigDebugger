import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import nanwang.pig.entity.Job;
import nanwang.pig.entity.Output;
import nanwang.pig.entity.DbHandler;
import nanwang.pig.socket.Client;
import nanwang.pig.utils.Tool;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobID;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.plans.MROperPlan;
import org.apache.pig.data.Tuple;
import org.apache.pig.tools.pigstats.JobStats;
import org.apache.pig.tools.pigstats.OutputStats;
import org.apache.pig.tools.pigstats.PigProgressNotificationListener;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class implements a Pig API PigProgressNotificationListener class which feeds back
 * MapReduce progressions while executing jobs
 * @author wangnan
 * @since 2014-05-01
 */

public class ProgressNotification implements PigProgressNotificationListener{
	
	private int jobCounter = 0;
	private Client client = null;
	private String scriptName = null;
	private String currentJobId = null;
	private JobClient jobClient = null;
	private float mapProgress = 0;
	private float reduceProgress = 0;
	private boolean mapDone = false;
	private boolean reduceDone = false;
	//Pig execution mode
	private String mode = null;
	private DbHandler dbHandler = null;
	private final int CheckMapReduceTime  = 500;

	public ProgressNotification(Client client, DbHandler dbHandler, String scriptName, String mode) throws UnknownHostException, IOException{
		this.client = client;
		this.scriptName = scriptName;
		this.mode = mode;
		
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

		String currentTime = Tool.getCurrentTime();
		String operation = currentTime + " Launch Pig script [" + uuid + "]";
		
		//Store job fail notification to db
		Job job = new Job();
		job.setUuid(uuid);
		job.setOperation(operation);
		dbHandler.insert(job);

		//Send launch script  
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("notification", "launch");
			jsonObject.put("uuid", uuid);
			jsonObject.put("operation", operation);
			jsonObject.put("mode", mode);
			jsonObject.put("plan", plan.toString());
			client.addMessage(jsonObject.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void jobFailedNotification(String uuid, JobStats stats) {
		
		String currentTime = Tool.getCurrentTime();
		String operation = currentTime + " JOB FAILS " + stats.getJobId() + " ALIAS " + stats.getAlias();
		
		//Store job fail notification to db
		Job job = new Job();
		job.setUuid(uuid);
		job.setOperation(operation);
		dbHandler.insert(job);
		
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
		
		String currentTime = Tool.getCurrentTime();
		String operation = currentTime + " FINISH JOB " + stats.getJobId() + " ALIAS " + stats.getAlias() + " FEATURES " + stats.getFeature();
		
		//Store job finish notification to db
		Job job = new Job();
		job.setUuid(uuid);
		job.setOperation(operation);
		dbHandler.insert(job);
		
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
		
		String currentTime = Tool.getCurrentTime();
		String operation = currentTime + " START JOB " + jobId;
		
		//Store job start notification to db
		Job job = new Job();
		job.setUuid(uuid);
		job.setOperation(operation);
		dbHandler.insert(job);
		
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
		
		String currentTime = Tool.getCurrentTime();
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
		String operation = currentTime + " SUBMIT the " + jobOrder + " JOB";
		
		//Store job submit notification
		Job job = new Job();
		job.setUuid(uuid);
		job.setOperation(operation);
		dbHandler.insert(job);
		
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
		
		String currentTime = Tool.getCurrentTime();
		String operation = currentTime + " COMPLETE " + jobsNumber + " JOBS";
		
		//Store complete notification to db
		Job job = new Job();
		job.setUuid(uuid);
		job.setOperation(operation);
		dbHandler.insert(job);
		
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
		
		String currentTime = Tool.getCurrentTime();
		String operation = currentTime + " LAUNCH " + jobsNumber + " JOBS";
		
		//Store start notification
		Job job = new Job();
		job.setUuid(uuid);
		job.setOperation(operation);
		dbHandler.insert(job);
		
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
		String operation = currentTime + " COMPLETE OUTPUT";
		if(mode.equals("mapreduce")){
			operation += " WITH " + outputStats.getNumberRecords() + " RECORDS";
		}
		
		//Store output complete notification to db
		Job job = new Job();
		job.setUuid(uuid);
		job.setOperation(operation);
		dbHandler.insert(job);
		
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
		String result = null;
		try {
			it = outputStats.iterator();
			jsonObject2.put("notification", "output");
			jsonObject2.put("uuid", uuid);
			while(it.hasNext()){
		        Tuple t = it.next();
		        if(t.getAll() != null){
		        	result += t.getAll().toString() + "-";
		        }
		    }
			jsonObject2.put("result", result);
			client.addMessage(jsonObject2.toString());
			
			scriptName = Tool.extractPigName(scriptName);
			//Store output into db
			Output output = new Output();
			output.setUuid(uuid);
			output.setOutput(result);
			output.setName(scriptName);
			output.setState("Succeed");
			output.setTime(currentTime);
			dbHandler.insert(output);
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
