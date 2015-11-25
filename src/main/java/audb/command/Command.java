package audb.command;

import audb.table.Table;
import audb.table.TableManager;


public abstract class Command {

    protected static TableManager tableManager = null;

    public abstract Table exec() throws Exception;

    public static void setTableManager(TableManager tm) {
        tableManager = tm;
    }

}
