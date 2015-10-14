package audb.parser;

import audb.command.*;


public class Parser {

    public Command getCommand(String str) {

        System.out.println("Parser had \"" + str + "\" on input and ignored it.");

    	Command command = new CreateTableCommand();

        return command;
    }

}
