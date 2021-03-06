// package audb;

// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.Iterator;
// import java.util.List;
// import java.util.Random;

// import audb.command.Command;
// import audb.command.Constraint;
// import audb.command.Constraint.ConstraintType;
// import audb.command.CreateTableCommand;
// import audb.command.InsertCommand;
// import audb.command.SelectCommand;
// import audb.index.Index.Order;
// import audb.parser.Parser;
// import audb.table.IntegerElement;
// import audb.table.Table;
// import audb.table.TableElement;
// import audb.table.TableManager;
// import audb.table.VarcharElement;
// import audb.type.IntegerType;
// import audb.type.Type;
// import audb.type.VarcharType;
// import audb.util.Pair;
// import audb.util.Third;
// import junit.framework.Test;
// import junit.framework.TestCase;
// import junit.framework.TestSuite;

// public class BTreeTest extends TestCase {
// 	final Random random = new Random();

// 	public BTreeTest(String testName) {
// 		super(testName);
// 	}

// 	public static Test suite() {
// 		return new TestSuite(BTreeTest.class);
// 	}

// 	public void testDummy() {
// 		assertTrue(true);
// 	}

// 	public void testMain() throws Exception {
// 		 Parser parser = new Parser();
// 		 TableManager tableManager = new TableManager();
// 		 Command.setTableManager(tableManager);
// 		 Command command;
//         //Type[] types = new Type[]{new VarcharType((byte) 15), new VarcharType((byte) 9)};
//         //String[] names = new String[]{"number", "text"};

// 		 String tableName = "mainTable";
//         String qq = "CREATE TABLE " + tableName + " (number VARCHAR (15), text VARCHAR (9))";
//         command = parser.getCommand(qq);
//         command.exec();
//         //command = new CreateTableCommand("table1", types, names);
//         //command.exec();

        
        
//         for (int i = 0; i < 10_00; i++) {
//             String q = String.format("INSERT INTO " + tableName+ " (number, text) VALUES ('%03d', 'sadfsd')", i);
//             command = parser.getCommand(q);
//             command.exec();
//             if (i % 10000 == 20) {
//                 System.out.format("%d\n", i);
//             }
//         }
        
// 		Order[] orders = new Order[1];
// 		String[] indexNames = new String[1];
// 		indexNames[0] = tableName + ".number";
// 		orders[0] = Order.ASC;
// 		Table t = tableManager.getTable(tableName);
// 		t.addBTreeIndex(indexNames, orders);
		
// 		List<Third<String, Constraint, String>> constrs = new ArrayList<>();
// 		constrs.add(Third.newThird(tableName + "." + "number", new Constraint(ConstraintType.LESS,
// 				new VarcharElement("005", (VarcharType) t.getTypes()[0])), tableName));
// 		SelectCommand sc = new SelectCommand(tableName, constrs);
// 		Pair<Table, Iterator<HashMap<String, TableElement>>> result = sc.exec();
// 		Iterator<HashMap<String, TableElement>> iter = result.second;

// 		int size = 0;
// 		while (iter.hasNext()) {
// 			size++;
// 			HashMap<String, TableElement> row = iter.next();
// 		}
// 		assertEquals(5, size);
        
// 	}
	
// 	public void testIndexOnEmpty() throws Exception {
// 		//Parser parser = new Parser();
// 		TableManager tableManager = new TableManager();
// 		Command.setTableManager(tableManager);

// 		Command command;
// 		Type[] types = new Type[]{new VarcharType((byte)3), new VarcharType((byte)9)};
// 		String[] names = new String[]{"number", "text"};

// 		String tableName = "table1";
// 		command = new CreateTableCommand(tableName, types, names);
// 		command.exec();

// 		try {
// 		Table t = tableManager.getTable(tableName);
// 		Order[] orders = new Order[1];
// 		String[] indexNames = new String[1];
// 		indexNames[0] = tableName + "." + names[0];
// 		orders[0] = Order.ASC;
// 		t.addBTreeIndex(indexNames, orders);
// 		} catch (Exception e) {
// 			e.printStackTrace(System.out);
// 			assertTrue(false);
// 		}

// 	}


// 	public void testCreateIndex() throws Exception {
// 		Parser parser = new Parser();
// 		TableManager tableManager = new TableManager();
// 		Command.setTableManager(tableManager);

// 		Command command;
// 		Type[] types = new Type[]{new VarcharType((byte)3), new VarcharType((byte)9)};
// 		String[] names = new String[]{"number", "text"};

// 		String tableName = "table2";
// 		command = new CreateTableCommand(tableName, types, names);
// 		command.exec();

// 		for(int i = 0; i < 15; i++) {
// 			String s1 = String.format("%03d", i);
// 			String s2 = "some_text";
// 			Object arr[] = new Object[]{s1, s2};

// 			command = new InsertCommand(tableName, arr);
// 			command.exec();
// 		}

// 		Table t = tableManager.getTable(tableName);
// 		Order[] orders = new Order[1];
// 		String[] indexNames = new String[1];
// 		indexNames[0] = tableName + "." + names[0];
// 		orders[0] = Order.ASC;
// 		t.addBTreeIndex(indexNames, orders);
// 	}


// 	public void testfindIndex() throws Exception {
// 		//Parser parser = new Parser();
// 		TableManager tableManager = new TableManager();
// 		Command.setTableManager(tableManager);

// 		Command command;
// 		Type[] types = new Type[]{new VarcharType((byte)3), new VarcharType((byte)9)};
// 		String[] names = new String[]{"number", "text"};

// 		String tableName = "table3";
// 		command = new CreateTableCommand(tableName, types, names);
// 		command.exec();

// 		for(int i = 0; i < 15; i++) {
// 			String s1 = String.format("%03d", i);
// 			String s2 = "some_text";
// 			Object arr[] = new Object[]{s1, s2};

// 			command = new InsertCommand(tableName, arr);
// 			command.exec();
// 		}

// 		Table t = tableManager.getTable(tableName);
// 		Order[] orders = new Order[1];
// 		String[] indexNames = new String[1];
// 		indexNames[0] = tableName + "." + names[0];
// 		orders[0] = Order.ASC;
// 		t.addBTreeIndex(indexNames, orders);

// 		List<Third<String, Constraint, String>> constrs = new ArrayList<>();
// 		SelectCommand sc = new SelectCommand(tableName, constrs);
// 		Pair<Table, Iterator<HashMap<String, TableElement>>> result = sc.exec();
// 		Iterator<HashMap<String, TableElement>> iter = result.second;

// 		int size = 0;
// 		while (iter.hasNext()) {
// 			size++;
// 			HashMap<String, TableElement> row = iter.next();
// 		}
// 		assertEquals(15, size);
	
// 		System.out.println(" ");
// 		constrs.add(Third.newThird(tableName + "." + "number", new Constraint(ConstraintType.GREATER,
// 				new VarcharElement("002", (VarcharType) types[0])), tableName));
// 		sc = new SelectCommand(tableName, constrs);
// 		result = sc.exec();
// 		iter = result.second;
// 		size = 0;
// 		while (iter.hasNext()) {
// 			size++;
// 			HashMap<String, TableElement> row = iter.next();
// 		}
// 		assertEquals(size, 12);
		
// 		System.out.println(" ");
// 		constrs.add(Third.newThird(tableName + "." + "number", new Constraint(ConstraintType.GREATER,
// 				new VarcharElement("001", (VarcharType) types[0])), tableName));
// 		sc = new SelectCommand(tableName, constrs);
// 		result = sc.exec();
// 		iter = result.second;
// 		size = 0;
// 		while (iter.hasNext()) {
// 			size++;
// 			HashMap<String, TableElement> row = iter.next();
// 		}
// 		assertEquals(12, size);
		
// 		System.out.println(" ");
// 		constrs.add(Third.newThird(tableName + "." + "number", new Constraint(ConstraintType.GREATER,
// 				new VarcharElement("005", (VarcharType) types[0])),tableName));
// 		sc = new SelectCommand(tableName, constrs);
// 		result = sc.exec();
// 		iter = result.second;
// 		size = 0;
// 		System.out.println("HERE");
// 		while (iter.hasNext()) {
// 			size++;
// 			HashMap<String, TableElement> row = iter.next();
// 			System.out.println(row.get(tableName + "." + "number"));
// 		}
		
// 		assertEquals(9, size);
// 	}
	
// 	public void testCompositKey() throws Exception {
// 		//Parser parser = new Parser();
// 		TableManager tableManager = new TableManager();
// 		Command.setTableManager(tableManager);

// 		Command command;
// 		Type[] types = new Type[]{new IntegerType(Type.INT), new VarcharType((byte)3), new VarcharType((byte)9)};
// 		String[] names = new String[]{"id", "number", "text"};

// 		String tableName = "table4";
// 		command = new CreateTableCommand(tableName, types, names);
// 		command.exec();

// 		int indexSize = 2;
// 		Table t = tableManager.getTable(tableName);
// 		Order[] orders = new Order[indexSize];
// 		String[] indexNames = new String[indexSize];
// 		indexNames[0] = tableName + "." + names[0];
// 		indexNames[1] = tableName + "." + names[1];
// 		orders[0] = Order.ASC;
// 		orders[1] = Order.ASC;
						
// 		t.addBTreeIndex(indexNames, orders);
// 		// insert values
// 		for(int i = 0; i < 150000; i++) {
// 			Integer id = i;
// 			String s1 = String.format("%03d", i % 100);
// 			String s2 = "some_text";
// 			Object arr[] = new Object[]{id, s1, s2};

// 			command = new InsertCommand(tableName, arr);
// 			command.exec();
			
// 		}

// 		List<Third<String, Constraint, String>> constrs = new ArrayList<>();

// 		constrs.add(Third.newThird(tableName + "." + "number", new Constraint(ConstraintType.GREATER,
// 				new VarcharElement("002", (VarcharType) types[1])), tableName));
		
// 		SelectCommand sc = new SelectCommand(tableName, constrs);
// 		Pair<Table, Iterator<HashMap<String, TableElement>>> result = sc.exec();
// 		Iterator<HashMap<String, TableElement>> iter = result.second;
// 		int size = 0;
// 		while (iter.hasNext()) {
// 			size++;
// 			HashMap<String, TableElement> row = iter.next();
// 		}
		
// 		assertEquals(145500, size);
	
// 		long t1 = System.currentTimeMillis();
// 		constrs.add(Third.newThird(tableName + "." + "id", new Constraint(ConstraintType.LESS,
// 				new IntegerElement(10, types[0])), tableName));
// 		System.out.println(" ");
// 		sc = new SelectCommand(tableName, constrs);
// 		result = sc.exec();
// 		iter = result.second;
// 		size = 0;
		
		
		
// 		while (iter.hasNext()) {
// 			size++;
// 			HashMap<String, TableElement> row = iter.next();
// 			System.out.println(row.get(tableName + "." + "number"));
// 		}
// 		long t2 = System.currentTimeMillis();
// 		System.out.println("Time passed " + Long.toString(t2 - t1));
// 		assertEquals(7,size);
// 	}
	
// 	public void testAlexei() throws Exception {
// 		//Parser parser = new Parser();
// 		TableManager tableManager = new TableManager();
// 		Command.setTableManager(tableManager);

// 		Command command;
// 		Type[] types = new Type[]{new VarcharType((byte)3), new VarcharType((byte)9)};
// 		String[] names = new String[]{"number", "text"};

// 		String tableName = "table5";
// 		command = new CreateTableCommand(tableName, types, names);
// 		command.exec();

// 		for(int i = 0; i < 15; i++) {
// 			String s1 = String.format("%03d", i);
// 			String s2 = "some_text";
// 			Object arr[] = new Object[]{s1, s2};

// 			command = new InsertCommand(tableName, arr);
// 			command.exec();
// 		}


// 		List<Third<String, Constraint, String>> constrs = new ArrayList<>();

// 		constrs.add(Third.newThird(tableName + "." + "number", new Constraint(ConstraintType.LESS,
// 				new VarcharElement("002", (VarcharType) types[1])), tableName));
// 		constrs.add(Third.newThird(tableName + "." + "number", new Constraint(ConstraintType.GREATER_OR_EQUAL,
// 				new VarcharElement("001", (VarcharType) types[1])), tableName));
// 		SelectCommand sc = new SelectCommand(tableName, constrs);
// 		Pair<Table, Iterator<HashMap<String, TableElement>>> result = sc.exec();
// 		Iterator<HashMap<String, TableElement>> iter = result.second;
// 		int size = 0;
// 		while (iter.hasNext()) {
// 			size++;
// 			HashMap<String, TableElement> row = iter.next();
// 			System.out.println(row.get(tableName + "." + "number"));
// 		}
// 		assertEquals(1, size);
// 	}

// 	/*
//     public void testInsert() {
//         BTree t = new BTree(5);

//         HashMap<Integer, String> hm = new HashMap<Integer, String>();
//         for (int i = 0; i < 100; i++) {
//         	hm.put(i, Integer.toString(i));
//         }

//         for (int i = 0; i < 2; i++)
//         	t.insert(i, hm.get(i));

//         assertEquals(1, t.find(0).size());
//         assertEquals(hm.get(0), t.find(0).get(0));

//         for (int i = 2; i < 13; i++)
//         	t.insert(i, hm.get(i));

//         for (int i = 0; i < 13; i++) {
// 	        assertEquals(1, t.find(i).size());
// 	        assertEquals(hm.get(i), t.find(i).get(0));
//         }

//         // now 2 
//         t.insert(3, "test");
//         assertEquals(2, t.find(3).size());
//         assertEquals(true, t.find(3).contains("3"));
//         assertEquals(true, t.find(3).contains("test"));

//         for (int i = 13; i < 100; i++) {
//         	t.insert(i, hm.get(i));
//         }

//         assertEquals(1, t.find(21).size());
//         assertEquals("18", t.find(18).get(0));
//         assertEquals("67", t.find(67).get(0));
//     }

//     public void testRemove() {
//         BTree<Integer, String> t = new BTree<Integer, String>(5);

//         HashMap<Integer, String> hm = new HashMap<Integer, String>();
//         for (int i = 0; i < 100; i++) {
//         	hm.put(i, Integer.toString(i));
//         }

//         for (int i = 0; i < 100; i++)
//         	t.insert(i, hm.get(i));

//         assertTrue(t.find(67).size() == 1);
//         t.remove(67);
//         assertTrue(t.find(67).size() == 0);
//         for (int i = 0; i < 100; i++)
//         	t.remove(i);

//         for (int i = 0; i < 100; i++)
//         	assertEquals(0, t.find(i).size());
//     }

//     public void testMixed() {
//         BTree<Integer, String> t = new BTree<Integer, String>(10);

//         HashMap<Integer, String> hm = new HashMap<Integer, String>();
//         for (int i = 0; i < 100; i++) {
//         	hm.put(i, Integer.toString(i));
//         }

//         for (int j = 0; j < 10; j++) {
//         	for (int i = 0; i < 100; i++)
//             	t.insert(i, hm.get(i));
//             assertTrue(t.find(67).size() == 1);
//             t.remove(67);
//             assertTrue(t.find(67).size() == 0);
//             for (int i = 0; i < 100; i++)
//             	t.remove(i);
//             for (int i = 0; i < 100; i++)
//             	assertEquals(0, t.find(i).size());
//         }

//         Random r = new Random(System.currentTimeMillis());
//         List<Integer> randomNumbers = new LinkedList<Integer>();
//         for (int i = 0; i < 100; i++) {
//         	int randNum = r.nextInt();
//         	t.insert(randNum, "string");
//         	randomNumbers.add(randNum);
//         }

//         for (Integer i : randomNumbers) {
//         	assertTrue(t.find(i).size() >= 1);
//         }
//     }

//     public void testFindAll() {
//         BTree<Integer, String> t = new BTree<Integer, String>(6);

//         HashMap<Integer, String> hm = new HashMap<Integer, String>();
//         for (int i = 0; i < 100; i++) {
//         	hm.put(i, Integer.toString(i));
//         }

//         for (int i = 0; i < 100; i++)
//         	t.insert(i, hm.get(i));

//         ArrayList<Integer> excludeList = new ArrayList<Integer>();
//         excludeList.add(12);
//         excludeList.add(13);
//         List<String> result = t.findAll(4, 15, excludeList);

//         assertEquals(10, result.size());
//     }*/
// }
