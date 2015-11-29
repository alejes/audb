package audb.index;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import audb.command.Constraint;
import audb.command.Constraint.ConstraintType;
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
			if (null == elements[i]) {
				continue;
			}
			int result = elements[i].compareTo(other.elements[i]);
			if (result == 0)
				continue;
			
			return ((orders[i] == Order.ASC) ?  result : -result);
		}
		return 0;
	}
}

class IndexValueInstance implements Comparable<IndexValueInstance> {
	public final int page;
	public final int offset;
	
	public IndexValueInstance(int page, int offset) {
		this.page = page;
		this.offset = offset;
	}

	// dummy method
	public int compareTo(IndexValueInstance o) {
		return Integer.compare(page, o.page);
	}
	
	
}

public class BTreeIndex extends Index {
	private static final long MAX_RAM_SIZE_MB = 128;
	private Table table;
	private BTree<IndexKeyInstance, IndexValueInstance> btree;
	private String[] keyColumnsNames;
	
	// load from database
    public BTreeIndex(Table table, long mainPage, PageStructure pageStructure) {
        super(table, mainPage, pageStructure);
        this.table = table;
        
        // read index here
        //btree = new BTree<IndexKeyInstance, Long>(fanout);
    }

    public void init() throws Exception {
    	
    }

    private ComparablePair<IndexKeyInstance, IndexValueInstance> buildIndexPair(
    		FullScanResult iter, String[] names) {
		HashMap<String, TableElement> row = iter.next();
		int offset = iter.getCurrentOffset() - 1;
		int pageNum = (int) iter.getCurrentPageNumber();
		TableElement[] elements = new TableElement[names.length];
		for (int i = 0; i < names.length; i++) {
			elements[i] = row.get(names[i]);
		}
		
		return ComparablePair.newPair(new IndexKeyInstance(orders, elements), 
				new IndexValueInstance(pageNum, offset));
    }
    
    private void buildInternal(final String[] names, final Order[] orders) {    	
    	
    	ArrayList<ComparablePair<IndexKeyInstance, IndexValueInstance>> data = 
    			new ArrayList<ComparablePair<IndexKeyInstance, IndexValueInstance>>();
    	FullScanResult iter = (FullScanResult)table.iterator();
    	
    	while (iter.hasNext()) {
    		ComparablePair<IndexKeyInstance, IndexValueInstance> newRecord = 
    				buildIndexPair(iter, names);
    		
    		data.add(newRecord);
    	}
    	
    	if (data.size() == 0)
    		return;
    	
    	Collections.sort(data);
    	
    	for (Pair<IndexKeyInstance, IndexValueInstance> p : data) {
    		btree.insert(p);
    	}
    	
    	keyColumnsNames = names.clone();
    }
    
    
    // in future, will probably use external merge sort
    private void buildExternal(final String[] names, final Order[] orders) { 
    	FullScanResult iter = (FullScanResult)table.iterator();
    	while (iter.hasNext()) {
    		ComparablePair<IndexKeyInstance, IndexValueInstance> newRecord = 
    				buildIndexPair(iter, names);
    		btree.insert(newRecord);
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
    	btree = new BTree<IndexKeyInstance, IndexValueInstance>(keysPerPage);
    	
    	
    	if (size <= MAX_RAM_SIZE_MB) {
    		buildInternal(names, orders);
    	} else {
    		buildExternal(names, orders);
    	}
    }

    @Override
    public void add(TableElement[] data, int pageNumber, int offset) {
    	btree.insert(ComparablePair.newPair(new IndexKeyInstance(orders, data), 
    			new IndexValueInstance((int)pageNumber, offset)));
    }

    public boolean canResolve(String[] columnNames) {
        for (String s : columnNames) {
        	if (s == keyColumnsNames[0]) {
        		return true;
        	}
        }
        
        return false;
    }
    
    boolean constraintsDefineEmptyset(HashMap<String, Constraint> upperBound, 
    		HashMap<String, Constraint> bottomBound, HashMap<String, Constraint> exactBound,
    		HashMap<String, ArrayList<Constraint>> exactNotBound, String columnNames[]) {
    	// check that set is not evidently empty
    	for (String s : columnNames) {
    		if (upperBound.containsKey(s) && bottomBound.containsKey(s)) {
    			if (!upperBound.get(s).elementSatisfies(bottomBound.get(s).reference)) {
    				return true;
    			}
    			if (!bottomBound.get(s).elementSatisfies(upperBound.get(s).reference)) {
    				return true;
    			}
    		}
    		
    		if (upperBound.containsKey(s) && exactBound.containsKey(s)) {
    			if (!upperBound.get(s).elementSatisfies(exactBound.get(s).reference)) {
    				return true;
    			}
    		}
    		
    		if (bottomBound.containsKey(s) && exactBound.containsKey(s)) {
    			if (!bottomBound.get(s).elementSatisfies(exactBound.get(s).reference)) {
    				return true;
    			}
    		}
    		
    		if (exactBound.containsKey(s) && exactNotBound.containsKey(s)) {
    			TableElement exact = exactBound.get(s).reference;
    			for (Constraint c : exactNotBound.get(s)) {
    				if (!c.elementSatisfies(exact)) {
    					return true;
    				}
    			}
    		}
    	}
    	
    	return false;
    }
    
    private void reduceConstraints(HashMap<String, Constraint> upperBound, 
    		HashMap<String, Constraint> bottomBound, HashMap<String, Constraint> exactBound,
    		HashMap<String, ArrayList<Constraint>> exactNotBound, String columnNames[]) {

    	for (String s : columnNames) {
    		if (exactBound.containsKey(s)) {
    			upperBound.put(s, new Constraint(ConstraintType.GREATER_OR_EQUAL, exactBound.get(s).reference));
    			bottomBound.put(s, new Constraint(ConstraintType.GREATER_OR_EQUAL, exactBound.get(s).reference));
    		} else {
    			if (bottomBound.containsKey(s)) {
    				Constraint tmp = bottomBound.get(s);
    				if (tmp.constraintType == ConstraintType.GREATER) {
    					exactNotBound.get(s).add(new Constraint(ConstraintType.NOT_EQUAL, tmp.reference));
    					bottomBound.put(s, new Constraint(ConstraintType.GREATER_OR_EQUAL, tmp.reference));
    				}
    			} 
    			if (upperBound.containsKey(s)) {
    				Constraint tmp = upperBound.get(s);
    				if (tmp.constraintType == ConstraintType.LESS) {
    					exactNotBound.get(s).add(new Constraint(ConstraintType.NOT_EQUAL, tmp.reference));
    					upperBound.put(s, new Constraint(ConstraintType.LESS_OR_EQUAL, tmp.reference));
    				}
    			}
    		}
    	}
    }
    
    private IndexKeyInstance buildKeyInstanceByConstraints(HashMap<String, Constraint> bounds) {
    	TableElement[] elements = new TableElement[keyColumnsNames.length];
    	for (int i = 0; i < keyColumnsNames.length; i++) {
    		if (bounds.containsKey(keyColumnsNames[i])) {
    			elements[i] = bounds.get(keyColumnsNames[i]).reference;
    		} else {
    			elements[i] = null;
    		}
    	}
    	
    	return new IndexKeyInstance(orders, elements);
    }
    
    private List<IndexKeyInstance> buildExcludedKeys(
    		HashMap<String, ArrayList<Constraint>> exactNotBound) {
    	List<IndexKeyInstance> excludedKeys = new LinkedList<IndexKeyInstance>();
    	for (int i = 0; i < keyColumnsNames.length; i++) {
    		String colName = keyColumnsNames[i];
    		if (!exactNotBound.containsKey(colName))
    			continue;
    		List<Constraint> excludingConstraints = exactNotBound.get(keyColumnsNames[i]);
    		for (Constraint c : excludingConstraints) {
    			TableElement[] elements = new TableElement[keyColumnsNames.length];
    			elements[i] = c.reference;
    			excludedKeys.add(new IndexKeyInstance(orders, elements));
    		}
    	}
    	
    	return excludedKeys;
    }
    
    public Table find(String columnNames[], Constraint[] constraints) {
        HashMap<String, Constraint> upperBound = new HashMap<String, Constraint>();
        HashMap<String, Constraint> bottomBound = new HashMap<String, Constraint>();
        HashMap<String, Constraint> exactBound = new HashMap<String, Constraint>();
        HashMap<String, ArrayList<Constraint>> exactNotBound = new HashMap<String, ArrayList<Constraint>>();
        
        Table result = new Table(pageStructure);
        for (String s : columnNames)
        	exactNotBound.put(s,  new ArrayList<Constraint>());
        
    	for (int i = 0; i < columnNames.length; i++) {
    		switch (constraints[i].constraintType) {
    		case EQUAL:
    			if (!exactBound.containsKey(columnNames[i])) {
    				exactBound.put(columnNames[i], constraints[i]);
    			} else {
    				if (!exactBound.get(columnNames[i]).elementSatisfies(constraints[i].reference)) {
    					return result; // no way
    				}
    			}
    			
    			break;
			case GREATER:
			case GREATER_OR_EQUAL:
				if (!bottomBound.containsKey(columnNames[i])) {
    				bottomBound.put(columnNames[i], constraints[i]);
    			} else {
    				if (!constraints[i].elementSatisfies(bottomBound.get(columnNames[i]).reference)) {
    					bottomBound.put(columnNames[i], constraints[i]); // new constraint is stronger
    				}
    			}
				break;
			case LESS:
			case LESS_OR_EQUAL:
				if (!upperBound.containsKey(columnNames[i])) {
					upperBound.put(columnNames[i], constraints[i]);
    			} else {
    				if (!constraints[i].elementSatisfies(upperBound.get(columnNames[i]).reference)) {
    					upperBound.put(columnNames[i], constraints[i]); // new constraint is stronger
    				}
    			}
			case NOT_EQUAL:
				exactNotBound.get(columnNames[i]).add(constraints[i]);
				break;
    		}
        }
    	
    	if (constraintsDefineEmptyset(upperBound, bottomBound, exactBound, exactNotBound, columnNames)) {
    		return result;
    	}
    	
    	// make all constraints are <=, >= and !=
    	reduceConstraints(upperBound, bottomBound, exactBound, exactNotBound, columnNames);
    	
    	IndexKeyInstance bottomKey = buildKeyInstanceByConstraints(bottomBound);
    	IndexKeyInstance upperKey = buildKeyInstanceByConstraints(upperBound);
    	List<IndexKeyInstance> excludedKeys = buildExcludedKeys(exactNotBound);
    	
    	List<IndexValueInstance> pagesAndOffsets = btree.findAll(bottomKey, upperKey, excludedKeys);
    	
    	// build temporary table
    	return null;
    }
}