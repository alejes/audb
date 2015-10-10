package com.audatabases.core;

import javax.management.openmbean.OpenDataException;

import com.audatabases.parser.ChooseDatabase;
import com.audatabases.parser.Command;
import com.audatabases.parser.ExitCommand;
import com.audatabases.util.Result;

public class DatabaseHandler {
	Database database = null;
	
	private Result closeDataBase() {
		return database.commitChanges();
	}
	
	public Result evaluateCommand(ChooseDatabase command) {
		if (database != null) {
			Result commitResult = closeDataBase();
			database = null;
		}
		
		database = new Database(command.getTableName());
		return null;
	}
	
	public Result evaluateCommand(Command command) {
		
		return null;
	}
	
	public Result evaluateCommand(ExitCommand command) {
		database.evaluateCommand(command);
		return null;
	}
	
}
