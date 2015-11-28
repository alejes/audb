package audb.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import audb.command.Constraint;
import audb.page.PageManager;
import audb.page.PageStructure;
import audb.result.FullScanResult;
import audb.table.Table;
import audb.table.TableElement;

class Pair<F, S> {
	Pair(F f, S s) {
		first = f;
		second = s;
	}
	
	public static <F,S> Pair <F,S> newPair(F f, S s) {
		return new Pair<F,S>(f, s);
	}
	
    public F first;
    public S second;
}

public class BTreeIndex extends Index {
	private static final long MAX_RAM_SIZE_MB = 128;
	private Table table;
	
    public BTreeIndex(Table table, long mainPage, PageStructure pageStructure) {
        super(table, mainPage, pageStructure);
        this.table = table;
    }

    public void init() throws Exception {
    	
    }

    private void buildInternal(final String[] names, final Order[] orders) {    	
    	
    	ArrayList<Pair<HashMap<String, TableElement>, Long>> data = 
    			new ArrayList<Pair<HashMap<String,TableElement>, Long>>();
    	FullScanResult iter = (FullScanResult)table.iterator();
    	
    	while (iter.hasNext()) {
    		HashMap<String, TableElement> row = iter.next();
    		long pageNum = iter.getCurrentPageNumber();
    		data.add(new Pair<HashMap<String, TableElement>, Long>(row, pageNum));
    	}
    	
    	if (data.size() == 0)
    		return;
    	
    	Collections.sort(data, new Comparator<Pair<HashMap<String, TableElement>, Long>>() {

			public int compare(Pair<HashMap<String, TableElement>, Long> o1, 
					Pair<HashMap<String, TableElement>, Long> o2) {
				
				for (int i = 0; i < names.length; i++) {
					String name = names[i];
					
					int result = o1.first.get(name).compareTo(o2.first.get(name));
					if (result == 0)
						continue;
					
					if (orders[i] == Order.ASC) {
						return result;
					}
					
					return -result;
				}
				return 0;
			}
		});
    	
    	int maxKeySize = 0;
    	iter = (FullScanResult)table.iterator();
    	HashMap<String, TableElement> row = iter.next();
    	for (String s : names) {
    		TableElement el = row.get(s);
    		maxKeySize += el.getSizeInBytes();
    	}
    	
    	int keysPerPage = PageManager.PAGE_SIZE / maxKeySize;
    	
    }
    
    public void create(String[] names, Order[] orders) throws Exception {
    	// first of all, sort values
    	long size = pageStructure.getCountOfPages() * PageManager.PAGE_SIZE;
    	if (size <= MAX_RAM_SIZE_MB) {
    		buildInternal(names, orders);
    	} else {
    		
    	}
    }

    public void add(TableElement[] data, long pageNumber) {
    	
    }

    public boolean canResolve(String[] columnNames) {
        return false;
    }

    public Table find(String columnNames[], Constraint[] constraints) {
        return null;
    }
}