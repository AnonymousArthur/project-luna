package com.luna.model;

import java.util.ArrayList;

public class SparkAppModel{
	String name;
	String jarName;
	String classPath;
	ArrayList<String> arguments;
	
	public SparkAppModel(String name, String jarName,String classPath,ArrayList<String> arguments){
		this.arguments = new ArrayList<String>();
		this.name = name;
		this.jarName = jarName;
		this.classPath = classPath;
		this.arguments = arguments;
	}
	
	public String getName() {
		return name;
	}
	
	public String getJarName(){
		return jarName;
	}
	
	public String getClassPath(){
		return classPath;
	}
	
	public ArrayList<String> getArguments(){
		return arguments;
	}
}
