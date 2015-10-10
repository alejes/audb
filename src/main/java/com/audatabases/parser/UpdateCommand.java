package com.audatabases.parser;

import java.util.List;

import com.audatabases.util.RowsConstraint;

public class UpdateCommand extends Command {
	
	// TODO in future, add support for multiple constraints, connected
	// into some logical expression with AND, NOT, OR
	// (e.g. UPDATE ... WHERE (col1 = 3 AND col2 > "abba") OR col1 = 9)
	public UpdateCommand(String tableName, List<String> setColNames, 
			List<String> setColValues, RowsConstraint constraints) {
		super(tableName);
		// TODO Auto-generated constructor stub
	}
}
