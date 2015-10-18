package audb.table;

import java.nio.charset.StandardCharsets;

import audb.page.Page;
import audb.page.PageCache;
import audb.page.PageManager;
import audb.type.Type;
import audb.type.TypeUtil;


public class Table {

	public PageCache pageCache;

	public static final int TYPES_INFO = 20;

	public static final int PAGE_SIZE = PageManager.PAGE_SIZE;
    public static final int INFO_SIZE = 32;

	public static final long INFO_PAGE = 0;
    public static final int COUNT_OF_RECORDS = PAGE_SIZE - 8;
    public static final int NEXT_PAGE = PAGE_SIZE - 16;
    public static final int PREV_PAGE = PAGE_SIZE - 24;


	private String[] names;
	private Type[] types;
	private int recordSize;
	private int maxRecords;
	private long nextEmptyPage;
	private long nextFullPage;
	private long countOfPages;


	public Table(String tablename) throws Exception {
		pageCache = new PageCache(tablename);
	}

	public int getRecordSize() {
		return recordSize;
	}

	public void init() throws Exception {

		Page page = pageCache.getPage(INFO_PAGE);
		page.pin();
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
			this.types[i] = TypeUtil.makeType(type);
		}

		nextEmptyPage = page.readLong(NEXT_PAGE);
		nextFullPage = page.readLong(PREV_PAGE);
		countOfPages = page.readLong(COUNT_OF_RECORDS);


		page.write();
		page.unpin();

		calculateRecordInfo();
	}

	public void create(Type[] types, String[] names) {

		Page page = pageCache.getPage(INFO_PAGE);
		page.pin();
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

		countOfPages = 0l;
		nextEmptyPage = 0l;
		nextFullPage = 0l;
		page.writeLong(COUNT_OF_RECORDS, countOfPages);
		page.writeLong(NEXT_PAGE, nextEmptyPage);
		page.writeLong(PREV_PAGE, nextFullPage);

		addPageEmpty(getNewPage());

		page.unpin();
		page.write();

		this.names = new String[names.length];
		this.types = new Type[types.length];
		System.arraycopy(names, 0, this.names, 0, names.length);
		System.arraycopy(types, 0, this.types, 0, types.length);
		calculateRecordInfo();
	}

	private void addPageEmpty(long pageNum) {
		refresh();
		Page page = pageCache.getPage(INFO_PAGE);
		long nextPage = page.readLong(NEXT_PAGE);
		page.writeLong(NEXT_PAGE, pageNum);
		page.write();

		page = pageCache.getPage(pageNum);
		page.writeLong(PREV_PAGE, INFO_PAGE);
		page.writeLong(NEXT_PAGE, nextPage);
		page.write();

		if(nextPage != INFO_PAGE) {
			page = pageCache.getPage(nextPage);
			page.writeLong(PREV_PAGE, pageNum);
		}
	}

	private void addPageFull(long pageNum) {
		refresh();
		Page page = pageCache.getPage(INFO_PAGE);
		long nextPage = page.readLong(PREV_PAGE);
		page.writeLong(PREV_PAGE, pageNum);
		page.write();

		page = pageCache.getPage(pageNum);
		page.writeLong(PREV_PAGE, INFO_PAGE);
		page.writeLong(NEXT_PAGE, nextPage);
		page.write();

		if(nextPage != INFO_PAGE) {
			page = pageCache.getPage(nextPage);
			page.writeLong(PREV_PAGE, pageNum);
			page.write();
		}
	}


	private void removePage(long pageNum) {
		refresh();
		Page page = pageCache.getPage(pageNum);
		long prevPage = page.readLong(PREV_PAGE);
		long nextPage = page.readLong(NEXT_PAGE);

		page = pageCache.getPage(prevPage);
		page.writeLong(NEXT_PAGE, nextPage);
		page.write();

		if(nextPage != INFO_PAGE) {
			page = pageCache.getPage(nextPage);
			page.writeLong(PREV_PAGE, prevPage);
			page.write();
		}
	}

	private long getNewPage() {
		refresh();
		countOfPages += 1;
		Page page = pageCache.getPage(countOfPages);
		page.writeLong(COUNT_OF_RECORDS, 0l);
		page.write();

		page = pageCache.getPage(INFO_PAGE);
		page.writeLong(COUNT_OF_RECORDS, countOfPages);
		page.write();

		return countOfPages;
	}

	public String[] getNames() {
		return names;
	}

	public Type[] getTypes() {
		return types;
	}

    private Object[] read(long pageNum, int offset) {
		Page page = pageCache.getPage(pageNum);
		page.pin();
		int ptr = offset * recordSize;
		Object[] objects = new Object[types.length];
		for(int i = 0; i < types.length; i++) {
			byte[] data = new byte[types[i].getSize()];
			System.arraycopy(page.data, ptr, data, 0, types[i].getSize());
			objects[i] = types[i].fromBytes(data);
			ptr += types[i].getSize();
		}
		page.unpin();
        return objects;
    }

    private void write(long pageNum, int offset, Object[] objects) throws Exception {
    	Page page = pageCache.getPage(pageNum);
		int ptr = offset * recordSize;
		for(int i = 0; i < types.length; i++) {
			byte[] data = types[i].toBytes(objects[i]);
			System.arraycopy(data, 0, page.data, ptr, data.length);
			ptr += data.length;
		}
		ptr += 1;
		page.write();
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
    	refresh();
    	Page page = pageCache.getPage(nextEmptyPage);
    	long countOfRecords = page.readLong(COUNT_OF_RECORDS);
    	write(nextEmptyPage, (int)countOfRecords, data);
    	countOfRecords += 1;
    	page.writeLong(COUNT_OF_RECORDS, countOfRecords);
    	page.write();

    	if(countOfRecords >= maxRecords) {
    		long curPage = nextEmptyPage;
    		removePage(curPage);
    		addPageFull(curPage);
    		refresh();
    		if(nextEmptyPage == INFO_PAGE)
    			addPageEmpty(getNewPage());
    	}
    }

    private void refresh() {
		Page page = pageCache.getPage(INFO_PAGE);
		countOfPages = page.readLong(COUNT_OF_RECORDS);
		nextEmptyPage = page.readLong(NEXT_PAGE);
		nextFullPage = page.readLong(PREV_PAGE);
    }

    public void close() {
    	pageCache.close();
    }

}
