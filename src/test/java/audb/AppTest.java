package audb;

import audb.command.Command;
import audb.page.PageCache;
import audb.page.PageStructure;
import audb.parser.Parser;
import audb.parser.Shower;
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

    public void testBigTestFor4M() throws Exception {
        assertTrue(true);
        Parser parser = new Parser();
        TableManager tableManager = new TableManager();
        Command.setTableManager(tableManager);


        parser.getCommand("CREATE TABLE tableload4m (number VARCHAR (15), text VARCHAR (9))").exec();


        for (int i = 0; i < 100_000_0; i++) {
            parser.getCommand(String.format("INSERT INTO tableload4m (number, text) VALUES ('%09d', 'sadfsd')", i)).exec();
            if (i % 10000 == 25) {
                System.out.println("Insert " + i + " items");
            }
        }

        PageStructure.flush();
    }


    public void testAddIndex() throws Exception {
        assertTrue(true);
        Parser parser = new Parser();
        TableManager tableManager = new TableManager();
        Command.setTableManager(tableManager);


        parser.getCommand("CREATE TABLE table12 (number VARCHAR (15), text VARCHAR (9))").exec();


        for (int i = 0; i < 10; i++) {
            parser.getCommand(String.format("INSERT INTO table12 (number, text) VALUES ('%03d', 'sadfsd')", i)).exec();
        }

        parser.getCommand("CREATE UNIQUE INDEX indexname ON table12 (number DESC, text ASC) USING BTREE;").exec();

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
        q = "update table123 set text = 'aaaaa' where number < '005';";

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

    public void testDeleteNullPointerTest() throws Exception {
        assertTrue(true);
        Parser parser = new Parser();
        TableManager tableManager = new TableManager();
        Command.setTableManager(tableManager);

        parser.getCommand("CREATE TABLE tbldeltetest2 (number VARCHAR (15), text VARCHAR (9));").exec();

        for (int i = 0; i < 10; ++i) {
            parser.getCommand("INSERT INTO tbldeltetest2 (number, text) VALUES ('00" + i + "', 'tesxt');").exec();
        }

        parser.getCommand("DELETE from tbldeltetest2 WHERE number < '005'").exec();
        parser.getCommand("DELETE from tbldeltetest2 WHERE number < '007'").exec();
        parser.getCommand("DELETE from tbldeltetest2 WHERE (((((number < '010')))))").exec();

        PageStructure.flush();

        assertTrue(true);
    }

    public void testSimpleUpdateTest() throws Exception {
        assertTrue(true);
        Parser parser = new Parser();
        TableManager tableManager = new TableManager();
        Command.setTableManager(tableManager);

        Command command;
        String q;
        String qq = "CREATE TABLE simplupdtest (number VARCHAR (9), text VARCHAR (9));";
        command = parser.getCommand(qq);
        command.exec();
        command = parser.getCommand("INSERT INTO simplupdtest (number, text) VALUES ('77777', '55555')");
        command.exec();
        command = parser.getCommand("UPDATE simplupdtest SET text = '88888'");
        command.exec();
        q = "select * from simplupdtest";
        command = parser.getCommand(q);
        Pair<Table, Iterator<HashMap<String, TableElement>>> exRes = command.exec();
        Iterator<HashMap<String, TableElement>> res = exRes.second;

        while (res.hasNext()) {
            HashMap<String, TableElement> arr = res.next();
            /*
            for (String name : arr.keySet()) {
                System.out.print(arr.get(name).toString() + " ");
            }
            */
            System.out.println(arr.get("simplupdtest.text").toString());

            assertTrue(arr.get("simplupdtest.text").showString().compareTo("88888") == 0);
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

        while (res.hasNext()) {
            HashMap<String, TableElement> arr = res.next();
            for (String name : arr.keySet()) {
                System.out.print(arr.get(name).toString() + " ");
            }
            System.out.println();
        }

        PageStructure.flush();

        assertTrue(true);
    }

    public void testOfJoins() throws Exception {
        assertTrue(true);
        Parser parser = new Parser();
        TableManager tableManager = new TableManager();
        Command.setTableManager(tableManager);

        parser.getCommand("CREATE TABLE table002 (number VARCHAR (15), text VARCHAR (9))").exec();
        parser.getCommand("CREATE TABLE table003 (number VARCHAR (15), text VARCHAR (9))").exec();

        for (int i = 0; i < 15; i++) {
            parser.getCommand(String.format("INSERT INTO table002 (number, text) VALUES ('%03d', 'sadfsd')", i)).exec();
        }
        for (int i = 0; i < 15; i++) {
            parser.getCommand(String.format("INSERT INTO table003 (number, text) VALUES ('%03d', 'sadfsd')", i)).exec();
        }

        Shower.show_exsept("SELECT * FROM table003 JOIN table002 ON table003.number = table002.number WHERE (table002.number <= '010') and (table003.number >= '007')");

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
