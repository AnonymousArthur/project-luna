package com.luna.controller.hadoop;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class HDFSUpload {
	private File file ;
	final String PATH = "/usr/local/hadoop-2.7.2/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin";
	final String JAVA_HOME = System.getProperty("java.home");
	final String HADOOP_HOME = "/usr/local/hadoop-2.7.2";
	final String HDFS = HADOOP_HOME+"/bin/hdfs";
	final String DFS = "dfs";
	final String PUT = "-put";
	final String CATALINA = System.getProperty("catalina.home");
	final String FILES = CATALINA+"/webapps/ROOT/Luna/Pending-Files/";
	public HDFSUpload(File file){
		this.file = file;
	}
	public boolean copy2HDFS() throws Exception{
		ArrayList<String> commandList = new ArrayList<String>();
		commandList.add(HDFS);
		commandList.add(DFS);
		commandList.add(PUT);
		commandList.add(FILES+file.getName());
		String[] command = commandList.toArray(new String[0]);
		ProcessBuilder pb = new ProcessBuilder(Arrays.asList(command));			
		Map<String, String> envs = pb.environment();
		envs.put("PATH", PATH);
		envs.put("JAVA_HOME", JAVA_HOME);
		envs.put("HADOOP_HOME", HADOOP_HOME);
		Process p = pb.start();
		String output = loadStream(p.getInputStream());
		String error = loadStream(p.getErrorStream());
		System.out.println(output+"\n"+error);
		p.waitFor();
		return false;		
	}
	
	private String loadStream(InputStream s) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(s));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null){
            sb.append(line).append("\n");
        }
        br.close();
        return sb.toString();
 }
}
