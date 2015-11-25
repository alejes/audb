package audb.command;

import audb.table.Table;
import audb.type.Type;


public class CreateTableCommand extends Command {

    private String tableName;
    private Type[] types;
    private String[] names;

	public CreateTableCommand(String tableName, Type[] types, String[] names) {
		this.tableName = tableName;
		this.names = names;
		this.types = types;
	}

    public Table exec() throws Exception {
		if(tableManager.hasTable(tableName))
			throw new Exception("CreateTableCommand.java");
		tableManager.createTable(tableName, types, names);				

    	return null;
    }

}
