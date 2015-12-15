package audb.command;

import java.util.HashMap;
import java.util.Iterator;

import audb.table.Table;
import audb.table.TableElement;
import audb.type.Type;
import audb.util.Pair;


public class CreateTableCommand extends Command {

    private String tableName;
    private Type[] types;
    private String[] names;

	public CreateTableCommand(String tableName, Type[] types, String[] names) {
		this.tableName = tableName;
		this.names = names;
		this.types = types;
	}

    public Pair<Table, Iterator<HashMap<String, TableElement>>> exec() throws Exception {
		// if(tableManager.hasTable(tableName))
		// 	throw new Exception("CreateTableCommand.java");
		tableManager.createTable(tableName, types, names);				
		
    	return null;
    }

}
