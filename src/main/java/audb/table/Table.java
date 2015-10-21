package audb.table;

import java.nio.charset.StandardCharsets;

import audb.page.Page;
import audb.page.PageCache;
import audb.page.PageManager;
import audb.type.Type;


public class Table {

	private PageCache pageCache;
    private PageManager pageManager;

    public static final int TYPES_INFO       = 20;
    
    public static final int PAGE_SIZE        = PageManager.PAGE_SIZE;
    
    public static final long INFO_PAGE       = 0;
    public static final int COUNT_OF_RECORDS = PageManager.PAGE_SIZE - Long.BYTES;
    public static final int NEXT_PAGE        = PageManager.PAGE_SIZE - 2 * Long.BYTES;
    public static final int PREV_PAGE        = PageManager.PAGE_SIZE - 3 * Long.BYTES;
    public static final int INFO_SIZE        = 4 * Long.BYTES;
    
    public static final int FIRST_EMPTY      = PageManager.PAGE_SIZE - 2 * Long.BYTES;
    public static final int LAST_EMPTY       = PageManager.PAGE_SIZE - 3 * Long.BYTES;
    public static final int FIRST_FULL       = PageManager.PAGE_SIZE - 4 * Long.BYTES;
    public static final int LAST_FULL        = PageManager.PAGE_SIZE - 5 * Long.BYTES;
    public static final long EMPTY_END       = -1;
    public static final long FULL_END        = -2;    

	private String[] names;
	private Type[] types;

	private int recordSize;
	private int maxRecords;

	private long nextEmptyPage;
	private long nextFullPage;
    private long lastEmptyPage;
    private long lastFullPage;
	private long countOfPages;

    public Table(PageManager pm, PageCache pc) {
        pageCache = pc;
        pageManager = pm;
    }

    public PageManager getPageManager() {
        return pageManager;
    }

    public PageCache getPageCache() {
        return pageCache;
    }

	public int getRecordSize() {
		return recordSize;
	}

	public void init() throws Exception {

		Page page = pageCache.getPage(pageManager, INFO_PAGE);
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
			this.types[i] = Type.makeType(type);
		}

		page.write();
		page.unpin();

        calculateRecordInfo();
	}

	public void create(Type[] types, String[] names) {

		Page page = pageCache.getPage(pageManager, INFO_PAGE);
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

		page.writeLong(COUNT_OF_RECORDS, 0l);
		page.writeLong(FIRST_EMPTY, EMPTY_END);
		page.writeLong(FIRST_FULL, FULL_END);
        page.writeLong(LAST_EMPTY, EMPTY_END);
        page.writeLong(LAST_FULL, FULL_END);

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
		Page page = pageCache.getPage(pageManager, INFO_PAGE);
		long nextPage = page.readLong(FIRST_EMPTY);
        if(nextPage == EMPTY_END)
            page.writeLong(LAST_EMPTY, pageNum);
		page.writeLong(FIRST_EMPTY, pageNum);
		page.write();

		page = pageCache.getPage(pageManager, pageNum);
		page.writeLong(PREV_PAGE, EMPTY_END);
		page.writeLong(NEXT_PAGE, nextPage);
		page.write();

		if(nextPage != EMPTY_END) {
			page = pageCache.getPage(pageManager, nextPage);
			page.writeLong(PREV_PAGE, pageNum);
            page.write();
		}
	}

	private void addPageFull(long pageNum) {
		refresh();
		Page page = pageCache.getPage(pageManager, INFO_PAGE);
		long nextPage = page.readLong(LAST_FULL);
        if(nextPage == FULL_END)
            page.writeLong(FIRST_FULL, pageNum);
        page.writeLong(LAST_FULL, pageNum);
        page.write();

		page = pageCache.getPage(pageManager, pageNum);
		page.writeLong(PREV_PAGE, nextPage);
		page.writeLong(NEXT_PAGE, FULL_END);
		page.write();

		if(nextPage != FULL_END) {
			page = pageCache.getPage(pageManager, nextPage);
			page.writeLong(NEXT_PAGE, pageNum);
			page.write();
		}
	}


	private void removePage(long pageNum) {
		refresh();
		Page page = pageCache.getPage(pageManager, pageNum);
		long prevPage = page.readLong(PREV_PAGE);
		long nextPage = page.readLong(NEXT_PAGE);

        if(prevPage != EMPTY_END && prevPage != FULL_END) {
            page = pageCache.getPage(pageManager, prevPage);
            page.writeLong(NEXT_PAGE, prevPage);
            page.write();
        } else if(prevPage == EMPTY_END) {
            page = pageCache.getPage(pageManager, INFO_PAGE);
            page.writeLong(FIRST_EMPTY, nextPage);
            page.write();
        } else {
            page = pageCache.getPage(pageManager, INFO_PAGE);
            page.writeLong(FIRST_FULL, nextPage);
            page.write();
        }

        if(nextPage != EMPTY_END && nextPage != FULL_END) {
            page = pageCache.getPage(pageManager, nextPage);
            page.writeLong(PREV_PAGE, prevPage);
            page.write();
        } else if(nextPage == EMPTY_END) {
            page = pageCache.getPage(pageManager, INFO_PAGE);
            page.writeLong(LAST_EMPTY, prevPage);
            page.write();
        } else {
            page = pageCache.getPage(pageManager, INFO_PAGE);
            page.writeLong(LAST_FULL, prevPage);
            page.write();
        }
	}

	private long getNewPage() {
		refresh();
		countOfPages += 1;
		Page page = pageCache.getPage(pageManager, countOfPages);
		page.writeLong(COUNT_OF_RECORDS, 0l);
		page.write();

		page = pageCache.getPage(pageManager, INFO_PAGE);
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
		Page page = pageCache.getPage(pageManager, pageNum);
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
    	Page page = pageCache.getPage(pageManager, pageNum);
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
    	Page page = pageCache.getPage(pageManager, nextEmptyPage);
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
    		if(nextEmptyPage == EMPTY_END)
    			addPageEmpty(getNewPage());
    	}
    }

    private void refresh() {
		Page page = pageCache.getPage(pageManager, INFO_PAGE);
		nextEmptyPage = page.readLong(FIRST_EMPTY);
        nextFullPage = page.readLong(FIRST_FULL);
        lastEmptyPage = page.readLong(LAST_EMPTY);
        lastFullPage = page.readLong(LAST_FULL);
        countOfPages = page.readLong(COUNT_OF_RECORDS);
    }

    public void close() {
    	pageCache.close();
    }

}
