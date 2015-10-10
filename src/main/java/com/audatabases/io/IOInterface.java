package com.audatabases.io;

import com.database.util.Result;

public interface IOInterface {
	String readCommand();
	void writeResult(Result r);
}
