package com.luna.model;

public class JobModel {
	String target;
	String source;
	public JobModel(String source, String target){
		this.target = target;
		this.source = source;
	}
	
	public String getSource(){
		return this.source;
	}
	
	public String getTarget() {
		return this.target;
	}
}
