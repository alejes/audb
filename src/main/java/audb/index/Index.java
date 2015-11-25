package audb.index;

import audb.page.PageStructure;
import audb.type.Type;
import audb.table.Table;


public abstract class Index {

    public enum Order {
        ASC, DESC
    }

    private PageStructure pageStructure;
    private long mainPage;

    public Index(Table table, long mainPage, PageStructure pageStructure) {
        this.pageStructure = pageStructure;
        this.mainPage = mainPage;
    }

    public abstract void init() throws Exception;

    public abstract void create(String[] names, Order[] orders) throws Exception;

    public abstract void add(Object[] data);

    public abstract boolean canResolve(String[] names);

    public abstract Table find(String[] names);
}