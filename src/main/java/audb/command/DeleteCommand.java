package audb.command;

import audb.parser.Parser;
import audb.table.Table;
import audb.table.TableElement;
import audb.table.TableLine;
import audb.util.Pair;

import java.util.HashMap;
import java.util.Iterator;


public class DeleteCommand extends Command {

    private Iterator<HashMap<String, TableElement>> iterator;    

    public DeleteCommand(Iterator<HashMap<String, TableElement>> it) {
        this.iterator = it;
    }

    public Pair<Table, Iterator<HashMap<String, TableElement>>> exec() throws Exception {
        int affectedRows = 0;
        while (iterator.hasNext()) {
            TableLine curr = (TableLine)iterator.next();
            String tableName = curr.getTableName();
            
            if(!tableManager.hasTable(tableName))
                throw new Exception("No such table.");
            Table table = tableManager.getTable(tableName);
            String[] names = table.getNames();
            table.delete(curr.getPageNumber(), curr.getOffset());
            ++affectedRows;
        }
        Parser.affectedRows = affectedRows;

        return null;
    }

}
