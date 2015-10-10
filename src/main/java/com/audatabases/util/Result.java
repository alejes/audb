package com.audatabases.util;

import java.util.EnumMap;
import java.util.List;

enum ErrorCodes {
	PARSE_ERROR
}

public class Result {
	private static final EnumMap<ErrorCodes, String> errorMessages;
	
	static {
		errorMessages = new EnumMap<ErrorCodes, String>(ErrorCodes.class);
		errorMessages.put(ErrorCodes.PARSE_ERROR, "Error parsing sql statement."); 
		// and so on 
	}
	
	public List<TableColumn> columns;
	public List<List<String> > rows; // TODO think about what it should be: may be tuples are better?
	
	
	private ErrorCodes errorCode;
}
