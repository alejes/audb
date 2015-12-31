package audb;

import audb.command.Command;
import audb.page.PageStructure;
import audb.parser.Parser;
import audb.parser.Shower;
import audb.table.TableManager;
import audb.page.PageCache;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Random;


public class Main {

    public static void main(String[] args) {

        Parser parser = new Parser();
        TableManager tableManager = new TableManager();
        Command.setTableManager(tableManager);

        try {
            Command command;
            //Type[] types = new Type[]{new VarcharType((byte) 15), new VarcharType((byte) 9)};
            //String[] names = new String[]{"number", "text"};

            // String qq = "CREATE TABLE table2 (number INT, text VARCHAR (9))";
            // command = parser.getCommand(qq);
            // command.exec();
            // //command = new CreateTableCommand("table1", types, names);
            // //command.exec();

            // for (int i = 0; i < 1_000_000; i++) {
            //     String q = String.format("INSERT INTO table2 (number, text) VALUES (%d, '"
            //         + generateString(new Random(), "abcdef", 7) + "')", i);
            //     command = parser.getCommand(q);
            //     command.exec();
            //     if (i % 100000 == 0) {
            //         System.out.format("%d\n", i);
            //     }
            // }
            // System.out.println("Ready.");

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
            	long t = System.currentTimeMillis();
                PageCache.totalNumber = 0;
                PageCache.hitNumber = 0;
                
                Shower.show(s);
                System.out.println("Elapsed time: " + Long.toString(System.currentTimeMillis() - t) + " ms.");
                System.out.println("Total page read number: " + PageCache.totalNumber + "; hit number " +
                    PageCache.hitNumber);
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
    public static String generateString(Random rng, String characters, int length) {
        char[] text = new char[length];
        for (int i = 0; i < length; i++)
        {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);
    }
}
