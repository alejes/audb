package audb.table;

import audb.page.PageCache;
import audb.page.Page;


public class Table {

	private PageCache pageCache;

	public Table(String tablename) {
		pageCache = new PageCache("db/" + tablename);
	}

	public createTable(Type[] types) {

	}

    public Object[] read(long pageNum, int offset) {

        return null;
    }

    public void write(Object[] data) {
    	
    }

    public void close() {
    	pageCache.close();
    }

}
