package com.luna.controller.workflow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.luna.controller.helper.StagesExecutor;
import com.luna.model.FileModel;
import com.luna.model.MapReduceAppModel;
import com.luna.model.SparkAppModel;

/**
 * Servlet implementation class WorkflowExecutor
 */
@WebServlet("/WorkflowExecutor")
public class WorkflowExecutor extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public WorkflowExecutor() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html"); 
		PrintWriter out = response.getWriter(); 
		out.println("<HTML><HEAD>"); 
		out.println("<TITLE>Workflow Executor</TITLE>"); 
		out.println("</HEAD><BODY>");
		out.println("<p>USE HTTP POST!</p>");
		out.println("</BODY></HTML>");
		out.flush();
		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BufferedReader br = new BufferedReader(new  InputStreamReader(request.getInputStream()));
	    String json = "";
	    if(br != null){
	        json = br.readLine();
	    }
		JSONObject jObj = new JSONObject(json); // this parses the json
		printJSON(jObj);
		
		JSONObject processes =jObj.getJSONObject("processes");
		Iterator<String> processesIterator = processes.keys();
		
		//Initialize Components information.
		HashMap<String, FileModel> fileHashMap = new HashMap<String, FileModel>();
		HashMap<String, SparkAppModel> sparkHashMap = new HashMap<String, SparkAppModel>();
		HashMap<String, MapReduceAppModel> mrHashMap = new HashMap<String, MapReduceAppModel>();
		try{
			while(processesIterator.hasNext()){
				String key = processesIterator.next();		
				JSONObject component = processes.getJSONObject(key);
				JSONObject metadata = component.getJSONObject("metadata");
				switch (component.getString("component")) {
				case "File":
					FileModel newfile = new FileModel(metadata.getString("label"), metadata.getString("source"));
					fileHashMap.put(key, newfile);
					break;
				case "Spark":
					JSONArray argumentsArray = metadata.getJSONArray("argument");
					ArrayList<String> arguments = new ArrayList<String>();
					for(int i = 0; i < argumentsArray.length(); i++){
						arguments.add(argumentsArray.getString(i));
					}
					SparkAppModel newSpark = new SparkAppModel(metadata.getString("label"),metadata.getString("jarName"),metadata.getString("classPath"),arguments);
					sparkHashMap.put(key, newSpark);
					break;
				case "MapReduce":
					JSONArray mrArgumentsArray = metadata.getJSONArray("argument");
					ArrayList<String> mrArguments = new ArrayList<String>();
					for(int i = 0; i < mrArgumentsArray.length(); i++){
						mrArguments.add(mrArgumentsArray.getString(i));
					}
					MapReduceAppModel newMR = new MapReduceAppModel(metadata.getString("label"),metadata.getString("jarName"),metadata.getString("classPath"),mrArguments);
					mrHashMap.put(key, newMR);
					break;
				default:
					throw new Exception("Unknown component type! "+component.getString("component"));
				}
			}
		}catch(Exception e){
			response.setStatus(400);
			response.getWriter().write(e.getMessage());
			response.getWriter().close();
		}
		System.out.println("\n\n\n\n\n");
		
		JSONObject inports = jObj.getJSONObject("inports");
		JSONObject outports = jObj.getJSONObject("outports");
		String startKey = "";
		String endKey = "";
		try{
			startKey = getStartKey(inports);
			endKey = getEndKey(outports);		
		}catch(Exception e){
			response.setStatus(400);
			response.getWriter().write(e.getMessage());
			response.getWriter().close();
		}

		ArrayList<ArrayList<String>> stages = new ArrayList<ArrayList<String>>();			
		JSONArray connectionsArray = jObj.getJSONArray("connections");	
		try{
			stages = loadStages(connectionsArray,startKey,endKey,fileHashMap, sparkHashMap, mrHashMap);			
		}catch(Exception e){
			response.setStatus(400);
			if(e.getMessage() != null)
				response.getWriter().write(e.getMessage());
			response.getWriter().close();
		}
		
		//printStage(stages);
		UUID executionId = UUID.randomUUID();
		StagesExecutor executor = new StagesExecutor(stages, fileHashMap, sparkHashMap, mrHashMap,executionId);
		PrintWriter out = response.getWriter(); 
		out.println("SUBMITTED: "+ executionId.toString()); 
		out.flush();
		out.close();
		executor.run();
	}
	
	private ArrayList<ArrayList<String>> loadStages(
			JSONArray connectionsArray, String startKey, String endKey, HashMap<String, FileModel> fileHashMap, HashMap<String, SparkAppModel> sparkHashMap, HashMap<String, MapReduceAppModel> mrHashMap) throws Exception {
		int totalJobs = sizeOfElements(fileHashMap, sparkHashMap, mrHashMap);
		int consumedJobs = 0;
		int stage = 0;
		int forceTerminationCounter = 0;
		HashMap<String, ArrayList<String>> waitingMap = loadWaitingMap(connectionsArray); //Waiting map indicates required job before execute the key job.
		//ArrayList<String> connectionsEnd = new ArrayList<String>(); //Stages' connections end right here.
		ArrayList<ArrayList<String>> stages = new ArrayList<ArrayList<String>>();
		while(forceTerminationCounter < totalJobs && consumedJobs < totalJobs){
			ArrayList<String> lookingFor = new ArrayList<String>();	
			if(stage == 0){
				ArrayList<String> jobsInStage = new ArrayList<String>();
				jobsInStage.add(startKey); //The first stage will always be start key.
				stages.add(stage, jobsInStage);
				//connectionsEnd.add(startKey); //Stages' connections end right here.
				waitingMap = sourceAchieved(waitingMap, jobsInStage); //Remove this stage's jobs from waiting map.
				consumedJobs += jobsInStage.size();
				stage++;
			}else{
				//Previous target will be sources this time.
				for(String jobKey: stages.get(stage-1)){
					lookingFor.add(jobKey);
					 //Remove source from connections ends if the target has a source.
				}
				ArrayList<String> jobsInStage = findJobsBySources(connectionsArray, lookingFor);
				
				if (jobsInStage != null) {
					//Examine whether the job is ready to execute.
					ArrayList<String> removeList = new ArrayList<String>();
					for (String jobKey : jobsInStage) {
						if(waitingMap.get(jobKey) != null){
							//If it's not, remove it from this stage.
							if(waitingMap.get(jobKey).size() != 0){
								removeList.add(jobKey);
							}
						}
					}
					for (String removeKey : removeList) {
						jobsInStage.remove(removeKey);
					}
					/*for (String targetKey : jobsInStage) {
						for (String sourceKey : lookingFor) {
							ArrayList<String> tmpArrayList = new ArrayList<String>();
							tmpArrayList.add(sourceKey);
							if(findJobsBySources(connectionsArray, tmpArrayList).contains(targetKey)){
								connectionsEnd.remove(sourceKey);
							}
						}
					}*/
					stages.add(stage, jobsInStage);
					//connectionsEnd.addAll(jobsInStage);
					waitingMap = sourceAchieved(waitingMap, jobsInStage);//Remove achieved job from waiting list.
					consumedJobs += jobsInStage.size();					
					stage++;
				}else{
					throw new Exception("Connection not found! Check your graph!");
				}
			}
			forceTerminationCounter++;		
		}
		//System.out.println(forceTerminationCounter+" "+consumedJobs+" "+totalJobs);
		if(forceTerminationCounter > totalJobs || consumedJobs < totalJobs /*|| !(endValidation(connectionsEnd,connectionsArray,endKey))*/ || stages.size() == 0){
			throw new Exception("Faulty graph! Check connections!");
		}
		return stages;
	}
	/*
	private boolean endValidation(ArrayList<String> connectionsEnd, JSONArray connectionsArray, String endKey) {
		boolean result = false;
		connectionsEnd.remove(endKey);
		if(connectionsEnd.size() == 0){
			result = true;
		}else if(connectionsEnd.size() > 0){
			ArrayList<String> removeList = new ArrayList<String>();
			for (String key : connectionsEnd) {
				ArrayList<String> lookingFor = new ArrayList<String>();
				lookingFor.add(key);
				if(findJobsBySources(connectionsArray, lookingFor).contains(endKey)){
					removeList.add(key);
				}
			}
			for (String key : removeList) {
				connectionsEnd.remove(key);
			}
			if(connectionsEnd.size() ==0){
				result = true;
			}else{
				result = false;
			}
		}
		System.out.println(connectionsEnd.toString());
		return result;
	}*/

	private HashMap<String, ArrayList<String>> sourceAchieved(
			HashMap<String, ArrayList<String>> waitingMap, ArrayList<String> jobsInStage) {
		Iterator<String> testit = waitingMap.keySet().iterator();
		 while(testit.hasNext()){
			 String keyString = testit.next();
			 for (String jobKey : jobsInStage) {
				 if(waitingMap.get(keyString).contains(jobKey)){
					 waitingMap.get(keyString).remove(jobKey);
				 }
			}
		 }
		return waitingMap;
	}

	private HashMap<String, ArrayList<String>> loadWaitingMap(
			JSONArray connectionsArray) {
		HashMap<String, ArrayList<String>> waitingMap = new HashMap<String, ArrayList<String>>();
		for(int i = 0; i < connectionsArray.length(); i++){
			JSONObject connection = connectionsArray.getJSONObject(i);
			String sourceKey = connection.getJSONObject("src").getString("process");
			String targetKey = connection.getJSONObject("tgt").getString("process");
			if(waitingMap.get(targetKey) != null){
				waitingMap.get(targetKey).add(sourceKey);
			}else{
				ArrayList<String> newArrayList = new ArrayList<String>();
				newArrayList.add(sourceKey);
				waitingMap.put(targetKey, newArrayList);
			}
		}
		return waitingMap;
	}

	private String getEndKey(JSONObject outports) throws Exception {
		Iterator<String> outportsIterator = outports.keys();
		int outportsSize = 0;
		String endKey = "";
		while(outportsIterator.hasNext()){
			outportsSize++;
			if(outportsSize > 1){
				throw new Exception("Multiple terminations detected! Expected one termination!");
			}
			String key = outportsIterator.next();
			endKey = outports.getJSONObject(key).getString("process");
		}
		if(outportsSize == 0){
			throw new Exception("No termination detected! Expected one termination!");
		}
		return endKey;
	}

	private String getStartKey(JSONObject inports) throws Exception {
		String startKey = "";
		Iterator<String> inportsIterator = inports.keys();
		int inportsSize = 0;
		while(inportsIterator.hasNext()){
			inportsSize++;
			if(inportsSize > 1){
				throw new Exception("Multiple entries detected! Expected one entry!");				
			}
			String key = inportsIterator.next();
			startKey = inports.getJSONObject(key).getString("process");
		}
		if(inportsSize == 0){
			throw new Exception("No entry detected! Expected one entry!");
		}
		return startKey;
	}

	public void printFiles(HashMap<String, FileModel> fileHashMap) {
		 Iterator<String> testit = fileHashMap.keySet().iterator();
		 while(testit.hasNext()){
			 String key = testit.next();
			 System.out.println(key+" "+fileHashMap.get(key).getName()+" "+fileHashMap.get(key).getSource());
		 }
	}
	
	public void printSparks(HashMap<String, SparkAppModel> sparkHashMap){
		Iterator<String> testit = sparkHashMap.keySet().iterator();
		 while(testit.hasNext()){
			 String key = testit.next();
			 String argumentsString = "";
			 for (String e : sparkHashMap.get(key).getArguments()) {
				argumentsString += e;
			 }
			 System.out.println(key+" "+sparkHashMap.get(key).getName()+" "+sparkHashMap.get(key).getJarName()+" "+sparkHashMap.get(key).getClassPath()+" "+argumentsString);		
		 }
	}
	
	public void printMR(HashMap<String, MapReduceAppModel> mrHashMap){
		Iterator<String> testit = mrHashMap.keySet().iterator();
		 while(testit.hasNext()){
			 String key = testit.next();
			 String argumentsString = "";
			 for (String e : mrHashMap.get(key).getArguments()) {
				argumentsString += e;
			 }
			 System.out.println(key+" "+mrHashMap.get(key).getName()+" "+mrHashMap.get(key).getJarName()+" "+mrHashMap.get(key).getClassPath()+" "+argumentsString);		
		 }		
	}
	
	public void printWaitingMap(HashMap<String, ArrayList<String>> waitingMap){
		Iterator<String> testit = waitingMap.keySet().iterator();
		 while(testit.hasNext()){
			 String keyString = testit.next();
			 System.out.println(keyString+" waiting for: "+waitingMap.get(keyString).toString());
		 }
	}
	
	public void printStage(ArrayList<ArrayList<String>> stages){
		for (ArrayList<String> stage : stages) {
			System.out.println("Stage "+stages.indexOf(stage)+":");
			for (String jobKey : stage) {
				System.out.println("Job Key: "+jobKey);
			}
		}
	}
	
	public ArrayList<String> findJobsBySources(JSONArray connectionsArray, ArrayList<String> lookingFor){
		ArrayList<String> jobsFound = new ArrayList<String>();
		for(int i = 0; i < connectionsArray.length(); i++){
			JSONObject connection = connectionsArray.getJSONObject(i);
			String sourceKey = connection.getJSONObject("src").getString("process");
			String targetKey = connection.getJSONObject("tgt").getString("process");
			if(lookingFor.contains(sourceKey) && !jobsFound.contains(targetKey)){
				jobsFound.add(targetKey);
			}
		}
		return jobsFound;
	}
	
	public int sizeOfElements(HashMap<String, FileModel> fileHashMap,HashMap<String, SparkAppModel> sparkHashMap,HashMap<String, MapReduceAppModel> mrHashMap){
		int size = fileHashMap.size()+sparkHashMap.size()+mrHashMap.size();
		return size;		
	}
	public void printJSON(JSONObject jObj){
		Iterator<String> it = jObj.keys(); //gets all the keys
		while(it.hasNext())
		{
		    String key = (String) it.next(); // get key
		    Object o = jObj.get(key); // get value		   
		    System.out.println(key+" : "+o);
		}	
	}
}
