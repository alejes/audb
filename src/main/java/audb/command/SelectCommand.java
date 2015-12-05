package audb.command;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import audb.table.Table;
import audb.table.TableElement;
import audb.util.Pair;

public class SelectCommand extends Command {

    private String tableName;
    private final List<Pair<String, Constraint>> constraints;
    
	public SelectCommand(String tableName, List<Pair<String, Constraint>> constraints) {
		this.tableName = tableName;
		this.constraints = constraints;
	}

    public Pair<Table, Iterator<HashMap<String, TableElement>>> exec() throws Exception {
        if(!tableManager.hasTable(tableName))
            throw new Exception("InsertCommand.java");
        if (constraints.size() == 0)
        	return Pair.newPair(null, tableManager.getTable(tableName).iterator());
        
        return Pair.newPair(null, tableManager.getTable(tableName).select(constraints));
        
    }

}
