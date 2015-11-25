package audb.index;

import audb.page.PageStructure;
import audb.result.Result;
import audb.type.Type;


public abstract class Index {

    public enum Order {
        ASC, DESC
    }

    private PageStructure pageStructure;

    public Index(PageStructure pageStructure) {
        this.pageStructure = pageStructure;
    }

    public abstract void createIndex(Result elements, String[] indexNames,
        Type[] indexTypes, Order[] orders) throws Exception;

    public abstract boolean canResolve(String[] names, Type[] types);

    public abstract Result find(String[] names, Type[] types);
}