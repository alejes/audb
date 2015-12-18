package audb.command;

import java.util.HashMap;
import java.util.Iterator;

import audb.table.Table;
import audb.table.TableElement;
import audb.util.Pair;
import audb.table.TableLine;


public class UpdateCommand extends Command {

    private Iterator<HashMap<String, TableElement>> iterator;    
    private HashMap<String, Object> values;

    public UpdateCommand(Iterator<HashMap<String, TableElement>> it, 
        HashMap<String, Object> v) {
        this.iterator = it;
        this.values = v;
    }

    public Pair<Table, Iterator<HashMap<String, TableElement>>> exec() throws Exception {

        while (iterator.hasNext()) {
            TableLine curr = (TableLine)iterator.next();
            String tableName = curr.getTableName();
            
            if(!tableManager.hasTable(tableName))
                throw new Exception("No such table.");
            Table table = tableManager.getTable(tableName);
            String[] names = table.getNames();
            Object[] newValues = new Object[names.length];
            for (int i = 0; i < names.length; ++i)
                newValues[i] = values.containsKey(names[i]) ? values.get(names[i]) : null;
            table.write(curr.getPageNumber(), curr.getOffset(), newValues);
        }
        

        return null;
    }

}
