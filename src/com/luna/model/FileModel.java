package com.luna.model;

public class FileModel{
	String name;
	String source;

	public FileModel(String name, String source){
		this.name = name;
		this.source = source;
	}
	public String getName () {
		return name;
	}
	public String getSource() {
		return source;
	}
}
