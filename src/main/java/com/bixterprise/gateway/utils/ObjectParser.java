package com.bixterprise.gateway.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectParser {

	protected static ObjectMapper mapper = new ObjectMapper();
	
	public static  <T> T parse(String value, Class<T> t){
		try {
			return mapper.readValue(value, t);
		} catch (JsonProcessingException e) {
			return null;
		}
	}
}
