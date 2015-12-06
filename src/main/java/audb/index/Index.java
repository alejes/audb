package audb.index;

import java.util.Arrays;
import java.util.List;

import audb.command.Constraint;
import audb.page.PageStructure;
import audb.table.Table;
import audb.table.TableElement;
import audb.util.Pair;


public abstract class Index {

    public enum Order {
        ASC, DESC
    }

    protected PageStructure pageStructure;
    protected int mainPage;
    protected List<String> keyColumnsNames;
    protected Order[] orders;
    
    public Index(Table table, int mainPage, PageStructure pageStructure) {
        this.pageStructure = pageStructure;
        this.mainPage = mainPage;
    }

    public abstract void init() throws Exception;

    public void create(String[] names, Order[] orders) {
    	keyColumnsNames = Arrays.asList(names);
    	this.orders = orders;
    }

    public abstract void add(TableElement[] data, int pageNumber, int offset);
    public abstract List<Pair<String, Constraint>> filterNonIndexedConstraints(
    		List<Pair<String, Constraint>> constrs);
    
    public abstract boolean canResolve(String[] names);
    public abstract boolean canResolve(List<Pair<String, Constraint>> constrs);
    public abstract List<IndexValueInstance> find(List<Pair<String, Constraint>> constraints);
    public abstract List<IndexValueInstance> find(String columnNames[], Constraint[] constraints);
}