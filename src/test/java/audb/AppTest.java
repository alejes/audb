package audb;

import audb.command.Command;
import audb.page.PageCache;
import audb.page.PageStructure;
import audb.parser.Parser;
import audb.table.Table;
import audb.table.TableElement;
import audb.table.TableManager;
import audb.table.VarcharElement;
import audb.type.Type;
import audb.type.VarcharType;
import audb.util.Pair;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        assertTrue( true );
        String a = "aaa";
        String b = "aab";
        assertTrue(a.compareTo(b) < 0);
    }

    public void testAddIndex() throws Exception {
        assertTrue(true);
        Parser parser = new Parser();
        TableManager tableManager = new TableManager();
        Command.setTableManager(tableManager);

        Command command;
        String q;
        String qq = "CREATE TABLE table12 (number VARCHAR (15), text VARCHAR (9))";
        command = parser.getCommand(qq);
        command.exec();


        for (int i = 0; i < 10_000_0; i++) {
            command = parser.getCommand(String.format("INSERT INTO table12 (number, text) VALUES ('%03d', 'sadfsd')", i));
            command.exec();
        }
        q = "CREATE UNIQUE INDEX indexname ON table12 (number DESC, text ASC) USING BTREE;";

        command = parser.getCommand(q);
        command.exec();

        PageStructure.flush();
    }

    public void testUpdate() throws Exception {
        assertTrue(true);
        Parser parser = new Parser();
        TableManager tableManager = new TableManager();
        Command.setTableManager(tableManager);

        Command command;
        String q;
        String qq = "CREATE TABLE table123 (number VARCHAR (15), text VARCHAR (9))";
        command = parser.getCommand(qq);
        command.exec();


        for (int i = 0; i < 15; i++) {
            command = parser.getCommand(String.format("INSERT INTO table123 (number, text) VALUES ('%03d', 'sadfsd')", i));
            command.exec();
        }
        q = "update table123 set text = 'aaaaa' where number < 005;";

        command = parser.getCommand(q);
        command.exec();
        q = "select * from table123";
        command = parser.getCommand(q);
        Pair<Table, Iterator<HashMap<String, TableElement>>> exRes = command.exec();
        Iterator<HashMap<String, TableElement>> res = exRes.second;

        while (res.hasNext()) {
            HashMap<String, TableElement> arr = res.next();
            for (String name : arr.keySet()) {
                if (arr.get(name) instanceof VarcharElement) {
                    System.out.print(arr.get(name).showString() + " ");
                }

            }
            System.out.println();
        }

        PageStructure.flush();

        assertTrue(true);
    }

    public void testintTest() throws Exception {
        assertTrue(true);
        Parser parser = new Parser();
        TableManager tableManager = new TableManager();
        Command.setTableManager(tableManager);

        Command command;
        String q;
        String qq = "CREATE TABLE tableinttest (number INT, text VARCHAR (9));";
        command = parser.getCommand(qq);
        command.exec();

        for (int i = 0; i < 10; ++i) {
            command = parser.getCommand("INSERT INTO tableinttest (number, text) VALUES (" + i + ", 'tesxt');");
            command.exec();
        }

        q = "select * from tableinttest";
        command = parser.getCommand(q);
        Pair<Table, Iterator<HashMap<String, TableElement>>> exRes = command.exec();
        Iterator<HashMap<String, TableElement>> res = exRes.second;

        while (res.hasNext()) {
            HashMap<String, TableElement> arr = res.next();
            for (String name : arr.keySet()) {
                if (arr.get(name) instanceof VarcharElement) {
                    System.out.print(arr.get(name).toString() + " ");
                }

            }
            System.out.println();
        }

        PageStructure.flush();

        assertTrue(true);
    }

    public void testdoubleTest() throws Exception {
        assertTrue(true);
        Parser parser = new Parser();
        TableManager tableManager = new TableManager();
        Command.setTableManager(tableManager);

        Command command;
        String q;
        String qq = "CREATE TABLE tabledoubletest (number INT, text VARCHAR (9), val double);";
        command = parser.getCommand(qq);
        command.exec();

        for (int i = 0; i < 10; ++i) {
            command = parser.getCommand("INSERT INTO tabledoubletest (number, text, val) VALUES (" + i + ", 'tesxt', " + (2.34 + i) + ");");
            command.exec();
        }

        q = "select * from tabledoubletest where (val < 5.0)";
        command = parser.getCommand(q);
        Pair<Table, Iterator<HashMap<String, TableElement>>> exRes = command.exec();
        Iterator<HashMap<String, TableElement>> res = exRes.second;

      /*  while (res.hasNext()) {
            HashMap<String, TableElement> arr = res.next();
            for (String name : arr.keySet()) {
                System.out.print(arr.get(name).toString() + " ");
            }
            System.out.println();
        }*/

        PageStructure.flush();

        assertTrue(true);
    }
    
    
    public void testLoadTable() throws Exception {
        assertTrue(true);
        Parser parser = new Parser();
        TableManager tableManager = new TableManager();
        Command.setTableManager(tableManager);

        Command command;
        String q;
        Table t = tableManager.getTable("parsertab");
        //assert (t != null);

        q = "CREATE TABLE parsertab (number VARCHAR (15), text VARCHAR (9))";
        command = parser.getCommand(q);
        command.exec();

        q = "insert into parsertab (number, text) VALUES ('34343','434343')";
        command = parser.getCommand(q);
        command.exec();

        q = "select * from parsertab";
        command = parser.getCommand(q);
        Pair<Table, Iterator<HashMap<String, TableElement>>> exRes = command.exec();
        Iterator<HashMap<String, TableElement>> res = exRes.second;

        while (res.hasNext()) {
            HashMap<String, TableElement> arr = res.next();
            for (String name : arr.keySet()) {
                if (arr.get(name) instanceof VarcharElement) {
                    System.out.print(arr.get(name).toString() + " ");
                }

            }
            System.out.println();
        }
        q = "CREATE TABLE parsertab (number VARCHAR (15), text VARCHAR (9))";
        command = parser.getCommand(q);
        command.exec();
        q = "insert into parsertab (number, text) VALUES ('34343','434343')";
        command = parser.getCommand(q);
        command.exec();

        q = "select * from parsertab";
        command = parser.getCommand(q);
        command.exec();

        exRes = command.exec();
        res = exRes.second;

        while (res.hasNext()) {
            HashMap<String, TableElement> arr = res.next();
            for (String name : arr.keySet()) {
                if (arr.get(name) instanceof VarcharElement) {
                    System.out.print(arr.get(name).toString() + " ");
                }

            }
            System.out.println();
        }

        PageStructure.flush();

        assertTrue(true);
    }

    public void testHundreadNumbers() {
        try {
            TableManager tableManager = new TableManager();
            String tableName = "dummy_table";
            int columnsNumber = 3;
            
            Type types[] = new Type[columnsNumber];
            String names[] = new String[columnsNumber];
            
            for (int i = 0; i < columnsNumber; ++i) {
            	types[i] = new VarcharType((byte)10);
            	names[i] = "column" + Integer.toString(i);
            }
            tableManager.createTable(tableName, types, names);
        	PageCache pc = PageCache.getInstance();
            
        } catch(Exception e) {
            assertTrue(false);
        }
    }
}
