package audb.index;

import audb.command.Constraint;
import audb.page.PageStructure;
import audb.table.Table;
import audb.table.TableElement;

public class BTreeIndex extends Index {

    public BTreeIndex(Table table, long mainPage, PageStructure pageStructure) {
        super(table, mainPage, pageStructure);
    }

    public void init() throws Exception {

    }

    public void create(String[] names, Order[] orders) throws Exception {

    }

    public void add(TableElement[] data) {
    	
    }

    public void add(Object[] data) {
    	
    }

    public boolean canResolve(String[] columnNames) {
        return false;
    }

    public Table find(String columnNames[], Constraint[] constraints) {
        return null;
    }
}