package audb;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import audb.command.*;
import audb.util.*;
import audb.result.*;
import audb.page.PageStructure;
import audb.table.Table;
import audb.table.TableElement;
import audb.table.TableManager;
import audb.table.VarcharElement;
import audb.type.Type;
import audb.type.VarcharType;

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

    public void testUpdate() {
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

            for(int i = 0; i < 100; i++) {
                String s1 = String.format("%07d", i);
                String s2 = "some_text";
                Object arr[] = new Object[]{s1, s2};

                command = new InsertCommand("table1", arr);
                command.exec();
            }
            
            Table t = tableManager.getTable("table1");
            

            Iterator<HashMap<String, TableElement>> it = new FullScanIterator(t);
            HashMap<String, Object> newValues = new HashMap<String, Object>();
            newValues.put("number", "1");
            command = new UpdateCommand(it, newValues);
            // command = new DeleteCommand(it);
            command.exec();
            it = new FullScanIterator(t);
            while (it.hasNext()) {
                HashMap<String, TableElement> arr = it.next();
                for (String name : arr.keySet()) {
                    if (arr.get(name) instanceof VarcharElement) {
                        System.out.print(arr.get(name).toString() + " ");
                    }
                    System.out.println();
                }
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


    public void testJoin() {
        if (true)
            return;

        TableManager tableManager = new TableManager();
        Command.setTableManager(tableManager);

        try {
            Command command;
            Type[] types = new Type[]{new VarcharType((byte)3), new VarcharType((byte)3)};
            String[] names = new String[]{"a_field", "b_field"};

            command = new CreateTableCommand("join1", types, names);
            command.exec();
            command = new CreateTableCommand("join2", types, names);
            command.exec();

            String s1;
            String s2;
            Object arr[];

            s1 = "00a";
            s2 = "001";
            arr = new Object[]{s1, s2};
            command = new InsertCommand("join1", arr);
            command.exec();

            s1 = "00b";
            s2 = "002";
            arr = new Object[]{s1, s2};
            command = new InsertCommand("join1", arr);
            command.exec();

            s1 = "01b";
            s2 = "002";
            arr = new Object[]{s1, s2};
            command = new InsertCommand("join1", arr);
            command.exec();

            s1 = "001";
            s2 = "00c";
            arr = new Object[]{s1, s2};
            command = new InsertCommand("join2", arr);
            command.exec();

            s1 = "002";
            s2 = "00d";
            arr = new Object[]{s1, s2};
            command = new InsertCommand("join2", arr);
            command.exec();

            Table t1 = tableManager.getTable("join1");
            Table t2 = tableManager.getTable("join2");
            Iterator<HashMap<String, TableElement>> it;
            Iterator<HashMap<String, TableElement>> it1;
            List<Pair<String, String>> list;
            list = new LinkedList();
            list.add(Pair.newPair("join1.b_field", "join2.a_field"));
            it1 = new FullScanIterator(t1);
            it = new JoinIterator(it1, list, new LinkedList<Third<String, Constraint, String>>(), t2);

            
            while (it.hasNext()) {
                HashMap<String, TableElement> elem = it.next();
                for (String name : elem.keySet()) {
                    if (elem.get(name) instanceof VarcharElement) {
                        System.out.print(elem.get(name).toString() + " ");
                    }
                }
                System.out.println();
            }

            PageStructure.flush();
        } catch(Exception e) {
             e.printStackTrace();
        }    
    }
}
