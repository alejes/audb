package audb;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import audb.command.Command;
import audb.command.CreateTableCommand;
import audb.command.InsertCommand;
import audb.parser.Parser;
import audb.result.Result;
import audb.table.TableManager;
import audb.type.Type;
import audb.type.TypeUtil;
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

            for(int i = 0; i < 20; i++) {
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
                Result res = command.exec();

                while(res != null && res.hasNext()) {
                    Object[] arr = res.getNext();
                    for(int i = 0; i < arr.length; i++) {
                        if(TypeUtil.isVarchar(res.getTypes()[i])) {
                            System.out.print(((String)arr[i]) + " ");
                        }
                    }
                    System.out.println();
                }
            }
            tableManager.close();
        } catch(Exception e) {
             e.printStackTrace();
        }

    }

}
