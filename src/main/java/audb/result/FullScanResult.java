package audb.result;

import audb.page.Page;
import audb.page.PageCache;
import audb.table.Table;
import audb.type.Type;


public class FullScanResult implements Result {

	public static final long INFO_PAGE = Table.INFO_PAGE;
	public static final int COUNT_OF_RECORDS = Table.COUNT_OF_RECORDS;
    public static final int NEXT_PAGE = Table.NEXT_PAGE;
    public static final int PREV_PAGE = Table.PREV_PAGE;

	private PageCache pageCache;
	private Type[] types;
	private String[] names;
	private int recordSize;

	private Page page = null;
	private int count;
	private int offset;


	private Object[] next;

	private boolean hasNext;
	private boolean firstTime;
	private boolean isFullReady;
	private long nextPage = 0;
	private long firstEmpty;

	public FullScanResult(Table table) {
		this.pageCache = table.pageCache;
		this.types = table.getTypes();
		this.names = table.getNames();
		this.recordSize = table.getRecordSize();
		page = pageCache.getPage(INFO_PAGE);

		count = 0;
		offset = 0;
		firstTime = true;
		isFullReady = false;
		hasNext = true;

		findNextPage();
		next = findNext();
	}

    public Object[] getNext() {
    	Object[] tmp = next;
    	next = findNext();
    	return tmp;
    }

    private Object[] findNext() {
    	if(offset == count) {
    		page.unpin();
    		page = pageCache.getPage(nextPage);
    		page.pin();
    		offset = 0;
    		count = (int)page.readLong(COUNT_OF_RECORDS);
    		findNextPage();
    	}
    	Object[] res = null;
    	hasNext = false;
    	if(offset < count && page.getPageNumber() != INFO_PAGE) {
    		res = read(page.getPageNumber(), offset);
    		hasNext = true;
    	}
    	offset += 1;
    	return res;
    }

    public boolean hasNext() {
    	if(!hasNext)
    		page.unpin();
    	return hasNext;
    }

    private void findNextPage() {
    	if(!isFullReady)
    		findNextFullPage();
    	if(isFullReady)
    		findNextEmptyPage();
    }

    private void findNextFullPage() {
    	if(firstTime) {
    		nextPage = page.readLong(PREV_PAGE);
    		firstEmpty = page.readLong(NEXT_PAGE);
    		firstTime = false;
    	} else {
    		nextPage = page.readLong(NEXT_PAGE);
    	}

		if(nextPage == INFO_PAGE) {
			isFullReady = true;
			firstTime = true;
		}
    }

    private void findNextEmptyPage() {

    	if(!firstTime)
    		nextPage = page.readLong(NEXT_PAGE);
    	else {
			nextPage = firstEmpty;
    		firstTime = false;
    	}
    	

		if(nextPage == INFO_PAGE) {
			hasNext = false;
		}
    }

    public Type[] getTypes() {
    	return types;
    }

    public Object[] read(long pageNum, int offset) {
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

    public void close() {
    	page.unpin();
    }
}
