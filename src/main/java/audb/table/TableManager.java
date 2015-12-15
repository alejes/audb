package audb.table;

import java.util.HashMap;
import java.io.File;

import audb.type.Type;
import audb.page.PageManager;
import audb.page.PageStructure;


public class TableManager {

	private HashMap<String, Table> hashMap;
    private String dir = "db/";

	public TableManager() {
		hashMap = new HashMap<String, Table>();
        for (final File fileEntry : new File(dir).listFiles()) {
            if (!fileEntry.isDirectory() && fileEntry.getName().endsWith(".db")) {
                String str = fileEntry.getName();
                str = str.substring(0, str.lastIndexOf('.'));
                try {
                    PageStructure ps = new PageStructure(new PageManager("db/" + str + ".db"));
                    Table table = new Table(ps);
                    table.init();
                    hashMap.put(str, table);
                } catch(Exception e) {
                    System.err.println("Can't open table " + str);
                }
            }
        }
	}

	public boolean hasTable(String tableName) {
		return hashMap.containsKey(tableName);
	}

	public void createTable(String tableName, Type[] types, String[] names) throws Exception {
        if(hasTable(tableName)) {
            Table table = hashMap.get(tableName);
            table.create(types, names);
            // System.err.println("creating " + tableName);
            return;
        }
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
