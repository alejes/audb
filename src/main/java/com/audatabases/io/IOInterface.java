package com.audatabases.io;

import com.audatabases.util.Result;

public interface IOInterface {
	String readCommand();
	void writeResult(Result r);
}
