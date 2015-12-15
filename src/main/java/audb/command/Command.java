package audb.command;

import audb.table.Table;
import audb.table.TableElement;
import audb.table.TableManager;
import audb.util.Pair;

import java.util.HashMap;
import java.util.Iterator;


public abstract class Command {

    protected static TableManager tableManager = null;

    public static TableManager getTableManager() {
        return tableManager;
    }

    public static void setTableManager(TableManager tm) {
        tableManager = tm;
    }

    public abstract Pair<Table, Iterator<HashMap<String, TableElement>>> exec() throws Exception;

}
