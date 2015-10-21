package audb.command;

import audb.result.Result;
import audb.table.TableManager;


public abstract class Command {

    protected static TableManager tableManager = null;

    public abstract Result exec() throws Exception;

    public static void setTableManager(TableManager tm) {
        tableManager = tm;
    }

}
