package audb.command;

import audb.result.FullScanResult;
import audb.result.Result;
import audb.table.PageFullScan;
import audb.table.TableManager;


public class SelectCommand implements Command {

    private TableManager tableManager = null;
    private String tableName;

	public SelectCommand(String tableName) {
		this.tableName = tableName;
	}

    public Result exec() throws Exception {
        if(!tableManager.hasTable(tableName))
            throw new Exception("InsertCommand.java");
        return new FullScanResult(tableManager.getTable(tableName));
    }

    public void setTableManager(TableManager tm) {
        tableManager = tm;
    }


}
