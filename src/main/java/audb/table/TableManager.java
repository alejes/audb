package audb.table;

import java.util.HashMap;

import audb.type.Type;
import audb.page.PageManager;
import audb.page.PageStructure;


public class TableManager {

	private HashMap<String, Table> hashMap;

	public TableManager() {
		hashMap = new HashMap<String, Table>();
	}

	public boolean hasTable(String tableName) {
		return hashMap.containsKey(tableName);
	}

	public void createTable(String tableName, Type[] types, String[] names) throws Exception {
        PageStructure ps = new PageStructure(new PageManager("db/" + tableName + ".db"));
        ps.clear();
		Table table = new Table(ps);
		table.create(types, names);
		hashMap.put(tableName, table);
	}

    public Table getTable(String str) {
        return hashMap.get(str);
    }

}
