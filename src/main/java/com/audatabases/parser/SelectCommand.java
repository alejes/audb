package com.audatabases.parser;

import java.util.List;

public class SelectCommand extends Command {
	public SelectCommand(String tableName, List<String> columnNames) {
		super(tableName);
	}
}	
