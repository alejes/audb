package audb.command;

import audb.parser.Parser;
import audb.table.Table;
import audb.table.TableElement;
import audb.table.TableLine;
import audb.util.Pair;

import java.util.HashMap;
import java.util.Iterator;


public class UpdateCommand extends Command {

    private Iterator<HashMap<String, TableElement>> iterator;    
    private HashMap<String, Object> values;

    public UpdateCommand(Iterator<HashMap<String, TableElement>> it, 
        HashMap<String, Object> v) {
        this.iterator = it;
        this.values = v;
    }

    public Pair<Table, Iterator<HashMap<String, TableElement>>> exec() throws Exception {
        int affectedRows = 0;
        while (iterator.hasNext()) {
            TableLine curr = (TableLine)iterator.next();
            if (curr.isDeleted())
                continue;
            String tableName = curr.getTableName();
            
            if(!tableManager.hasTable(tableName))
                throw new Exception("No such table " + tableName + ".");
            Table table = tableManager.getTable(tableName);
            String[] names = table.getNames();
            Object[] newValues = new Object[names.length];
            // TODO index should be updated also
            for (int i = 0; i < names.length; ++i)
                newValues[i] = values.containsKey(names[i]) ? values.get(names[i]) : null;
            table.write(curr.getPageNumber(), curr.getOffset(), newValues);
            ++affectedRows;
        }
        Parser.affectedRows = affectedRows;

        return null;
    }

}
