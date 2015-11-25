package audb.index;

import audb.command.Constraint;
import audb.page.PageStructure;
import audb.table.Table;
import audb.table.TableElement;


public abstract class Index {

    public enum Order {
        ASC, DESC
    }

    private PageStructure pageStructure;
    private long mainPage;
    private String[] columnNames;
    private Order[] orders;
    
    public Index(Table table, long mainPage, PageStructure pageStructure) {
        this.pageStructure = pageStructure;
        this.mainPage = mainPage;
    }

    public abstract void init() throws Exception;

    public abstract void create(String[] names, Order[] orders) throws Exception;

    public abstract void add(TableElement[] data);

    public abstract void add(Object[] data);
    
    public abstract boolean canResolve(String[] names);

    public abstract Table find(String columnNames[], Constraint[] constraints);
}