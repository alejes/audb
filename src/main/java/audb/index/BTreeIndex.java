package audb.index;

import audb.page.PageStructure;
import audb.type.Type;
import audb.table.Table;

public class BTreeIndex extends Index {

    long mainPage;

    public BTreeIndex(long mainPage, PageStructure pageStructure) {
        super(pageStructure);
        this.mainPage = mainPage;
    }

    public void createIndex(Table elements, String[] indexNames,
        Type[] indexTypes, Order[] orders) throws Exception {

    }

    public boolean canResolve(String[] names, Type[] types) {
        return false;
    }

    public Table find(String[] names, Type[] types) {
        return null;
    }
}