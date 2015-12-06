package audb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import audb.command.Command;
import audb.command.Constraint;
import audb.command.CreateTableCommand;
import audb.command.InsertCommand;
import audb.command.SelectCommand;
import audb.index.Index.Order;
import audb.parser.Parser;
import audb.table.Table;
import audb.table.TableElement;
import audb.table.TableManager;
import audb.type.Type;
import audb.type.VarcharType;
import audb.util.Pair;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class BTreeTest extends TestCase {
	public BTreeTest(String testName) {
        super(testName);
    }
	
    public static Test suite() {
        return new TestSuite(BTreeTest.class);
    }
    
    public void testDummy() {
    	assertTrue(true);
    }
    
    public void testCreateIndex() throws Exception {
        Parser parser = new Parser();
        TableManager tableManager = new TableManager();
        Command.setTableManager(tableManager);

        Command command;
        Type[] types = new Type[]{new VarcharType((byte)3), new VarcharType((byte)9)};
        String[] names = new String[]{"number", "text"};

        String tableName = "table1";
        command = new CreateTableCommand(tableName, types, names);
        command.exec();

        for(int i = 0; i < 15; i++) {
            String s1 = String.format("%03d", i);;
            String s2 = "some_text";
            Object arr[] = new Object[]{s1, s2};

            command = new InsertCommand("table1", arr);
            command.exec();
        }
        
        Table t = tableManager.getTable(tableName);
        Order[] orders = new Order[1];
        String[] indexNames = new String[1];
        indexNames[0] = names[0];
        orders[0] = Order.ASC;
        t.addBTreeIndex(indexNames, orders);
    }
    
    public void testfindIndex() throws Exception {
        Parser parser = new Parser();
        TableManager tableManager = new TableManager();
        Command.setTableManager(tableManager);

        Command command;
        Type[] types = new Type[]{new VarcharType((byte)3), new VarcharType((byte)9)};
        String[] names = new String[]{"number", "text"};

        String tableName = "table1";
        command = new CreateTableCommand(tableName, types, names);
        command.exec();

        for(int i = 0; i < 15; i++) {
            String s1 = String.format("%03d", i);;
            String s2 = "some_text";
            Object arr[] = new Object[]{s1, s2};

            command = new InsertCommand("table1", arr);
            command.exec();
        }
        
        Table t = tableManager.getTable(tableName);
        Order[] orders = new Order[1];
        String[] indexNames = new String[1];
        indexNames[0] = names[0];
        orders[0] = Order.ASC;
        t.addBTreeIndex(indexNames, orders);
        
        List<Pair<String, Constraint>> constrs = new ArrayList<Pair<String, Constraint>>();
        SelectCommand sc = new SelectCommand(tableName, constrs);
        Pair<Table, Iterator<HashMap<String, TableElement>>> result = sc.exec();
        Iterator<HashMap<String, TableElement>> iter = result.second;
        
        int size = 0;
        while (iter.hasNext()) {
        	size++;
        	HashMap<String, TableElement> row = iter.next();
        	System.out.println(row.get("number"));
        }
        
        assertEquals(15, size);
    }
    
    /*
    public void testInsert() {
        BTree t = new BTree(5);
        
        HashMap<Integer, String> hm = new HashMap<Integer, String>();
        for (int i = 0; i < 100; i++) {
        	hm.put(i, Integer.toString(i));
        }
        
        for (int i = 0; i < 2; i++)
        	t.insert(i, hm.get(i));
        
        assertEquals(1, t.find(0).size());
        assertEquals(hm.get(0), t.find(0).get(0));
        
        for (int i = 2; i < 13; i++)
        	t.insert(i, hm.get(i));
        
        for (int i = 0; i < 13; i++) {
	        assertEquals(1, t.find(i).size());
	        assertEquals(hm.get(i), t.find(i).get(0));
        }
        
        // now 2 
        t.insert(3, "test");
        assertEquals(2, t.find(3).size());
        assertEquals(true, t.find(3).contains("3"));
        assertEquals(true, t.find(3).contains("test"));
        
        for (int i = 13; i < 100; i++) {
        	t.insert(i, hm.get(i));
        }
        
        assertEquals(1, t.find(21).size());
        assertEquals("18", t.find(18).get(0));
        assertEquals("67", t.find(67).get(0));
    }
    
    public void testRemove() {
        BTree<Integer, String> t = new BTree<Integer, String>(5);
        
        HashMap<Integer, String> hm = new HashMap<Integer, String>();
        for (int i = 0; i < 100; i++) {
        	hm.put(i, Integer.toString(i));
        }
        
        for (int i = 0; i < 100; i++)
        	t.insert(i, hm.get(i));
        
        assertTrue(t.find(67).size() == 1);
        t.remove(67);
        assertTrue(t.find(67).size() == 0);
        for (int i = 0; i < 100; i++)
        	t.remove(i);
        
        for (int i = 0; i < 100; i++)
        	assertEquals(0, t.find(i).size());
    }
    
    public void testMixed() {
        BTree<Integer, String> t = new BTree<Integer, String>(10);
        
        HashMap<Integer, String> hm = new HashMap<Integer, String>();
        for (int i = 0; i < 100; i++) {
        	hm.put(i, Integer.toString(i));
        }
        
        for (int j = 0; j < 10; j++) {
        	for (int i = 0; i < 100; i++)
            	t.insert(i, hm.get(i));
            assertTrue(t.find(67).size() == 1);
            t.remove(67);
            assertTrue(t.find(67).size() == 0);
            for (int i = 0; i < 100; i++)
            	t.remove(i);
            for (int i = 0; i < 100; i++)
            	assertEquals(0, t.find(i).size());
        }
        
        Random r = new Random(System.currentTimeMillis());
        List<Integer> randomNumbers = new LinkedList<Integer>();
        for (int i = 0; i < 100; i++) {
        	int randNum = r.nextInt();
        	t.insert(randNum, "string");
        	randomNumbers.add(randNum);
        }
        
        for (Integer i : randomNumbers) {
        	assertTrue(t.find(i).size() >= 1);
        }
    }
    
    public void testFindAll() {
        BTree<Integer, String> t = new BTree<Integer, String>(6);
        
        HashMap<Integer, String> hm = new HashMap<Integer, String>();
        for (int i = 0; i < 100; i++) {
        	hm.put(i, Integer.toString(i));
        }
        
        for (int i = 0; i < 100; i++)
        	t.insert(i, hm.get(i));
        
        ArrayList<Integer> excludeList = new ArrayList<Integer>();
        excludeList.add(12);
        excludeList.add(13);
        List<String> result = t.findAll(4, 15, excludeList);
        
        assertEquals(10, result.size());
    }*/
}
