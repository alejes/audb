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
        q = "CREATE UNIQUE INDEX indexname ON table1(number DESC, text ASC) USING BTREE;";

        command = parser.getCommand(q);
        command.exec();

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
