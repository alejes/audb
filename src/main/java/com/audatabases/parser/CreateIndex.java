package com.audatabases.parser;

import java.util.List;

enum IndexOption {
	BTREE,
	HASH
}

public class CreateIndex extends Command {
	public CreateIndex(String tableName, List<String> columnNames, 
			IndexOption indexOption) {
		super(tableName);
	}
}
