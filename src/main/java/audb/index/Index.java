package audb.index;

import audb.page.PageStructure;
import audb.type.Type;
import audb.table.Table;


public abstract class Index {

    public enum Order {
        ASC, DESC
    }

    private PageStructure pageStructure;

    public Index(PageStructure pageStructure) {
        this.pageStructure = pageStructure;
    }

    public abstract void createIndex(Table elements, String[] indexNames,
        Type[] indexTypes, Order[] orders) throws Exception;

    public abstract boolean canResolve(String[] names, Type[] types);

    public abstract Table find(String[] names, Type[] types);
}