package audb;

import audb.page.Page;
import audb.page.PageCache;
import audb.parser.Parser;
import audb.command.Command;
import audb.result.Result;
import audb.result.FullScanResult;
import audb.type.Type;
import audb.type.TypeUtil;
import audb.type.VarcharType;
import audb.table.Table;

import java.io.*;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {

        Parser parser = new Parser();

        // try {
        //     String s;
        //     BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        //     while ((s = in.readLine()) != null && s.length() != 0) {

        //         Command command = parser.getCommand(s);
        //         Result result  = command.exec();

        //         while(result.hasNext()) {
        //             Object[] cur = result.getNext();

        //             System.out.print("| ");
        //             for (int i = 0; i < cur.length; i++) {
        //                 Type curType = result.getColumns()[i];
        //                 if(curType.getType() == Type.STRING) {
        //                     String out = String.
        //                         format("%1$" + (curType.getSize() + ((String)cur[i]).length()) + "s", cur[i]);
        //                     out = out + " | ";
        //                     System.out.print(out);
        //                 }
        //             } 
        //             System.out.println();  
        //         }
        //     }
        // } catch(Exception e) {
        //      e.printStackTrace();
        // }

        try {

            Table table = new Table("table1");
            Type[] types = new Type[]{new VarcharType(3), new VarcharType(9)};
            String[] names = new String[]{"number", "text"};
            table.create(types, names);
            table.close();
            table = new Table("table1");
            table.init();

            for(int i = 0; i < 10; i++) {
                String s1 = String.format("%03d", i);;
                String s2 = "some_text";
                Object arr[] = new Object[]{s1, s2};
                table.addRecord(arr);
                
            }

            Result res = new FullScanResult(table);

            while(res.hasNext()) {
                Object[] arr = res.getNext();
                for(int i = 0; i < arr.length; i++) {
                    if(TypeUtil.isVarchar(table.getTypes()[i])) {
                        System.out.print(((String)arr[i]) + " ");
                    }
                }
                System.out.println();
            }

            res.close();
            table.close();

        } catch(Exception e) {
            System.out.println("Something goes wrong.");
            e.printStackTrace();
        }

    }

}
