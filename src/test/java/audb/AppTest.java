package audb;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import audb.page.Page;
import audb.page.PageCache;
import audb.table.Table;
import audb.table.TableManager;
import audb.type.Type;
import audb.type.VarcharType;

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
    public void testApp()
    {
        assertTrue( true );
        String a = "aaa";
        String b = "aab";
        assertTrue(a.compareTo(b) < 0);
    }

    public void testHundreadNumbers() 
    {
        try {
            TableManager tableManager = new TableManager();
            String tableName = "dummy_table";
            int columnsNumber = 3;
            
            Type types[] = new Type[columnsNumber];
            String names[] = new String[columnsNumber];
            
            for (int i = 0; i < columnsNumber; ++i) {
            	types[i] = new VarcharType(10);
            	names[i] = "column" + Integer.toString(i);
            }
            tableManager.createTable(tableName, types, names);
        	PageCache pc = PageCache.getInstance();
            
        } catch(Exception e) {
            assertTrue(false);
        }
    }
}
