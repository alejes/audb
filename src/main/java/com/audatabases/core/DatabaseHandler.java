package com.audatabases.core;

import javax.management.openmbean.OpenDataException;

import com.audatabases.parser.Command;
import com.audatabases.parser.ExitCommand;
import com.database.util.Result;

public class DatabaseHandler {
	Database database = null;
	
	private Result closeDataBase() {
		// commit all changes to HDD
		return null;
	}
	
	private Result OpenDatabase(String databaseName) {
		if (database != null) {
			closeDataBase();
		}
		
		return null;
	}
	
	public Result evaluateCommand(Command command) {
		
		return null;
	}
	
	public Result evaluateCommand(ExitCommand command) {
		closeDataBase();
		return null;
	}
	
}
