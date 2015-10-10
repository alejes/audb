package com.audatabases.io;

import java.util.Scanner;

import com.database.util.Result;

public class IOHandler implements IOInterface {
	Scanner terminalInput = new Scanner(System.in);
	
	public String readCommand() {
		String commandStr = terminalInput.nextLine();
		return commandStr;
	}

	public void writeResult(Result r) {
		// TODO Auto-generated method stub
		
	}

}
