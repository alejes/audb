package audb.command;

import audb.type.Type;
import audb.result.Result;
import audb.table.TableManager;


public class CreateTableCommand extends Command {

    private String tableName;
    private Type[] types;
    private String[] names;

	public CreateTableCommand(String tableName, Type[] types, String[] names) {
		this.tableName = tableName;
		this.names = names;
		this.types = types;
	}

    public Result exec() throws Exception {
		if(tableManager.hasTable(tableName))
			throw new Exception("CreateTableCommand.java");
		tableManager.createTable(tableName, types, names);				

    	return null;
    }

}
