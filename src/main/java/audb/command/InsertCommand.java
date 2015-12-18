package audb.command;

import audb.table.Table;
import audb.table.TableElement;
import audb.util.Pair;

import java.util.HashMap;
import java.util.Iterator;


public class InsertCommand extends Command {

    private String tableName;
    private Object[] data;

	public InsertCommand(String tableName, Object[] data) {
		this.tableName = tableName;
		this.data = data;
	}

    public Pair<Table, Iterator<HashMap<String, TableElement>>> exec() throws Exception {

    	if(!tableManager.hasTable(tableName))
			throw new Exception("Unknown table " + tableName);
		Table table = tableManager.getTable(tableName);
		table.addRecord(data);

    	return Pair.newPair(table, null);
    }

}
