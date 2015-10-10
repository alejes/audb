package com.audatabases.core;

import pages.PageManager;

import com.audatabases.parser.CreateIndex;
import com.audatabases.parser.CreateTableCommand;
import com.audatabases.parser.InsertCommand;
import com.audatabases.parser.SelectCommand;
import com.audatabases.parser.UpdateCommand;
import com.database.util.Result;

public class Database {
	private PageManager pageManager = null;
	private String name;
	
	public Database(String dbName) {
		name = dbName;
		// TODO Auto-generated constructor stub
	}
	
	Result evaluateCommand(CreateTableCommand command) {
		
		return null;
	}
	
	Result evaluateCommand(InsertCommand command) {
		
		return null;
	}
	
	Result evaluateCommand(SelectCommand command) {
		
		return null;
	}
	
	Result evaluateCommand(CreateIndex command) {
		
		return null;
	}
	
	Result evaluateCommand(UpdateCommand command) {
		
		return null;
	}
	
	Result performOperationsBeforeShutDown() {
		
		return null;
	}
}
