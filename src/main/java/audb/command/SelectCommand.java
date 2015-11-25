package audb.command;

import audb.table.Table;

public class SelectCommand extends Command {

    private String tableName;

	public SelectCommand(String tableName) {
		this.tableName = tableName;
	}

    public Table exec() throws Exception {
        if(!tableManager.hasTable(tableName))
            throw new Exception("InsertCommand.java");
        return tableManager.getTable(tableName);
    }

}
