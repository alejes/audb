package audb.command;

import java.util.HashMap;
import java.util.Iterator;

import audb.table.Table;
import audb.table.TableElement;
import audb.table.TableManager;
import audb.util.Pair;


public abstract class Command {

    protected static TableManager tableManager = null;

    public abstract Pair<Table, Iterator<HashMap<String, TableElement>>> exec() throws Exception;

    public static void setTableManager(TableManager tm) {
        tableManager = tm;
    }

}
