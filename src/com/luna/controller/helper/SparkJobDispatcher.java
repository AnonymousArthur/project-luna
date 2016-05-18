package com.luna.controller.helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class SparkJobDispatcher implements Runnable{
	String appName;
	String jarFile;
	String classPath;
	ArrayList<String> arguments;
	final String PATH = "/usr/local/spark-1.4.1/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin";
	final String JAVA_HOME = System.getProperty("java.home");
	final String SPARK_HOME = "/usr/local/spark-1.4.1";
	final String SPARK = SPARK_HOME+"/bin/spark-submit";
	final String JAR = "jar";
	final String CATALINA = System.getProperty("catalina.home");
	final String SPARK_JARS = CATALINA+"/webapps/ROOT/Luna/Spark-Jars/";
	final String LOG_BASE = CATALINA+"/webapps/ROOT/Luna/Logs/";
	final String FILES = CATALINA+"/webapps/ROOT/Luna/Pending-Files/";
	final String CLASS_PREFIX = "--class";
	final String MASTER = "--master";
	final String RESOURCES = "local[*]";
	UUID jobExecutionId;
	File executionSpace;
	String LOG;
	String STDOUT;
	String STDERR;
	
	public SparkJobDispatcher(String executionId, String appName, String jarFile, String classPath, ArrayList<String> arguments, UUID jobExecutionId) {
		this.appName = appName;
		this.jarFile = jarFile;
		this.classPath = classPath;
		this.arguments = arguments;
		this.jobExecutionId = jobExecutionId; 
		executionSpace = new File(LOG_BASE+"/"+executionId+"/"+jobExecutionId);
		executionSpace.mkdirs();
	}
	
	
	@Override
	public void run() {
		try{
			LOG = executionSpace.getCanonicalPath().toString()+"/LOG";
			STDOUT = executionSpace.getCanonicalPath().toString()+"/STDOUT";
			STDERR = executionSpace.getCanonicalPath().toString()+"/STDERR";
			File log = new File(LOG);
			File stdout = new File(STDOUT);
			File stderr = new File(STDERR);
			log.createNewFile();
			stdout.createNewFile();
			stderr.createNewFile();
			BufferedWriter logBuffer = new BufferedWriter(new FileWriter(log));
			BufferedWriter stdoutBuffer = new BufferedWriter(new FileWriter(stdout));
			BufferedWriter stderrBuffer = new BufferedWriter(new FileWriter(stderr));
			ArrayList<String> commandList = new ArrayList<String>();
			commandList.add(SPARK);
			logBuffer.write("Spark Path: "+SPARK+"\n");
			logBuffer.flush();
			commandList.add(CLASS_PREFIX);
			commandList.add(classPath);
			logBuffer.write(CLASS_PREFIX+" "+classPath+"\n");
			logBuffer.flush();
			commandList.add(MASTER);
			commandList.add(RESOURCES);
			logBuffer.write(MASTER+" "+RESOURCES+"\n");
			logBuffer.flush();
			commandList.add(SPARK_JARS+jarFile);
			logBuffer.write("Jar Path: "+SPARK_JARS+jarFile+"\n");
			logBuffer.flush();
			logBuffer.write("Arguments: \n");
			logBuffer.flush();
			for (String argument : arguments) {				
				commandList.add(argument.replaceFirst("^-f:", FILES));
				logBuffer.write("Argument: "+argument+"\n");
				logBuffer.flush();
			}
			String[] command = commandList.toArray(new String[0]);
			ProcessBuilder pb = new ProcessBuilder(Arrays.asList(command));			
			Map<String, String> envs = pb.environment();
			envs.put("PATH", PATH);
			envs.put("JAVA_HOME", JAVA_HOME);
			envs.put("SPARK_HOME", SPARK_HOME);
			System.out.println(appName+" Start.");
			logBuffer.write(appName+" Start.\n");
			logBuffer.flush();
			Process p = pb.start();
			String output = loadOutStream(p.getInputStream(),stdoutBuffer);
			String error = loadErrStream(p.getErrorStream(),stderrBuffer);		
			p.waitFor();
			System.out.println(appName+" Finished.");
			logBuffer.write(appName+" Finished.\n");
			logBuffer.flush();
			logBuffer.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}		
	}
	private String loadOutStream(InputStream s, BufferedWriter stdoutBuffer) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(s));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null){
            sb.append(line).append("\n");
            System.out.println(line);
            stdoutBuffer.write(line+"\n");
            stdoutBuffer.flush();
        }
        br.close();
        stdoutBuffer.close();
        return sb.toString();
	}
	private String loadErrStream(InputStream s, BufferedWriter stderrBuffer) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(s));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null){
            sb.append(line).append("\n");
            //System.out.println(line);
            stderrBuffer.write(line+"\n");
            stderrBuffer.flush();
        }
        br.close();
        stderrBuffer.close();
        return sb.toString();
	}
}
