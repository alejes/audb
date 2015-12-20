package audb;

import audb.command.Command;
import audb.page.PageStructure;
import audb.parser.Parser;
import audb.table.Table;
import audb.table.TableElement;
import audb.table.TableManager;
import audb.table.VarcharElement;
import audb.type.Type;
import audb.type.VarcharType;
import audb.util.Pair;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;

public class Main {

    public static void main(String[] args) {

        Parser parser = new Parser();
        TableManager tableManager = new TableManager();
        Command.setTableManager(tableManager);

        try {
            Command command;
            Type[] types = new Type[]{new VarcharType((byte) 15), new VarcharType((byte) 9)};
            String[] names = new String[]{"number", "text"};

            String qq = "CREATE TABLE table1 (number VARCHAR (15), text VARCHAR (9))";
            command = parser.getCommand(qq);
            command.exec();
            //command = new CreateTableCommand("table1", types, names);
            //command.exec();


            for (int i = 0; i < 10_00; i++) {
                String q = String.format("INSERT INTO table1 (number, text) VALUES ('%03d', 'sadfsd')", i);
                command = parser.getCommand(q);
                command.exec();
                if (i % 10000 == 20) {
                    System.out.format("%d\n", i);
                }
            }

            //вставлять 10кк с 8 мб heap
            //select no fullscan where and etc.
            //shutdown
            //join

/*
            Table t = tableManager.getTable("table1");
            Order[] orders = new Order[1];
            String[] indexNames = new String[1];
            indexNames[0] = names[0];
            orders[0] = Order.ASC;
            t.addBTreeIndex(indexNames, orders);
  */
            String s;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while ((s = in.readLine()) != null && s.length() != 0) {
                try {
                    command = parser.getCommand(s);
                    Pair<Table, Iterator<HashMap<String, TableElement>>> exRes = command.exec();
                    if (null == exRes) {
                        continue;
                    }
                    for (int tableIter = 0; tableIter < exRes.first.getNames().length; ++tableIter) {
                        if ((Parser.selectList == null) || Parser.selectList.isEmpty() || Parser.selectList.contains(exRes.first.getNames()[tableIter])) {
                            System.out.print(String.format("%24s", exRes.first.getNames()[tableIter] + "   " + exRes.first.getTypes()[tableIter] + " |"));
                        }
                    }
                    System.out.println();

                    Iterator<HashMap<String, TableElement>> res = exRes.second;

                    if (null == res)
                        continue;

                    while (res.hasNext()) {
                        HashMap<String, TableElement> arr = res.next();
                        for (String name : arr.keySet()) {
                            if ((Parser.selectList == null) || Parser.selectList.isEmpty() || Parser.selectList.contains(name)) {
                                if (arr.get(name) instanceof VarcharElement) {
                                    System.out.print(String.format("%25s", arr.get(name).showString() + " |"));
                                }
                            }
                        }
                        if (!arr.isEmpty()) {
                            System.out.println();
                        }
                    }
                } catch (Exception exp) {
                    System.out.println("Error: " + exp.getMessage() + exp.toString());
                }
                
            }
            PageStructure.flush();
        } catch(Exception e) {
             e.printStackTrace();
        }
        
        /**
         * For Roma
         */
        // try {
        //     PageManager pm = new PageManager("db/test.db");
        //     PageStructure ps = new PageStructure(pm);
        //     ps.clear();
        //     // long p1 = ps.getEmptyPage();
        //     // long p2 = ps.getEmptyPage();
        //     // long p3 = ps.getEmptyPage();
        //     // ps.releasePage(p2);
        //     // p2 = ps.getEmptyPage();
        //     // p2 = ps.getEmptyPage();
        //     Type[] types = new Type[]{new VarcharType(3), new VarcharType(9)};
        //     String[] names = new String[]{"number", "text"};
        //     Table table = new Table(ps);
        //     table.create(types, names);

        //     for(int i = 0; i < 20; i++) {
        //         String s1 = String.format("%03d", i);;
        //         String s2 = "some_text";
        //         Object arr[] = new Object[]{s1, s2};
                
        //         table.addRecord(arr);
        //     }

        //     PageFullScan pfs = new PageFullScan(table);
        //     while(pfs.hasNext()) {
        //         System.err.println(pfs.getNext().getPageNumber());
        //     }
            

        //     PageStructure.flush();
        // } catch(Exception e) {
        //      e.printStackTrace();
        // }

    }

}
