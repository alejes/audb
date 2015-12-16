package audb.index;

import java.util.Arrays;
import java.util.HashMap;
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
    
    public class KeySizeException extends Exception {
		private static final long serialVersionUID = 1L;
    	
		@Override
		public String getMessage() {
			return "Key is too big to fit in page size";
		}
    }

	public static class IndexFindResults {
		public HashMap<String, Constraint> bottomBounds;
		public HashMap<String, Constraint> upperBounds;
		public HashMap<String, Constraint> exactBounds;
		public HashMap<String, List<Constraint>> exactNotBounds;
		public List<IndexValueInstance> pagesAndOffstes;
		
		IndexFindResults(HashMap<String, Constraint> bottomBounds,
		HashMap<String, Constraint> upperBounds,
		HashMap<String, Constraint> exactBounds,
		HashMap<String, List<Constraint>> exactNotBounds,
		List<IndexValueInstance> pagesAndOffsets) {
			this.bottomBounds = bottomBounds;
			this.upperBounds = upperBounds;
			this.exactBounds = exactBounds;
			this.exactNotBounds = exactNotBounds;
			this.pagesAndOffstes = pagesAndOffsets;
		}
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

    public void create(String[] names, Order[] orders) throws KeySizeException {
    	keyColumnsNames = Arrays.asList(names);
    	this.orders = orders;
    }

    public abstract void add(TableElement[] data, int pageNumber, int offset);
    public abstract List<Pair<String, Constraint>> filterNonIndexedConstraints(
    		IndexFindResults ifr);
    
    public abstract boolean canResolve(String[] names);
    public abstract boolean canResolve(List<Pair<String, Constraint>> constrs);
    public abstract IndexFindResults find(List<Pair<String, Constraint>> constraints);
    public abstract IndexFindResults find(String columnNames[], Constraint[] constraints);
}