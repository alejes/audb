package com.audatabases.parser;


public abstract class Command {
	protected String tableName;
	
	protected Command(String tableName) {
		this.tableName = tableName;
	}
}
