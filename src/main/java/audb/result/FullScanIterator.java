package audb.result;

import java.util.HashMap;
import java.lang.ref.WeakReference;

import audb.page.Page;
import audb.page.PageStructure;
import audb.table.PageIterator;
import audb.table.Table;
import audb.table.TableElement;
import audb.type.Type;

public class FullScanIterator implements TableIterator {

    protected WeakReference<Table> table;

 //    protected PageStructure pageStructure;
 //    protected Type[] types;
 //    protected String[] names;
	// private int recordSize;

	protected PageIterator pfs;


	protected HashMap<String, TableElement> next;
	protected Page currentElementPage;
	protected Page nextElementPage;
    int offset;
    int currentOffset = -1;
    int countOfRecords;

	public FullScanIterator(Table table) {
        this.table = new WeakReference<Table>(table);
        // pageStructure = table.getPageStructure();
		// types = table.getTypes();
		// names = table.getNames();
		// recordSize = table.getRecordSize();

        pfs = new PageIterator(table);
		offset = 0;
        next = null;
        countOfRecords = 0;

        if(pfs.hasNext()) {
            nextElementPage = currentElementPage = pfs.getNext();
            countOfRecords = currentElementPage.readInteger(Table.COUNT_OF_RECORDS);
        }

        if (countOfRecords > 0)
            next = this.table.get().read(currentElementPage, offset);
	}
	
	public int getCurrentPageNumber() {
		return currentElementPage.getPageNumber();
	}
	
	public int getCurrentOffset() {
		return currentOffset;
	}
	
    public Type[] getTypes() {
    	return table.get().getTypes();
    }

  //   protected HashMap<String, TableElement> read(Page page, int offset) {
		// int ptr = offset * recordSize;
		// HashMap<String, TableElement> objects = new HashMap<String, TableElement>();
		// for(int i = 0; i < types.length; i++) {
		// 	byte[] data = new byte[types[i].getSize()];
		// 	System.arraycopy(page.data, ptr, data, 0, types[i].getSize());
		// 	objects.put(names[i], types[i].fromBytes(data));
		// 	ptr += types[i].getSize();
		// }
  //       return objects;
  //   }

    public void close() {
        pfs.close();
    }
    
	public HashMap<String, TableElement> next() {

    	HashMap<String, TableElement> tmp = next;
        currentOffset = offset++;
    	currentElementPage = nextElementPage;
        if(offset >= countOfRecords) {
            offset = 0;
            if(pfs.hasNext()) {
                nextElementPage = pfs.getNext();
                countOfRecords = nextElementPage.readInteger(Table.COUNT_OF_RECORDS);
            } else {
                nextElementPage = null;
                countOfRecords = 0;
            }
        }

        if(countOfRecords > offset) {
            next = table.get().read(nextElementPage, offset);
        } else 
            next = null;

    	return tmp;
    }

    public boolean hasNext() {
    	return next != null;
    }
}
