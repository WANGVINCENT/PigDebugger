import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nanwang.pig.entity.AVGCounter;
import nanwang.pig.entity.DbHandler;
import nanwang.pig.entity.JobCounter;
import nanwang.pig.socket.Client;
import nanwang.pig.socket.Sender;
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
	private String scriptId;
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
	private Sender sender;
	private final int CheckMapReduceTime  = 500;
	private final long start;
	private int jobRankNum;
	
	private final String initFilePath = "/usr/local/hadoop/logs/history/done/version-1/";

	public ProgressNotification(Sender sender, DbHandler dbHandler, String scriptName, String mode, String type, long start) throws UnknownHostException, IOException{
		this.scriptName = Tool.extractPigName(scriptName);
		this.mode = mode;
		this.type = type;
		this.dbHandler = dbHandler;
		this.start = start;
		this.sender = sender;
		this.client = sender.getClient();
		sender.setPigName(scriptName);
		
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
		
		scriptId = uuid;
		sender.setPigScriptId(scriptId);
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
				sender.addMessage(jsonObject.toString());
			}else{
				jsonObject.put("notification", "explain");
				jsonObject.put("plan", plan.toString());
				client.write(jsonObject.toString());
			}
			
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
			sender.addMessage(jsonObject.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void jobFinishedNotification(String uuid, JobStats stats) {
		
		jobRankNum++;
		//Construct the operation string
		String[] operations = new String[14]; 
		operations[0] = Tool.getCurrentTime();
		operations[1] = " FINISH JOB ";
		operations[2] = stats.getJobId();
		operations[3] = " ALIAS ";
		operations[4] = stats.getAlias();
		operations[5] = " FEATURES ";
		operations[6] = stats.getFeature();
		
		int mapNum = stats.getNumberMaps();
		int reduceNum = stats.getNumberReduces();
	
		//create a PigLatin counter entity
		JobCounter counter = new JobCounter();
		counter.setName(scriptName);
		counter.setMapElapsedTime(stats.getAvgMapTime()/1000);
		counter.setReduceElapsedTime(stats.getAvgREduceTime()/1000);
		counter.setMapNum(mapNum);
		counter.setReduceNum(reduceNum);
		counter.setJobRankNum(jobRankNum);
		
		StringBuilder filePath = new StringBuilder(initFilePath);
		//Obtain the root directory
		File rootFolder = new File(initFilePath.toString());
		File[] listOfRootFiles = rootFolder.listFiles();
		for(File file : listOfRootFiles){
			if(file.isDirectory()){
				filePath.append(file.getName());
				filePath.append("/");
			}
		}
		
		//Build the job info file path
		filePath.append(Tool.getDate());
		filePath.append("/");
		filePath.append("000000");
		filePath.append("/");
		
		File folder = new File(filePath.toString());
		File[] listOfFiles = folder.listFiles();
		BufferedReader br = null;
		String line = null;
		
		//Iterate through all files in specific the folder and find the file
		for (int i=0;i<listOfFiles.length;i++) {
			if(listOfFiles[i].isFile()&&listOfFiles[i].getName().endsWith(".pig")&&listOfFiles[i].getName().contains(stats.getJobId())) {
				
				Map<String, String> reduceStartTime = new HashMap<String, String>();
				try {
					long shuffleTime = 0;
					long sortTime = 0;
					long reduceTime = 0;
					
					//Read the file line by line
					br = new BufferedReader(new FileReader(filePath + listOfFiles[i].getName()));
					while ((line = br.readLine()) != null) {
						
						//Obtain the CPU time spent for each job
						if(line.contains("JOBID=")&&line.contains("CPU time spent")){
							Pattern pattern = Pattern.compile("(ms\\\\\\\\\\)\\))\\(([0-9]+)\\)");
							Matcher matcher = pattern.matcher(line);
							int count = 7;
							int mapCPU = 0, reducerCPU = 0;
						    while (matcher.find()) {
						    	if(Integer.valueOf(matcher.group(2)) > 0){
						    		if(count == 7){
						    			mapCPU = (int)(Long.valueOf(matcher.group(2))/1000/mapNum);
						    			counter.setMapCPUTime(mapCPU);
						    			operations[count] = "(MapCPUTime:" + String.valueOf(mapCPU) + "s)-";
						    		}else if(count == 8){
						    			reducerCPU = (int)(Long.valueOf(matcher.group(2))/1000/reduceNum);
						    			counter.setReduceCPUTime(reducerCPU);
						    			operations[count] = "(ReduceCPUTime:" + String.valueOf(reducerCPU) + "s)-";
						    		}else if(count == 9){
						    			operations[count] = "(TotalCPUTime:" + String.valueOf(mapCPU + reducerCPU) + "s)-";
						    			counter.setTotalCPUTime(mapCPU + reducerCPU);
						    		}
						    		count++;
						    	}
						    }
						    
						    Pattern shuffleBytesPattern = Pattern.compile("Reduce shuffle bytes\\)\\(([0-9]+)\\).*");
						    Matcher ShuffleBytesMatcher = shuffleBytesPattern.matcher(line);
							
							while (ShuffleBytesMatcher.find()) {
								int shuffleBytes = (int)(Long.valueOf(ShuffleBytesMatcher.group(1))/reduceNum/1048576);
								operations[13] = "(ShuffleBytes:" + String.valueOf(shuffleBytes) + "M)";
								counter.setShuffleBytes(shuffleBytes);
							}
						//Obtain the start time for each reducers
						}else if(line.contains("TASKID=")&&line.contains("TASK_TYPE=\"REDUCE\"")&&line.contains("START_TIME=")){
							Pattern startTimePattern = Pattern.compile("TASKID=\"(task.*)\".*TASK_TYPE.*START_TIME=\"([0-9]+)\"");
							Matcher startTimeMatcher = startTimePattern.matcher(line);
							
							while (startTimeMatcher.find()) {
								reduceStartTime.put(startTimeMatcher.group(1), startTimeMatcher.group(2));
							}
						//calculate respectively the shuffle time, sort time, reduce time and total reducer time
						}else if(line.contains("TASK_TYPE=\"REDUCE\"")&&line.contains("TASK_STATUS=\"SUCCESS\"")&&line.contains("SHUFFLE_FINISHED=")){
							Pattern shuffleTimePattern = Pattern.compile("TASKID=\"(.*)\".*TASK_ATTEMPT_ID=.*SHUFFLE_FINISHED=\"([0-9]+)\".*SORT_FINISHED=\"([0-9]+)\".*FINISH_TIME=\"([0-9]+)\"");
							Matcher shuffleTimeMatcher = shuffleTimePattern.matcher(line);
							
							while (shuffleTimeMatcher.find()) {
								if(reduceStartTime.containsKey(shuffleTimeMatcher.group(1))){
									long startTime = Long.valueOf(reduceStartTime.get(shuffleTimeMatcher.group(1)));
									shuffleTime += (Long.valueOf(shuffleTimeMatcher.group(2)) - startTime);
									sortTime += (Long.valueOf(shuffleTimeMatcher.group(3)) - startTime);
									reduceTime += (Long.valueOf(shuffleTimeMatcher.group(4)) - startTime);
								}
							}
						}
					}
					br.close();
					int avgShuffleTime = (int)(shuffleTime/1000/reduceNum);
					int avgSortTime = (int)(sortTime/1000/reduceNum - shuffleTime/1000/reduceNum);
					int avgReduceTime = (int)(reduceTime/1000/reduceNum - sortTime/1000/reduceNum);
					counter.setShufflePhaseTime(avgShuffleTime);
					counter.setSortPhaseTime(avgSortTime);
					counter.setReducePhaseTime(avgReduceTime);
					
					operations[10] = "(AvgShufflePhaseTime:" + String.valueOf(avgShuffleTime) + "s)-";
					operations[11] = "(AvgSortPhaseTime:" + String.valueOf(avgSortTime) + "s)-";
					operations[12] = "(AvgReducePhaseTime:" + String.valueOf(avgReduceTime) + "s)-";
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} 
	    }
		
		dbHandler.insert(counter);
		dbHandler.updateAVGCounter(new AVGCounter(counter));

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
			jsonObject.put("mapNumber", mapNum);
			jsonObject.put("reduceNumber", reduceNum);
			jsonObject.put("operation", operation);
			jsonObject.put("mode", mode);
			sender.addMessage(jsonObject.toString());
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
			sender.addMessage(jsonObject.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void jobsSubmittedNotification(String uuid, int number) {
		
		StringBuilder jobOrder = new StringBuilder();
		jobCounter += number;
		switch(jobCounter){
			case 1:
				jobOrder = new StringBuilder(jobCounter + "st");
				break;
			case 2:
				jobOrder = new StringBuilder(jobCounter + "nd");
				break;
			case 3:
				jobOrder = new StringBuilder(jobCounter + "rd");
				break;
			default:
				jobOrder = new StringBuilder(jobCounter + "th");
				break;
		}	
		
		//Construct the operation string
		String[] operations = { Tool.getCurrentTime(),
							    " SUBMIT the ",
							    jobOrder.toString(),
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
			sender.addMessage(jsonObject.toString());
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
			sender.addMessage(jsonObject.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		jsonObject = new JSONObject();
		try {
			long time = System.currentTimeMillis() - start;
			jsonObject.put("notification", "success");
			jsonObject.put("duration", time);
			sender.addMessage(jsonObject.toString());
			//Store job into database
			dbHandler.insertJob(scriptId, "Total execution time : " + time + " s");
			
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
			sender.addMessage(jsonObject.toString());
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
			sender.addMessage(jsonObject1.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Send output result
		/*Iterator<Tuple> it;
		StringBuilder result = new StringBuilder();
		try {
			it = outputStats.iterator();
			while(it.hasNext()){
		        Tuple t = it.next();
		        if(t.getAll() != null){
		        	result.append(t.getAll().toString()).append("-");
		        }
		    }
			
			//Store output into db
			dbHandler.insertOutput(uuid, result, scriptName, "Succeed", currentTime);
		} catch (IOException e) {
			e.printStackTrace();
		} */
	}
	
	@Override
	public void progressUpdatedNotification(String uuid, int number) {
	
		//Send output complete notification
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("notification", "progress");
			jsonObject.put("uuid", uuid);
			jsonObject.put("progress", number);
			sender.addMessage(jsonObject.toString());
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
				
				  sender.addMessage(jsonObject.toString());
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
				
				  sender.addMessage(jsonObject.toString());
			  } catch (JSONException e) {
				  e.printStackTrace();
			  } catch (IOException e) {
				  e.printStackTrace();
			  }
		  }
	 }
}
