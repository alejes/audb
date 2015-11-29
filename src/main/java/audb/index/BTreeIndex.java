package audb.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.print.attribute.standard.MediaSize.Other;

import audb.command.Constraint;
import audb.index.Index.Order;
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

class ComparablePair<F extends Comparable<F>, S extends Comparable<S>> 
	extends Pair<F, S> implements Comparable<ComparablePair<F, S>> {
	ComparablePair(F f, S s) {
		super(f, s);
	}

	public int compareTo(ComparablePair<F, S> other) {
		int result = first.compareTo(other.first);
		if (0 != result) {
			return result;
		}
		
		return second.compareTo(other.second);
	}
	
	public static <F extends Comparable<F>, S extends Comparable<S>> 
	ComparablePair <F,S> newPair(F f, S s) {
		return new ComparablePair<F,S>(f, s);
	}
}

class IndexKeyInstance implements Comparable<IndexKeyInstance> {
	final Order[] orders;
	final TableElement[] elements;
	
	IndexKeyInstance(Order[] orders, TableElement[] elements) {
		this.orders = orders;
		this.elements = elements;
	}
	
	public int compareTo(IndexKeyInstance other) {
		for (int i = 0; i < elements.length; i++) {			
			int result = elements[i].compareTo(other.elements[i]);
			if (result == 0)
				continue;
			
			return ((orders[i] == Order.ASC) ?  result : -result);
		}
		return 0;
	}
	
}

public class BTreeIndex extends Index {
	private static final long MAX_RAM_SIZE_MB = 128;
	private Table table;
	private BTree<IndexKeyInstance, Integer> btree;
	
    public BTreeIndex(Table table, long mainPage, PageStructure pageStructure) {
        super(table, mainPage, pageStructure);
        this.table = table;
        
        // read index here
        //btree = new BTree<IndexKeyInstance, Long>(fanout);
    }

    public void init() throws Exception {
    	
    }

    private void buildInternal(final String[] names, final Order[] orders) {    	
    	
    	ArrayList<ComparablePair<IndexKeyInstance, Integer>> data = 
    			new ArrayList<ComparablePair<IndexKeyInstance, Integer>>();
    	FullScanResult iter = (FullScanResult)table.iterator();
    	
    	while (iter.hasNext()) {
    		HashMap<String, TableElement> row = iter.next();
    		int pageNum = (int) iter.getCurrentPageNumber();
    		TableElement[] elements = new TableElement[names.length];
    		for (int i = 0; i < names.length; i++) {
    			elements[i] = row.get(names[i]);
    		}
    		
    		data.add(ComparablePair.newPair(new IndexKeyInstance(orders, elements), pageNum));
    	}
    	
    	if (data.size() == 0)
    		return;
    	
    	Collections.sort(data);
    	
    	for (Pair<IndexKeyInstance, Integer> p : data) {
    		btree.insert(p);
    	}
    }
    
    
    // in future, will probably use external merge sort
    private void buildExternal(final String[] names, final Order[] orders) { 
    	FullScanResult iter = (FullScanResult)table.iterator();
    	while (iter.hasNext()) {
    		HashMap<String, TableElement> row = iter.next();
    		int pageNum = (int) iter.getCurrentPageNumber();
    		TableElement[] elements = new TableElement[names.length];
    		for (int i = 0; i < names.length; i++) {
    			elements[i] = row.get(names[i]);
    		}
    		
    		btree.insert(ComparablePair.newPair(new IndexKeyInstance(orders, elements), pageNum));
    	}
    }
    
    public void create(String[] names, Order[] orders) throws Exception {
    	long size = (pageStructure.getCountOfPages() * PageManager.PAGE_SIZE + 512) / 1024;
    	
    	int maxKeySize = 0;
    	FullScanResult iter = (FullScanResult)table.iterator();
    	HashMap<String, TableElement> row = iter.next();
    	for (String s : names) {
    		TableElement el = row.get(s);
    		maxKeySize += el.getSizeInBytes();
    	}
    	
    	int keysPerPage = PageManager.PAGE_SIZE / (maxKeySize + Integer.BYTES);
    	btree = new BTree<IndexKeyInstance, Integer>(keysPerPage);
    	
    	
    	if (size <= MAX_RAM_SIZE_MB) {
    		buildInternal(names, orders);
    	} else {
    		buildExternal(names, orders);
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