package com.luna.controller.helper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import com.luna.model.FileModel;
import com.luna.model.MapReduceAppModel;
import com.luna.model.SparkAppModel;

public class StagesExecutor implements Runnable{
	ArrayList<ArrayList<String>> stages;
	HashMap<String, FileModel> fileHashMap;
	HashMap<String, SparkAppModel> sparkHashMap;
	HashMap<String, MapReduceAppModel> mrHashMap;
	UUID executionId;
	final String CATALINA = System.getProperty("catalina.home");
	final String LOG_BASE = CATALINA+"/webapps/ROOT/Luna/Logs/";
	public StagesExecutor(ArrayList<ArrayList<String>> stages, 
			HashMap<String, FileModel> fileHashMap, HashMap<String, SparkAppModel> sparkHashMap, HashMap<String, MapReduceAppModel> mrHashMap, UUID executionId){
		this.stages = stages;
		this.fileHashMap = fileHashMap;
		this.sparkHashMap = sparkHashMap;
		this.mrHashMap = mrHashMap;
		this.executionId = executionId;
		(new File(LOG_BASE+"/"+executionId.toString())).mkdirs();
	}
	public void run(){
		File log = new File(LOG_BASE+"/"+executionId.toString()+"/"+"LOG");
		try {
			log.createNewFile();
			BufferedWriter logBuffer = new BufferedWriter(new FileWriter(log));
			logBuffer.write("Start processing: "+executionId.toString()+"\n");
			logBuffer.flush();
			logBuffer.write("Total Of Stages: "+stages.size()+" (Stage start from 0)\n");
			logBuffer.flush();
			for (ArrayList<String> jobList : stages) {
				System.out.println("Stage: "+stages.indexOf(jobList));
				logBuffer.write("Stage: "+stages.indexOf(jobList)+"\n");
				logBuffer.flush();
				ArrayList<Thread> threads = new ArrayList<Thread>();
				for (String jobKey : jobList) {				
					if(fileHashMap.get(jobKey) != null){
						System.out.println("File: "+fileHashMap.get(jobKey).getName());
					}else if(sparkHashMap.get(jobKey) != null){
						UUID jobExecutionId = UUID.randomUUID();
						System.out.println("Spark: "+sparkHashMap.get(jobKey).getName());
						logBuffer.write("Spark: "+sparkHashMap.get(jobKey).getName()+"\n");
						logBuffer.flush();
						logBuffer.write("Starting "+sparkHashMap.get(jobKey).getName()+" JobExecutionId: "+jobExecutionId+"\n");
						logBuffer.flush();
						SparkJobDispatcher sparkJob = new SparkJobDispatcher(executionId.toString(), sparkHashMap.get(jobKey).getName(),sparkHashMap.get(jobKey).getJarName(),sparkHashMap.get(jobKey).getClassPath(),sparkHashMap.get(jobKey).getArguments(),jobExecutionId);
						Thread sparkThread = new Thread(sparkJob);
						threads.add(sparkThread);
					}else if(mrHashMap.get(jobKey) != null){
						UUID jobExecutionId = UUID.randomUUID();
						System.out.println("MR: "+mrHashMap.get(jobKey).getName());
						logBuffer.write("MR: "+mrHashMap.get(jobKey).getName()+"\n");
						logBuffer.flush();
						logBuffer.write("Starting "+mrHashMap.get(jobKey).getName()+" JobExecutionId: "+jobExecutionId+"\n");
						logBuffer.flush();
						MRJobDispatcher mrJob = new MRJobDispatcher(executionId.toString(),mrHashMap.get(jobKey).getName(),mrHashMap.get(jobKey).getJarName(),mrHashMap.get(jobKey).getClassPath(),mrHashMap.get(jobKey).getArguments(),jobExecutionId);
						Thread mrThread = new Thread(mrJob);
						threads.add(mrThread);
					}				
				}
				for (Thread thread : threads) {
					thread.start();
				}
				for (Thread thread : threads) {
					try {
						thread.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}				
				}
				System.out.println("Stage: "+stages.indexOf(jobList)+" finished.\n");
				logBuffer.write("Stage: "+stages.indexOf(jobList)+" finished.\n");
				logBuffer.flush();
			}
			System.out.println("Execution: "+ executionId.toString()+" finished!");
			logBuffer.write("Execution: "+ executionId.toString()+" finished!");
			logBuffer.flush();
			logBuffer.close();
		} catch (IOException e1) {			
			e1.printStackTrace();
		}
		return;
	}
}
