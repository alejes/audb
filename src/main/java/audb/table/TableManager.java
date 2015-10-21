package audb.table;

import java.util.HashMap;

import audb.type.Type;
import audb.page.PageManager;
import audb.page.PageCache;


public class TableManager {

	private HashMap<String, Table> hashMap;
    private PageCache pageCache;

	public TableManager() {
		hashMap = new HashMap<String, Table>();
        pageCache = new PageCache();
	}

	public boolean hasTable(String tableName) {
		return hashMap.containsKey(tableName);
	}

	public void createTable(String tableName, Type[] types, String[] names) throws Exception {
		Table table = new Table(new PageManager("db/" + tableName), pageCache);
		table.create(types, names);
		hashMap.put(tableName, table);
	}

    public Table getTable(String str) {

        return hashMap.get(str);
    }

    public void close() {
    	pageCache.close();
    }

}
