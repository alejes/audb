package audb;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import audb.command.Command;
import audb.command.CreateTableCommand;
import audb.command.InsertCommand;
import audb.page.PageStructure;
import audb.parser.Parser;
import audb.table.Table;
import audb.table.TableManager;
import audb.type.Type;
import audb.type.VarcharType;

public class Main {

    public static void main(String[] args) {

        Parser parser = new Parser();
        TableManager tableManager = new TableManager();
        Command.setTableManager(tableManager);

        try {
            Command command;
            Type[] types = new Type[]{new VarcharType(3), new VarcharType(9)};
            String[] names = new String[]{"number", "text"};

            command = new CreateTableCommand("table1", types, names);
            command.exec();

            for(int i = 0; i < 15; i++) {
                String s1 = String.format("%03d", i);;
                String s2 = "some_text";
                Object arr[] = new Object[]{s1, s2};

                command = new InsertCommand("table1", arr);
                command.exec();
            }
            
            String s;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while ((s = in.readLine()) != null && s.length() != 0) {
                command = parser.getCommand(s);
                Table res = command.exec();
                
                if (null == res)
                	continue;
                
                for (Object[] arr : res) {
                    for(int i = 0; i < arr.length; i++) {
                        if(res.getTypes()[i] instanceof VarcharType) {
                            System.out.print(((String)arr[i]) + " ");
                        }
                    }
                    System.out.println();
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
