package audb.table;

import audb.command.Constraint;
import audb.index.BTreeIndex;
import audb.index.Index;
import audb.index.Index.IndexFindResults;
import audb.index.Index.KeySizeException;
import audb.index.Index.Order;
import audb.index.IndexValueInstance;
import audb.page.Page;
import audb.page.PageManager;
import audb.page.PageStructure;
import audb.result.ConditionalTableIterator;
import audb.result.FullScanIterator;
import audb.result.IndexedConditionalTableIterator;
import audb.type.MutableInt;
import audb.type.Type;
import audb.util.Pair;
import audb.util.Third;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class Table implements Iterable<HashMap<String, TableElement>> {

	public static final int TYPES_INFO       = 0;

	public static final int INFO_PAGE       = PageStructure.INFO_PAGE;

	public static final int CURRENT_PAGE     = PageManager.PAGE_SIZE - 4 * Integer.BYTES;
	public static final int FIRST_FULL       = PageManager.PAGE_SIZE - 5 * Integer.BYTES;
	public static final int LAST_FULL        = PageManager.PAGE_SIZE - 6 * Integer.BYTES;
	public static final int FULL_END        = -2;    

	public static final int NEXT_PAGE        = PageStructure.NEXT_PAGE;
	public static final int PREV_PAGE        = PageStructure.PREV_PAGE;
	public static final int COUNT_OF_RECORDS = PageManager.PAGE_SIZE - 3 * Integer.BYTES;
	public static final int INFO_SIZE        = 3 * Integer.BYTES;

	public static final int INDEX_COUNT      = PageManager.PAGE_SIZE - 7 * Integer.BYTES;


	private PageStructure pageStructure;
	private List<Index> indexList;
	private String tableName;

	private String[] names;
	private Type[] types;

	private int recordSize;
	private int maxRecords;

	private int nextEmptyPage;
	private int nextFullPage;
	private int lastEmptyPage;
	private int lastFullPage;
	private int countOfPages;


	public Table(PageStructure ps, String name) {
		pageStructure = ps;
		indexList = new LinkedList<>();
		tableName = name;
	}

	public PageStructure getPageStructure() {
		return pageStructure;
	}

	public int getRecordSize() {
		return recordSize;
	}

	public void init() throws Exception {

		Page page = pageStructure.getPage(INFO_PAGE);
		int ptr = TYPES_INFO;

		int length = page.data[ptr++];
		this.names = new String[length];
		this.types = new Type[length];

		for(int i = 0; i < names.length; i++) {
			byte type = page.data[ptr++];
			int nameLength = page.data[ptr++];
			byte[] name = new byte[nameLength];
			System.arraycopy(page.data, ptr + 0, name, 0, name.length);
			ptr += name.length;
			this.names[i] = tableName + "." + (new String(name, StandardCharsets.UTF_8));
			this.types[i] = Type.makeType(type);
		}

		int indexCount = page.readInteger(INDEX_COUNT);
		for (int i = 1; i <= indexCount; ++i) {
			Index index = new BTreeIndex(this, page.readInteger(INDEX_COUNT - i), pageStructure);
			// index.init();
			indexList.add(index);
			index.init();
		}

		page.write();
		calculateRecordInfo();
	}

	public void create(Type[] types, String[] names) {

		pageStructure.clear();

		Page page = pageStructure.getPage(INFO_PAGE);
		int ptr = TYPES_INFO;
		page.data[ptr++] = (byte) types.length;

		for(int i = 0; i < types.length; i++) {
			page.data[ptr++] = types[i].getId();
			byte[] name = names[i].getBytes();
			page.data[ptr++] = (byte)name.length;
			System.arraycopy(name, 0, page.data, ptr + 0, name.length);
			ptr += name.length;
		}

		page.writeInteger(CURRENT_PAGE, 0);
		page.writeInteger(FIRST_FULL, FULL_END);
		page.writeInteger(LAST_FULL, FULL_END);
		page.writeInteger(INDEX_COUNT, 0);
		page.write();

		getEmptyPage();

		this.names = new String[names.length];
		this.types = new Type[types.length];
		for (int i = 0; i < this.names.length; ++i) {
			this.names[i] = tableName + "." + names[i];
		}
		// System.arraycopy(names, 0, this.names, 0, names.length);
		System.arraycopy(types, 0, this.types, 0, types.length);


		calculateRecordInfo();
	}

	private void getEmptyPage() {
		int emptyPage = pageStructure.getEmptyPage();
		Page page = pageStructure.getPage(INFO_PAGE);
		page.writeInteger(CURRENT_PAGE, emptyPage);
		page.write();

		page = pageStructure.getPage(emptyPage);
		page.writeInteger(COUNT_OF_RECORDS, 0);
		page.write();
	}

	private void addFullPage(int pageNum) {
		Page page = pageStructure.getPage(INFO_PAGE);
		MutableInt firstPage = new MutableInt(page.readInteger(FIRST_FULL));
		MutableInt lastPage = new MutableInt(page.readInteger(LAST_FULL));
		pageStructure.pushBack(pageNum, firstPage, lastPage, FULL_END);
		page.writeInteger(FIRST_FULL, firstPage.get());
		page.writeInteger(LAST_FULL, lastPage.get());
		page.write();
	}

	public String[] getNames() {
		return names;
	}

	public Type[] getTypes() {
		return types;
	}

	public void write(int pageNum, int offset, Object[] objects) throws Exception {
		Page page = pageStructure.getPage(pageNum);
		int ptr = offset * recordSize;
		for(int i = 0; i < types.length; i++) {
			if (objects[i] == null) {
				ptr += types[i].getSize();
				continue;
			}
			if (!types[i].isValid(objects[i]))
				throw new Exception("not valid data");
			byte[] data = types[i].toBytes(objects[i]);
			System.arraycopy(data, 0, page.data, ptr, data.length);
			ptr += data.length;
		}
		page.data[ptr] = 0;
		page.write();
	}

	public void delete(int pageNum, int offset) throws Exception {
		Page page = pageStructure.getPage(pageNum);
		int ptr = (offset + 1) * recordSize - 1;
		page.data[ptr] = 1;
		page.write();	
	}

	public HashMap<String, TableElement> read(int pageNumber, int offset) {
		Page p = pageStructure.getPage(pageNumber);
		return read(p, offset);
	}

	public HashMap<String, TableElement> read(Page page, int offset) {
		int ptr = offset * recordSize;
		TableLine objects = new TableLine(tableName, page.getPageNumber(), offset);
		for(int i = 0; i < types.length; i++) {
			byte[] data = new byte[types[i].getSize()];
			System.arraycopy(page.data, ptr, data, 0, types[i].getSize());
			objects.put(names[i], types[i].fromBytes(data));
			ptr += types[i].getSize();
		}
		if (page.data[offset * recordSize + recordSize - 1] != 0)
			objects.setDeleted();
		return objects;
	}

	private void calculateRecordInfo() {
		recordSize = 0;
		for (Type t : types) {
			recordSize += t.getSize();
		}
		recordSize += 1; // deleted flag
		maxRecords = (PageManager.PAGE_SIZE - INFO_SIZE) / recordSize;
	}

	public void addRecord(Object[] data) throws Exception {
		for(int i = 0; i < types.length; i++) {
			if(!types[i].isValid(data[i]))
				throw new Exception("Not valid data in addRecord.");
		}

		Page page = pageStructure.getPage(INFO_PAGE);
		int currentPage = page.readInteger(CURRENT_PAGE);

		page = pageStructure.getPage(currentPage);
		int countOfRecords = page.readInteger(COUNT_OF_RECORDS);
		write(currentPage, countOfRecords, data);

		countOfRecords += 1;
		page.writeInteger(COUNT_OF_RECORDS, countOfRecords);
		page.write();

		if (countOfRecords >= maxRecords) {
			addFullPage(currentPage);
			getEmptyPage();
		}

		TableElement[] newElements = new TableElement[data.length];
		for (int i = 0; i < data.length; i++) {
			newElements[i] = types[i].fromObject(data[i]);
		}

		for (Index index : indexList) {
			index.add(newElements, currentPage, countOfRecords - 1);
		}
	}

	public void addBTreeIndex(String[] indexNames, Order[] orders) throws KeySizeException {
		int emptyPage = pageStructure.getEmptyPage();
		Index index = new BTreeIndex(this, emptyPage, pageStructure);
		index.create(indexNames, orders);

		Page page = pageStructure.getPage(INFO_PAGE);
		int indexCount = page.readInteger(INDEX_COUNT) + 1;
		page.writeInteger(INDEX_COUNT, indexCount);
		page.writeInteger(INDEX_COUNT - indexCount, emptyPage);
		indexList.add(index);
	}

	public Iterator<HashMap<String, TableElement>> iterator() {
		return new FullScanIterator(this);
	}

	public Iterator<HashMap<String, TableElement>> select(List<Third<String, Constraint, String>> constrs) {
		Index goodIndex = null;
		for (Index idx : indexList) {
			if (idx.canResolve(constrs)) {
				goodIndex = idx;
				break;
			}
		}

		if (null != goodIndex) {
			IndexFindResults ifr =  goodIndex.find(constrs);
			List<Pair<String, Constraint>> nonIndexedConstraints = 
					goodIndex.filterNonIndexedConstraints(ifr);
			
			List<IndexValueInstance> pagesAndOffsets = ifr.pagesAndOffstes;
			return new IndexedConditionalTableIterator(this, pagesAndOffsets, nonIndexedConstraints);
		} else {
			return new ConditionalTableIterator(this, constrs);
		}
	}


	public String getTableName() {
		return tableName;
	}

	@Override
	public String toString() {
		return "Table " + tableName;
	}

}
