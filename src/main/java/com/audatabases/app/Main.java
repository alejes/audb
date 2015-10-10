package com.audatabases.app;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.audatabases.core.Database;
import com.audatabases.core.DatabaseHandler;
import com.audatabases.io.IOHandler;
import com.audatabases.io.IOInterface;
import com.audatabases.parser.Command;
import com.audatabases.parser.Parser;

public class Main {
    
    private static String version = "AUDB 0.0.1 ";

    static {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Date date = new Date();
        version += dateFormat.format(date); 
    }

    public static void main(String[] args) {
        System.out.println(version);
        
        IOInterface ui = new IOHandler();
        Parser parser = new Parser();
        DatabaseHandler dbHandler = new DatabaseHandler();
        
        while (true) {
        	System.out.print("> ");
        	String commandString = ui.readCommand();
        	Command command = parser.parse(commandString);
        	if (null == dbHandler.evaluateCommand(command))
        		break;
        }
        
        System.out.println("Bye!");
    }

}
