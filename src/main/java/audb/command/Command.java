package audb.command;

import audb.result.Result;
import audb.table.TableManager;


public abstract class Command {

    protected TableManager tableManager = null;

    public abstract Result exec() throws Exception;

    public void setTableManager(TableManager tm) {
        tableManager = tm;
    }

}
