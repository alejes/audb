package audb;

import audb.page.Page;
import audb.page.PageCache;
import audb.parser.Parser;
import audb.command.Command;
import audb.command.Result;

import java.io.*;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {

        Parser parser = new Parser();

        try {
            String s;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while ((s = in.readLine()) != null && s.length() != 0) {

                Command command = parser.getCommand(s);
                Result result  = command.exec();

                while(result.hasNext()) {
                    HashMap<String, String> cur = result.getNext();
                    System.out.print("| ");
                    for (String name: cur.keySet()) {
                        String out = name + ": " + cur.get(name) + " | ";
                        System.out.print(out);
                    } 
                    System.out.println();  
                }
            }
        } catch(Exception e) {
             e.printStackTrace();
        }

        // try {

        // 	// PageCache pc;
        // 	// Page p;
        // 	PageCache pc = new PageCache("db/tst");
        // 	Page p = pc.getPage(1);
        // 	for(byte i = 0; i < 100; i++)
        // 		p.data[i] = i;
        // 	p.write();
        // 	pc.close();

        // 	pc = new PageCache("db/tst");
        // 	p = pc.getPage(1);
        // 	for(byte i = 0; i < 100; i++)
        // 		System.out.println(p.data[i]);
        // 	pc.close();

        // } catch(Exception e) {
        //     System.out.println("Something goes wrong.");
        // }
    }

}
