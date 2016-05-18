package com.luna.controller.helper;

import org.json.JSONObject;

public class escapeJSON {
	public escapeJSON(){}
	public JSONObject getEscapeJSONForExample(final String inputFile){
		JSONObject obj = new JSONObject();
		obj.put("input", new JSONObject().put("string", inputFile));
		return obj;
	}
}
