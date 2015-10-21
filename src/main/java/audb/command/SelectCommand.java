package audb.command;

import audb.result.FullScanResult;
import audb.result.Result;
import audb.table.PageFullScan;


public class SelectCommand implements Command {

	private String tableName;

	public SelectCommand(String tableName) {
		this.tableName = tableName;
        // PageFullScan pfs = new PageFullScan(tableManager.getTable(tableName));
        // while(pfs.hasNext()) {
        //     System.err.println("num " + pfs.getNext().getPageNumber());
        // }
	}

    public Result exec() throws Exception {
        // return null;
    	if(!tableManager.hasTable(tableName))
			throw new Exception("InsertCommand.java");
		return new FullScanResult(tableManager.getTable(tableName));
    }


}
