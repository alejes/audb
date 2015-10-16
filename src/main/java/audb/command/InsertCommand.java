package audb.command;

import audb.result.Result;
import audb.table.TableManager;
import audb.table.Table;


public class InsertCommand implements Command {

	private String tableName;
	private Object[] data;

	public InsertCommand(String tableName, Object[] data) {
		this.tableName = tableName;
		this.data = data;
	}

    public Result exec() throws Exception {

    	if(!tableManager.hasTable(tableName))
			throw new Exception("InsertCommand.java");
		Table table = tableManager.getTable(tableName);
		for(int i = 0; i < table.getTypes().length; i++) {
			if(!table.getTypes()[i].isValid(data[i]))
				throw new Exception("InsertCommand.java");

		}
		table.addRecord(data);

    	return null;
    }

}
