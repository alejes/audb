package audb.command;

import audb.table.Table;
import audb.table.TableElement;
import audb.util.Pair;

import java.util.HashMap;
import java.util.Iterator;


public class EmptyCommand extends Command {

    private String tableName;

    public EmptyCommand(String tableName) {
        this.tableName = tableName;
    }

    public Pair<Table, Iterator<HashMap<String, TableElement>>> exec() throws Exception {

        if (!tableManager.hasTable(tableName))
            throw new Exception("No such table.");
        Table table = tableManager.getTable(tableName);

        return Pair.newPair(table, null);
    }
}