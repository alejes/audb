package audb.index;

import audb.page.PageStructure;
import audb.result.Result;
import audb.type.Type;

public class BTreeIndex extends Index {

    long mainPage;

    public BTreeIndex(long mainPage, PageStructure pageStructure) {
        super(pageStructure);
        this.mainPage = mainPage;
    }

    public void createIndex(Result elements, String[] indexNames,
        Type[] indexTypes, Order[] orders) throws Exception {

    }

    public boolean canResolve(String[] names, Type[] types) {
        return false;
    }

    public Result find(String[] names, Type[] types) {
        return null;
    }
}