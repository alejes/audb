package audb;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;

import audb.command.Command;
import audb.command.CreateTableCommand;
import audb.command.InsertCommand;
import audb.page.PageStructure;
import audb.table.Table;
import audb.table.TableElement;
import audb.table.TableManager;
import audb.table.VarcharElement;
import audb.type.Type;
import audb.type.VarcharType;
import audb.result.FullScanIterator;

public class PageTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public PageTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(PageTest.class);
    }

    public void testLoading() {
        if (true)
            return;
        
        TableManager tableManager = new TableManager();
        Command.setTableManager(tableManager);

        try {
            Command command;
            Type[] types = new Type[]{new VarcharType((byte)7), new VarcharType((byte)9)};
            String[] names = new String[]{"number", "text"};

            // command = new CreateTableCommand("table2", types, names);
            // command.exec();
            Table t = tableManager.getTable("table2");

            for(int i = 0; i < 100; i++) {
                System.out.println("elements inserted " + i);
                String s1 = String.format("%07d", i);
                String s2 = "some_text";
                Object arr[] = new Object[]{s1, s2};

                command = new InsertCommand("table2", arr);
                command.exec();
            }

            

            Iterator<HashMap<String, TableElement>> it = new FullScanIterator(t);
            int counter = 0;
            while (it.hasNext()) {
                HashMap<String, TableElement> arr = it.next();
                
                for (String name : arr.keySet()) {
                    if (arr.get(name) instanceof VarcharElement) {
                        System.out.print(arr.get(name).toString() + " ");
                    }

                }
                System.out.println();
    
                ++counter;
            }

            PageStructure.flush();
        } catch(Exception e) {
             e.printStackTrace();
        }    
    }

    public void testMillionsNumbers() {
        if (true)
            return;

        TableManager tableManager = new TableManager();
        Command.setTableManager(tableManager);

        try {
            Command command;
            Type[] types = new Type[]{new VarcharType((byte)7), new VarcharType((byte)9)};
            String[] names = new String[]{"number", "text"};

            command = new CreateTableCommand("table1", types, names);
            command.exec();

            for(int i = 0; i < 1000000; i++) {
                if (i % 1000 == 0)
                    System.out.println("elements inserted " + i);
                String s1 = String.format("%07d", i);
                String s2 = "some_text";
                Object arr[] = new Object[]{s1, s2};

                command = new InsertCommand("table1", arr);
                command.exec();
            }
            
            Table t = tableManager.getTable("table1");
            

            Iterator<HashMap<String, TableElement>> it = new FullScanIterator(t);
            int counter = 0;
            while (it.hasNext()) {
                HashMap<String, TableElement> arr = it.next();
                if (counter % 100 == 0) {   
                    for (String name : arr.keySet()) {
                        if (arr.get(name) instanceof VarcharElement) {
                            System.out.print(arr.get(name).toString() + " ");
                        }

                    }
                    System.out.println();
                }
                ++counter;
            }

            PageStructure.flush();
        } catch(Exception e) {
             e.printStackTrace();
        }    
    }
}
