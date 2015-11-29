package audb.result;

import java.util.HashMap;

import audb.page.Page;
import audb.page.PageStructure;
import audb.table.PageIterator;
import audb.table.Table;
import audb.table.TableElement;
import audb.type.Type;

public class FullScanIterator implements TableIterator {

    protected PageStructure pageStructure;
    protected Type[] types;
    protected String[] names;
	private int recordSize;

	protected PageIterator pfs;


	protected HashMap<String, TableElement> next;
	protected Page page;
    int offset;
    long countOfRecords;

	public FullScanIterator(Table table) {
        pageStructure = table.getPageStructure();
		types = table.getTypes();
		names = table.getNames();
		recordSize = table.getRecordSize();

        pfs = new PageIterator(table);
		offset = 0;
        next = null;
        countOfRecords = 0;

        if(pfs.hasNext()) {
            page = pfs.getNext();
            countOfRecords = page.readLong(Table.COUNT_OF_RECORDS);
        }

        if (countOfRecords > 0)
            next = read(page, offset);
	}
	
	public long getCurrentPageNumber() {
		return page.getPageNumber();
	}
	
	public int getCurrentOffset() {
		return offset;
	}
	
    public Type[] getTypes() {
    	return types;
    }
	
    protected HashMap<String, TableElement> read(Page page, int offset) {
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

    public void close() {
        pfs.close();
    }
    
	public HashMap<String, TableElement> next() {

    	HashMap<String, TableElement> tmp = next;
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
}
