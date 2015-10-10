package com.audatabases.parser;

import java.util.List;

public class InsertCommand extends Command {
	public InsertCommand(String tableName, List<String> columnNames, List<String> values) {
		super(tableName);
	}
}
