package audb.command;

import audb.result.Result;
import audb.table.TableManager;


public interface Command {

    public Result exec() throws Exception;

    public void setTableManager(TableManager tm);

}
