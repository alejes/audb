package audb.result;

import audb.page.Page;
import audb.page.PageCache;
import audb.page.PageManager;
import audb.table.Table;
import audb.table.PageFullScan;
import audb.type.Type;


public class FullScanResult implements Result {

	public static final int COUNT_OF_RECORDS = Table.COUNT_OF_RECORDS;

	private PageCache pageCache;
    private PageManager pageManager;
	private Type[] types;
	private String[] names;
	private int recordSize;

    private PageFullScan pfs;


	private Object[] next;
    private Page page;
    int offset;
    long countOfRecords;

	public FullScanResult(Table table) {
		pageCache = table.getPageCache();
        pageManager = table.getPageManager();
		types = table.getTypes();
		names = table.getNames();
		recordSize = table.getRecordSize();

        pfs = new PageFullScan(table);
		offset = 0;
        next = null;
        countOfRecords = 0;

        if(pfs.hasNext()) {
            page = pfs.getNext();
            countOfRecords = page.readLong(COUNT_OF_RECORDS);
        }

        if(countOfRecords > 0)
            next = read(page, offset);

	}

    public Object[] getNext() {

        Object[] tmp = next;
        if(offset == countOfRecords) {
            offset = 0;
            if(pfs.hasNext()) {
                page = pfs.getNext();
                countOfRecords = page.readLong(COUNT_OF_RECORDS);
            } else {
                page = null;
                countOfRecords = 0;
            }
        }

        if(countOfRecords > offset) {
            next = read(page, offset);
            offset += 1;
        } else 
            next = null;

    	return tmp;
    }

    public boolean hasNext() {
    	return next != null;
    }

    public Type[] getTypes() {
    	return types;
    }

    private Object[] read(Page page, int offset) {
		int ptr = offset * recordSize;
		Object[] objects = new Object[types.length];
		for(int i = 0; i < types.length; i++) {
			byte[] data = new byte[types[i].getSize()];
			System.arraycopy(page.data, ptr, data, 0, types[i].getSize());
			objects[i] = types[i].fromBytes(data);
			ptr += types[i].getSize();
		}
        return objects;
    }

    public void close() {
    }
}
