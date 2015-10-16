package audb.parser;

import audb.command.*;


public class Parser {

    public Command getCommand(String str) {

        System.out.println("Parser had \"" + str + "\" on input and ignored it.");
        System.out.println("FullScan for table1 returned instead.");

        return new SelectCommand("table1");
    }

}
