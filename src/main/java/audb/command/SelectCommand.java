package audb.command;

import audb.result.Result;
import audb.result.FullScanResult;
import audb.table.TableManager;


public class SelectCommand implements Command {

	private String tableName;

	public SelectCommand(String tableName) {
		this.tableName = tableName;
	}

    public Result exec() throws Exception {
    	if(!tableManager.hasTable(tableName))
			throw new Exception("InsertCommand.java");
		return new FullScanResult(tableManager.getTable(tableName));
    }


}
