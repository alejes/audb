package audb.result;

import java.util.Iterator;

import audb.page.Page;
import audb.page.PageStructure;
import audb.table.Table;
import audb.table.PageFullScan;
import audb.type.Type;


public class FullScanResult implements Iterator<Object[]> {

    private PageStructure pageStructure;
	private Type[] types;
	private String[] names;
	private int recordSize;

    private PageFullScan pfs;


	private Object[] next;
    private Page page;
    int offset;
    long countOfRecords;

	public FullScanResult(Table table) {
        pageStructure = table.getPageStructure();
		types = table.getTypes();
		names = table.getNames();
		recordSize = table.getRecordSize();

        pfs = new PageFullScan(table);
		offset = 0;
        next = null;
        countOfRecords = 0;

        if(pfs.hasNext()) {
            page = pfs.getNext();
            countOfRecords = page.readLong(Table.COUNT_OF_RECORDS);
        }

        if(countOfRecords > 0)
            next = read(page, offset);

	}

    public Object[] next() {

        Object[] tmp = next;
        if(offset >= countOfRecords) {
            offset = 0;
            if(pfs.hasNext()) {
                page = pfs.getNext();
                countOfRecords = page.readLong(Table.COUNT_OF_RECORDS);
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
        pfs.close();
    }

}
