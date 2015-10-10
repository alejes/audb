package com.audatabases.parser;

import java.util.List;

import com.database.util.TableColumn;

public class CreateTableCommand extends Command {
	
	CreateTableCommand(String tableName, List<TableColumn> columns) {
		super(tableName);
	}

}
