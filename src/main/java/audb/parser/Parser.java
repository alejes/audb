package audb.parser;

import audb.command.Command;
import audb.command.Constraint;
import audb.command.SelectCommand;
import audb.table.Table;
import audb.table.TableManager;
import audb.util.Pair;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;


public class Parser {
    public Command selectParse(String str) throws Exception {
        //System.out.println(str);
        //      String[] from = str.split("(?i)from");
//        if (from.length != 2){
//            throw new Exception("Unsupported count of word from");
//        }
//        System.out.println("parse starts");
//        String statement = "SELECT COUNT(*) FROM db.table1";
//        CCJSqlParserManager parserManager = new CCJSqlParserManager();
//        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
//        System.out.format("%s is function call? %s",
        //              plainSelect.getSelectItems().get(0),
//                ((Function)((SelectExpressionItem) plainSelect.getSelectItems().get(0)).getExpression()).isAllColumns());
//        System.out.println("parse starts");
        //System.out.println(from[0]);
        //System.out.println(from[1]);
        //

        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        final List exprList = new ArrayList();
        Select select = (Select) parserManager.parse(new StringReader(str));
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        Expression where = plainSelect.getWhere();
        Limit limits = plainSelect.getLimit();
        List<SelectItem> selectItems = plainSelect.getSelectItems();
        //TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        //List<String> tables = tablesNamesFinder.getTableList(select);

        //Гениальный парсинг FROM
        String from = str.split("(?i)from")[1];
        from = from.trim();
        from = from.split(" ")[0];
        from = from.replace('`', ' ');
        from = from.trim();


        System.out.println("");
        System.out.print("Select please: ");
        for (SelectItem x :
                selectItems) {
            System.out.print(x.toString() + "|");
        }
        System.out.println();

        System.out.println("FROM TABLES: " + from);


        if (where == null)
            System.out.println("No Where");
        else
            System.out.println("Where: " + where.toString());
        if (limits == null)
            System.out.println("No limits");
        else
            System.out.println("Limits: " + limits.toString());

        return new SelectCommand(from, new ArrayList<Pair<String, Constraint>>());
    }

    public Command insertParse(String str) throws Exception {
        System.out.println(str);
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        Insert insert = (Insert) parserManager.parse(new StringReader(str));

        String table = insert.getTable().getName();

        TableManager tableManager = Command.getTableManager();
        Table tableStruct = tableManager.getTable("table1");
        String[] tableNames = tableStruct.getNames();

        System.out.println(tableStruct.getNames().length);
        for (int i = 0; i < tableNames.length; ++i) {
            System.out.println(tableNames[i]);
        }
        //System.out.println(tableStruct.getNames());


        System.out.print("Table:");
        System.out.println(table);

        return new SelectCommand(table, new ArrayList<Pair<String, Constraint>>());
    }
    public Command getCommand(String str) throws Exception {
        //str = "SELECT `id`, `password` FROM `table1` WHERE (`id` = '3' and `hyj`='dz')";
        str = "INSERT INTO table1 (col1, col2, col3) VALUES (?, 'sadfsd', 234)";
        System.out.println("Parser had \"" + str + "\" on input and ignored it.");
        System.out.println("FullScan for table1 returned instead.");

        String cmd = str.substring(0, Math.min(str.length(), 6)).toLowerCase();

        if (cmd.compareTo("select") == 0) {
            return selectParse(str);
            //return selectParse(str.substring(Math.min(str.length(), 6)));
        } else if (cmd.compareTo("insert") == 0) {
            return insertParse(str);
        } else throw new Exception("Unsupported action");
    }

}