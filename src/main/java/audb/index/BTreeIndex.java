package audb.index;

import audb.page.PageStructure;
import audb.type.Type;
import audb.table.Table;

public class BTreeIndex extends Index {

    public BTreeIndex(Table table, long mainPage, PageStructure pageStructure) {
        super(table, mainPage, pageStructure);
    }

    public void init() throws Exception {

    }

    public void create(String[] names, Order[] orders) throws Exception {

    }

    public void add(Object[] data) {

    }


    public boolean canResolve(String[] names) {
        return false;
    }

    public Table find(String[] names) {
        return null;
    }
}