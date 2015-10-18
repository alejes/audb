package audb.table;

import java.util.HashMap;

import audb.type.Type;


public class TableManager {

	private HashMap<String, Table> hashMap;

	public TableManager() {
		hashMap = new HashMap<String, Table>();
	}

	public boolean hasTable(String tableName) {
		return hashMap.containsKey(tableName);
	}

	public void createTable(String tableName, Type[] types, String[] names) throws Exception {
		Table table = new Table("db/" + tableName);
		table.create(types, names);
		hashMap.put(tableName, table);
	}

    public Table getTable(String str) {

        return hashMap.get(str);
    }

    public void close() {
    	for(Table table : hashMap.values()) {
    		table.close();
		}
    }

}
