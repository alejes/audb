package audb.command;

import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

import audb.table.Table;
import audb.table.TableElement;
import audb.type.Type;
import audb.result.JoinIterator;
import audb.util.*;


public class JoinCommand extends Command {

    private String tableName;
    private Iterator iterator;
    private List<Pair<String, String>> names;
    private List<Third<String, Constraint, String>> constraints;

    public JoinCommand(Iterator<HashMap<String, TableElement>> it, List<Pair<String, String>> names, 
        List<Third<String, Constraint, String>> constraints, String tableName) {
        
        this.tableName = tableName;
        this.iterator = it;
        this.names = names;
        this.constraints = constraints;

    }

    public Pair<Table, Iterator<HashMap<String, TableElement>>> exec() throws Exception {
        if(!tableManager.hasTable(tableName))
            throw new Exception("Unknown table " + tableName);
        Table table = tableManager.getTable(tableName);
        Iterator<HashMap<String, TableElement>> it = new JoinIterator(iterator, names, constraints, table);
        
        return Pair.newPair((Table)null, it);
    }

}
