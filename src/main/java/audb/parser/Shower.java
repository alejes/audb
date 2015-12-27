package audb.parser;

import audb.command.Command;
import audb.result.TableIterator;
import audb.table.Table;
import audb.table.TableElement;
import audb.table.TableLine;
import audb.type.Type;
import audb.util.Pair;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Alexey on 27.12.2015.
 */
public class Shower {
    private static Parser parser = new Parser();
    private static Command command;

    private static void show_with_except(String s) throws Exception {
        command = parser.getCommand(s);
        Pair<Table, Iterator<HashMap<String, TableElement>>> exRes = command.exec();
        if (null == exRes) {
            if (Parser.affectedRows >= 0) {
                System.out.println("OK " + Parser.affectedRows + " rows affected");
            } else {
                System.out.println("OK");
            }
            return;
        }

        TableIterator res = (TableIterator) exRes.second;

        if (null == res) {
            return;
        }
        String[] names1 = res.getNames();
        Type[] types1 = res.getTypes();
        for (int i = 0, names1Length = names1.length; i < names1Length; i++) {
            String fieldName = names1[i];
            Type fieldType = types1[i];
            if ((Parser.selectList == null) || Parser.selectList.isEmpty()) {
                System.out.print(String.format("%30s", fieldName + "  " + fieldType + "  |"));
            }
        }
        System.out.println();


        while (res.hasNext()) {
            HashMap<String, TableElement> arr = res.next();

            if (!((TableLine) arr).isDeleted()) {
                for (String name : arr.keySet()) {
                    if ((Parser.selectList == null) || Parser.selectList.isEmpty() || Parser.selectList.contains(name)) {
                        System.out.print(String.format("%30s", arr.get(name).showString() + " |"));
                    }
                }
                System.out.println();
            }
        }
    }

    public static void show(String s) {
        try {
            show_with_except(s);
        } catch (Exception exp) {
            System.out.println("Error: " + exp.getMessage() + exp.toString());
            for (StackTraceElement stackTraceElement : exp.getStackTrace()) {
                System.out.println(stackTraceElement);
            }
        }
    }

    public static void show_exsept(String s) throws Exception {
        show_with_except(s);
    }
}
