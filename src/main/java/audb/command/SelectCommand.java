package audb.command;

import audb.table.Table;
import audb.table.TableElement;
import audb.util.Pair;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class SelectCommand extends Command {

    private final List<Pair<String, Constraint>> constraints;
    private String tableName;

    public SelectCommand(String tableName, List<Pair<String, Constraint>> constraints) {
        this.tableName = tableName;
        this.constraints = constraints;
    }

    public Pair<Table, Iterator<HashMap<String, TableElement>>> exec() throws Exception {
        if(!tableManager.hasTable(tableName))
            throw new Exception("Unknown table " + tableName);
        if (constraints.size() == 0)
        	return Pair.newPair(tableManager.getTable(tableName), tableManager.getTable(tableName).iterator());
        
        return Pair.newPair(tableManager.getTable(tableName), tableManager.getTable(tableName).select(constraints));
        
    }

}
