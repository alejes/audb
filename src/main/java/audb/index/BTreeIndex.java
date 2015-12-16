package audb.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import audb.command.Constraint;
import audb.command.Constraint.ConstraintType;
import audb.index.btree.BTree;
import audb.page.Page;
import audb.page.PageManager;
import audb.page.PageReader;
import audb.page.PageStructure;
import audb.page.PageWriter;
import audb.result.FullScanIterator;
import audb.table.Table;
import audb.table.TableElement;
import audb.type.Type;
import audb.util.ComparablePair;
import audb.util.Pair;


public class BTreeIndex extends Index {
	private static final long MAX_RAM_SIZE_MB = 128;
	private Table table;
	private BTree btree;
	private List<Integer> keyColumnIndexes;
	
	// load from database
	public BTreeIndex(Table table, int mainPage, PageStructure pageStructure) {
		super(table, mainPage, pageStructure);
		this.table = table;
		keyColumnIndexes = new LinkedList<Integer>();
		// read index here
		//btree = new BTree<IndexKeyInstance, Long>(fanout);
	}

	public void init() throws Exception {
		Page p = pageStructure.getPage(mainPage);
		PageReader pr = new PageReader(p);
		int keyColumnsNumber = pr.readInteger();
		
		for (int i = 0; i < keyColumnsNumber; i++) {
			keyColumnIndexes.add(pr.readInteger());
		}
		int fanout = pr.readInteger();
		int rootPage = pr.readInteger();
		
		List<Type> keyTypes = new LinkedList<Type>();
		for (int idx : keyColumnIndexes)
			keyTypes.add(table.getTypes()[idx]);
		btree = new BTree(fanout, pageStructure, keyTypes, orders, rootPage);
	}

	private ComparablePair<IndexKeyInstance, IndexValueInstance> buildIndexPair(
			FullScanIterator iter, String[] names) {
		HashMap<String, TableElement> row = iter.next();
		int offset = iter.getCurrentOffset();
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
		FullScanIterator iter = (FullScanIterator)table.iterator();

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
	}


	// in future, will probably use external merge sort
	private void buildExternal(final String[] names, final Order[] orders) { 
		FullScanIterator iter = (FullScanIterator)table.iterator();
		while (iter.hasNext()) {
			ComparablePair<IndexKeyInstance, IndexValueInstance> newRecord = 
					buildIndexPair(iter, names);
			btree.insert(newRecord);
		}
	}

	private void fillMainPage() {
		Page p = pageStructure.getPage(mainPage);
		PageWriter pw = new PageWriter(p);
		
		int keyColumnsNumber = keyColumnIndexes.size();
		pw.writeInteger(keyColumnsNumber);
		
		for (int i : keyColumnIndexes) {
			pw.writeInteger(i);
		}
		
		pw.writeInteger(btree.getFanout());
		pw.writeInteger(btree.getRootPage());
	}
	
	public void create(String[] names, Order[] orders) {
		super.create(names, orders);
		long size = (pageStructure.getCountOfPages() * PageManager.PAGE_SIZE + 512) / 1024;
		int maxKeySize = 0;

		String[] tableNames = table.getNames();
		Type[] tableTypes = table.getTypes();
		List<Type> keyElementsTypes = new ArrayList<Type>(names.length);
		
		for (String s : names) {
			for (int i = 0; i < tableNames.length; i++) {
				if (s == tableNames[i]) {
					keyColumnIndexes.add(i);
					keyElementsTypes.add(tableTypes[i]);
					maxKeySize += tableTypes[i].getSize();
				}
			}
		}

		int innerBound = (PageManager.PAGE_SIZE - Byte.BYTES - Integer.BYTES +
				maxKeySize) / (maxKeySize + Integer.BYTES);
		int leafBound = (PageManager.PAGE_SIZE - Byte.BYTES - 2*Integer.BYTES +
				maxKeySize + IndexValueInstance.getSizeInBytes()) / (maxKeySize + 
						IndexValueInstance.getSizeInBytes());
		int fanout = Math.min(innerBound, leafBound);
		btree = new BTree(fanout, pageStructure, keyElementsTypes, orders);

		if (size <= MAX_RAM_SIZE_MB) {
			buildInternal(names, orders);
		} else {
			buildExternal(names, orders);
		}
		
		fillMainPage();
	}

	@Override
	public void add(TableElement[] data, int pageNumber, int offset) {
		TableElement[] keys = new TableElement[keyColumnIndexes.size()];
		int index = 0;
		for (Integer i : keyColumnIndexes) {
			keys[index++] = data[i];
		}
			
		boolean rootChanged = btree.insert(ComparablePair.newPair(new IndexKeyInstance(orders, keys), 
				new IndexValueInstance((int)pageNumber, offset)));
		if (rootChanged) {
			fillMainPage();
		}
	}

	public boolean canResolve(String[] columnNames) {
		for (String s : columnNames) {
			if (s == keyColumnsNames.get(0)) {
				return true;
			}
		}

		return false;
	}

	boolean constraintsDefineEmptyset(HashMap<String, Constraint> upperBound, 
			HashMap<String, Constraint> bottomBound, HashMap<String, Constraint> exactBound,
			HashMap<String, List<Constraint>> exactNotBound, String columnNames[]) {
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
			HashMap<String, List<Constraint>> exactNotBound, String columnNames[]) {

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
		TableElement[] elements = new TableElement[keyColumnsNames.size()];
		for (int i = 0; i < keyColumnsNames.size(); i++) {
			if (bounds.containsKey(keyColumnsNames.get(i))) {
				elements[i] = bounds.get(keyColumnsNames.get(i)).reference;
			} else {
				elements[i] = null;
			}
		}

		return new IndexKeyInstance(orders, elements);
	}

	private List<IndexKeyInstance> buildExcludedKeys(
			HashMap<String, List<Constraint>> exactNotBound) {
		List<IndexKeyInstance> excludedKeys = new ArrayList<IndexKeyInstance>();
		for (int i = 0; i < keyColumnsNames.size(); i++) {
			String colName = keyColumnsNames.get(i);
			if (!exactNotBound.containsKey(colName))
				continue;
			List<Constraint> excludingConstraints = exactNotBound.get(keyColumnsNames.get(i));
			for (Constraint c : excludingConstraints) {
				TableElement[] elements = new TableElement[keyColumnsNames.size()];
				elements[i] = c.reference;
				excludedKeys.add(new IndexKeyInstance(orders, elements));
			}
		}

		Collections.sort(excludedKeys);
		// TODO add sort here
		//excludedKeys.sort(c);
		return excludedKeys;
	}
	
	public IndexFindResults find(String columnNames[], Constraint[] constraints) {
		HashMap<String, Constraint> upperBound = new HashMap<String, Constraint>();
		HashMap<String, Constraint> bottomBound = new HashMap<String, Constraint>();
		HashMap<String, Constraint> exactBound = new HashMap<String, Constraint>();
		HashMap<String, List<Constraint>> exactNotBound = new HashMap<String, List<Constraint>>();

		IndexFindResults result = new IndexFindResults(bottomBound, upperBound, exactBound, 
				exactNotBound, new ArrayList<IndexValueInstance>());
		
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
				break;
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

		List<HashMap<String, Constraint>> bounds = new ArrayList<HashMap<String,Constraint>>(4);
		bounds.add(bottomBound);
		bounds.add(upperBound);
		bounds.add(exactBound);
		// build temporary table
		
		result.pagesAndOffstes = pagesAndOffsets;
		return result;
	}

	@Override
	public boolean canResolve(List<Pair<String, Constraint>> constrs) {
		for (Pair<String, Constraint> con : constrs) {
			if (con.first == keyColumnsNames.get(0)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public List<Pair<String, Constraint>> filterNonIndexedConstraints(IndexFindResults ifr) {
		Iterator<String> keyColumnIter = keyColumnsNames.iterator();
		
		HashMap<Pair<String, Constraint>, Boolean> isIndexedConstraint =
				new HashMap<Pair<String,Constraint>, Boolean>();
		for (String s : table.getNames()) {
			if (ifr.bottomBounds.containsKey(s))
				isIndexedConstraint.put(Pair.newPair(s, ifr.bottomBounds.get(s)), false);
			if (ifr.upperBounds.containsKey(s))
				isIndexedConstraint.put(Pair.newPair(s, ifr.upperBounds.get(s)), false);
			if (ifr.exactBounds.containsKey(s))
				isIndexedConstraint.put(Pair.newPair(s, ifr.exactBounds.get(s)), false);
			if (ifr.exactNotBounds.containsKey(s)) {
				for (Constraint con : ifr.exactNotBounds.get(s)) {
					boolean putValue = false;
					if (keyColumnsNames.contains(s)) // TODO probably, not equality should not be solved ny index
						putValue = (0 == keyColumnsNames.indexOf(s));
					isIndexedConstraint.put(Pair.newPair(s, con), putValue);
				}
			}
		}
		
		boolean bottomBoundsCanHaveIndexedConstraints = true;
		boolean upperBoundsCanHaveIndexedConstraints = true;
		boolean exactBoundsCanHaveIndexedConstraints = true;
		boolean exactNotBoundsCanHaveIndexedConstraints = true;
		
		for (String keyColumn : keyColumnsNames) {
			if (ifr.bottomBounds.containsKey(keyColumn))
				isIndexedConstraint.put(Pair.newPair(keyColumn,
						ifr.bottomBounds.get(keyColumn)), bottomBoundsCanHaveIndexedConstraints);
			else
				bottomBoundsCanHaveIndexedConstraints = false;
			
			if (ifr.upperBounds.containsKey(keyColumn))
				isIndexedConstraint.put(Pair.newPair(keyColumn,
						ifr.upperBounds.get(keyColumn)), upperBoundsCanHaveIndexedConstraints);
			else
				upperBoundsCanHaveIndexedConstraints = false;
			
			if (ifr.exactBounds.containsKey(keyColumn))
				isIndexedConstraint.put(Pair.newPair(keyColumn,
						ifr.exactBounds.get(keyColumn)), exactBoundsCanHaveIndexedConstraints);
			else
				exactBoundsCanHaveIndexedConstraints = false;
			
			if (ifr.exactNotBounds.containsKey(keyColumn) && exactNotBoundsCanHaveIndexedConstraints) {
				for (Constraint con : ifr.exactNotBounds.get(keyColumn)) {
					isIndexedConstraint.put(Pair.newPair(keyColumn, con), true);
				}
			} else {
				exactNotBoundsCanHaveIndexedConstraints = false;
			}
			
		}
		
		List<Pair<String, Constraint>> result = new LinkedList<Pair<String,Constraint>>();
		for (Pair<String, Constraint> p : isIndexedConstraint.keySet()) {
			if (!isIndexedConstraint.get(p)) {
				result.add(p);
			}
		}
		return result;
	}

	@Override
	public IndexFindResults find(List<Pair<String, Constraint>> constraints) {
		String[] names = new String[constraints.size()];
		Constraint[] cons = new Constraint[constraints.size()];
		
		int idx = 0;
		for (Pair<String, Constraint> p : constraints) {
			names[idx] = p.first;
			cons[idx] = p.second;
			idx++;
		}
		
		return find(names, cons);
	}
}