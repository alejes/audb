package audb.table;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import audb.command.Constraint;
import audb.index.BTreeIndex;
import audb.index.Index;
import audb.index.Index.Order;
import audb.index.IndexValueInstance;
import audb.page.Page;
import audb.page.PageManager;
import audb.page.PageStructure;
import audb.result.ConditionalTableIterator;
import audb.result.FullScanIterator;
import audb.result.IndexedConditionalTableIterator;
import audb.type.MutableLong;
import audb.type.Type;
import audb.util.Pair;


public class Table implements Iterable<HashMap<String, TableElement>> {

	public static final int TYPES_INFO       = 0;

	public static final long INFO_PAGE       = PageStructure.INFO_PAGE;

	public static final int CURRENT_PAGE     = PageManager.PAGE_SIZE - 4 * Long.BYTES;
	public static final int FIRST_FULL       = PageManager.PAGE_SIZE - 5 * Long.BYTES;
	public static final int LAST_FULL        = PageManager.PAGE_SIZE - 6 * Long.BYTES;
	public static final long FULL_END        = -2;    

	public static final int NEXT_PAGE        = PageStructure.NEXT_PAGE;
	public static final int PREV_PAGE        = PageStructure.PREV_PAGE;
	public static final int COUNT_OF_RECORDS = PageManager.PAGE_SIZE - 3 * Long.BYTES;
	public static final int INFO_SIZE        = 3 * Long.BYTES;

	public static final int INDEX_COUNT      = PageManager.PAGE_SIZE - 4 * Long.BYTES;


	private PageStructure pageStructure;
	private List<Index> indexList;

	private String[] names;
	private Type[] types;

	private int recordSize;
	private int maxRecords;

	private long nextEmptyPage;
	private long nextFullPage;
	private long lastEmptyPage;
	private long lastFullPage;
	private long countOfPages;


	public Table(PageStructure ps) {
		pageStructure = ps;
		indexList = new LinkedList<Index>();
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
			for(int j = 0; j < name.length; j++)
				name[j] = page.data[ptr + j];
			ptr += name.length;
			this.names[i] = new String(name, StandardCharsets.UTF_8);
			this.types[i] = Type.makeType(type);
		}

		long indexCount = page.readLong(INDEX_COUNT);
		for (long i = 0; i < indexCount; ++i) {
			Index index = new BTreeIndex(this, page.readLong(INDEX_COUNT - (int)i), pageStructure);
			indexList.add(index);
		}

		page.write();
		calculateRecordInfo();
	}

	public void create(Type[] types, String[] names) {

		Page page = pageStructure.getPage(INFO_PAGE);
		int ptr = TYPES_INFO;
		page.data[ptr++] = (byte) types.length;

		for(int i = 0; i < types.length; i++) {
			page.data[ptr++] = types[i].getId();
			byte[] name = names[i].getBytes();
			page.data[ptr++] = (byte)name.length;
			for(int j = 0; j < name.length; j++)
				page.data[ptr + j] = name[j];
			ptr += name.length;
		}

		page.writeLong(CURRENT_PAGE, 0l);
		page.writeLong(FIRST_FULL, FULL_END);
		page.writeLong(LAST_FULL, FULL_END);
		page.writeLong(INDEX_COUNT, 0l);
		page.write();

		getEmptyPage();

		this.names = new String[names.length];
		this.types = new Type[types.length];
		System.arraycopy(names, 0, this.names, 0, names.length);
		System.arraycopy(types, 0, this.types, 0, types.length);


		calculateRecordInfo();
	}

	private void getEmptyPage() {
		long emptyPage = pageStructure.getEmptyPage();
		Page page = pageStructure.getPage(INFO_PAGE);
		page.writeLong(CURRENT_PAGE, emptyPage);
		page.write();

		page = pageStructure.getPage(emptyPage);
		page.writeLong(COUNT_OF_RECORDS, 0l);
		page.write();
	}

	private void addFullPage(long pageNum) {
		Page page = pageStructure.getPage(INFO_PAGE);
		MutableLong firstPage = new MutableLong(page.readLong(FIRST_FULL));
		MutableLong lastPage = new MutableLong(page.readLong(LAST_FULL));
		pageStructure.pushBack(pageNum, firstPage, lastPage, FULL_END);
		page.writeLong(FIRST_FULL, firstPage.get());
		page.writeLong(LAST_FULL, lastPage.get());
		page.write();
	}

	public String[] getNames() {
		return names;
	}

	public Type[] getTypes() {
		return types;
	}

	private void write(long pageNum, int offset, Object[] objects) throws Exception {
		Page page = pageStructure.getPage(pageNum);
		int ptr = offset * recordSize;
		for(int i = 0; i < types.length; i++) {
			byte[] data = types[i].toBytes(objects[i]);
			System.arraycopy(data, 0, page.data, ptr, data.length);
			ptr += data.length;
		}
		ptr += 1;
		page.write();
	}

	public HashMap<String, TableElement> read(int pageNumber, int offset) {
		Page p = pageStructure.getPage(pageNumber);
		return read(p, offset);
	}

	public HashMap<String, TableElement> read(Page page, int offset) {
		int ptr = offset * recordSize;
		HashMap<String, TableElement> objects = new HashMap<String, TableElement>();
		for(int i = 0; i < types.length; i++) {
			byte[] data = new byte[types[i].getSize()];
			System.arraycopy(page.data, ptr, data, 0, types[i].getSize());
			objects.put(names[i], types[i].fromBytes(data));
			ptr += types[i].getSize();
		}
		return objects;
	}

	private void calculateRecordInfo() {
		recordSize = 0;
		for(int i = 0; i < types.length; i++) {
			recordSize += types[i].getSize();
		}
		recordSize += 1;
		maxRecords = (PageManager.PAGE_SIZE - INFO_SIZE) / recordSize;
	}

	public void addRecord(Object[] data) throws Exception {
		for(int i = 0; i < types.length; i++) {
			if(!types[i].isValid(data[i]))
				throw new Exception("Not valid data in addRecord.");
		}

		Page page = pageStructure.getPage(INFO_PAGE);
		long currentPage = page.readLong(CURRENT_PAGE);

		page = pageStructure.getPage(currentPage);
		long countOfRecords = page.readLong(COUNT_OF_RECORDS);
		write(currentPage, (int)countOfRecords, data);

		countOfRecords += 1;
		page.writeLong(COUNT_OF_RECORDS, countOfRecords);
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
			index.add(newElements, (int)currentPage, (int)countOfRecords);
		}
	}

	public void addBTreeIndex(String[] indexNames, Order[] orders) {
		long emptyPage = pageStructure.getEmptyPage();
		Index index = new BTreeIndex(this, emptyPage, pageStructure);
		index.create(indexNames, orders);

		Page page = pageStructure.getPage(INFO_PAGE);
		long indexCount = page.readLong(INDEX_COUNT);
		page.writeLong(INDEX_COUNT, indexCount + 1);
		page.writeLong(INDEX_COUNT - (int)indexCount, emptyPage);
		indexList.add(index);
	}

	public Iterator<HashMap<String, TableElement>> iterator() {
		return new FullScanIterator(this);
	}

	public Iterator<HashMap<String, TableElement>> select(List<Pair<String, Constraint>> constrs) {
		Index goodIndex = null;
		for (Index idx : indexList) {
			if (idx.canResolve(names)) {
				goodIndex = idx;
				break;
			}
		}

		if (null != goodIndex) {
			List<Pair<String, Constraint>> nonIndexedConstraints = 
					goodIndex.filterNonIndexedConstraints(constrs);
			List<IndexValueInstance> pagesAndOffsets = goodIndex.find(constrs);
			return new IndexedConditionalTableIterator(this, pagesAndOffsets, nonIndexedConstraints);
		} else {
			return new ConditionalTableIterator(this, constrs);
		}
	}

}
