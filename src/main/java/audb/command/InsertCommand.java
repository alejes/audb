package audb.command;

import audb.table.Table;


public class InsertCommand extends Command {

    private String tableName;
    private Object[] data;

	public InsertCommand(String tableName, Object[] data) {
		this.tableName = tableName;
		this.data = data;
	}

    public Table exec() throws Exception {

    	if(!tableManager.hasTable(tableName))
			throw new Exception("No such table.");
		Table table = tableManager.getTable(tableName);
		table.addRecord(data);

    	return table;
    }

}
