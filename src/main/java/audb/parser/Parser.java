package audb.parser;

import audb.command.*;
import audb.table.*;
import audb.type.DoubleType;
import audb.type.IntegerType;
import audb.type.Type;
import audb.type.VarcharType;
import audb.util.Pair;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;


public class Parser {
    private static CCJSqlParserManager parserManager = new CCJSqlParserManager();

    public Command selectParse(String str) throws Exception {
        final List exprList = new ArrayList();
        Select select = (Select) parserManager.parse(new StringReader(str));
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        Expression where = plainSelect.getWhere();
        Limit limits = plainSelect.getLimit();
        List<SelectItem> selectItems = plainSelect.getSelectItems();

        //Гениальный парсинг FROM
        String from = str.split("(?i)from")[1];
        from = from.trim();
        from = from.split(" ")[0];
        from = from.replace('`', ' ');
        from = from.trim();

        TableManager tableManager = Command.getTableManager();
        Table tableStruct = tableManager.getTable(from);
        if (tableStruct == null) {
            throw new IllegalArgumentException("unknown table");
        }

        //System.out.println("");
        System.out.print("Select please: ");
        for (SelectItem x :
                selectItems) {
            System.out.print(x.toString() + "|");
        }
        System.out.println();
        String[] tableNames = tableStruct.getNames();
        Type[] tableTypes = tableStruct.getTypes();


        //System.out.println("FROM TABLES: " + from);

        ArrayList<Pair<String, Constraint>> ConstraintsList = new ArrayList<Pair<String, Constraint>>();
        if (where != null) {
            String[] whereStatements = where.toString().split("(?i) AND ");
            for (String statement : whereStatements) {
                //System.out.println(statement);
                String[] splitConstraint = statement.split("[<=>]+", 2);
                if (splitConstraint.length != 2) {
                    throw new IllegalArgumentException("bad where statement " + statement);
                }
                String field = splitConstraint[0].replace('(', ' ').trim();
                String value = splitConstraint[1].trim();
                if (value.charAt(value.length() - 1) == ')') {
                    value = value.substring(0, value.length() - 1);
                }
                //System.out.print("[1]:");
                //System.out.println(field);
                //System.out.print("[2]:");
                //System.out.println(value);


                ArrayList<Object> args = new ArrayList<Object>();

                String beginWith = statement.substring(splitConstraint[0].length());
                Constraint.ConstraintType curent;
                if (beginWith.startsWith("<=")) {
                    curent = Constraint.ConstraintType.LESS_OR_EQUAL;
                } else if (beginWith.startsWith("<>")) {
                    curent = Constraint.ConstraintType.NOT_EQUAL;
                } else if (beginWith.startsWith("<")) {
                    curent = Constraint.ConstraintType.LESS;
                } else if (beginWith.startsWith("=")) {
                    curent = Constraint.ConstraintType.EQUAL;
                } else if (beginWith.startsWith(">=")) {
                    curent = Constraint.ConstraintType.GREATER_OR_EQUAL;
                } else if (beginWith.startsWith(">")) {
                    curent = Constraint.ConstraintType.GREATER;
                } else if (beginWith.startsWith("!=")) {
                    curent = Constraint.ConstraintType.NOT_EQUAL;
                } else {
                    throw new IllegalArgumentException("unsupported constraint in where statement" + statement);
                }
                //find type
                Type fieldType = null;
                for (int columnId = 0; columnId < tableNames.length; ++columnId) {
                    if (tableNames[columnId].compareTo(field) == 0) {
                        fieldType = tableTypes[columnId];
                    }
                }
                if (fieldType == null) {
                    throw new IllegalArgumentException("not found constraint in where" + statement);
                }

                TableElement el;
                switch (fieldType.getId()) {
                    case Type.INT:
                        try {
                            int val = Integer.parseInt(value);
                            el = new IntegerElement(val);
                        } catch (NumberFormatException nfe) {
                            throw new IllegalArgumentException("illegal int in where " + statement);
                        }
                    case Type.DOUBLE:
                        try {
                            double val = Double.parseDouble(value);
                            el = new DoubleElement(val);
                        } catch (NumberFormatException nfe) {
                            throw new IllegalArgumentException("illegal int in where " + statement);
                        }
                    default:
                        if (value.length() >= fieldType.getSize()) {
                            throw new IllegalArgumentException("very long VARCHAR in where " + statement);
                        }
                        el = new VarcharElement(value, new VarcharType((byte) value.length()));
                }
                ConstraintsList.add(Pair.newPair(field, new Constraint(curent, el)));
            }
        }
        System.out.println("CONSTRAINTS");
        for (Pair<String, Constraint> x : ConstraintsList) {
            System.out.print(x.first);
            System.out.print(" ");
            System.out.println(x.second);
        }
        //ConstraintType
        /*
        if (where == null)
            System.out.println("No Where");
        else {
            System.out.println("Where: " + where.toString());
        }
        */
        if (limits == null)
            System.out.println("No limits");
        else
            System.out.println("Limits: " + limits.toString());

        return new SelectCommand(from, ConstraintsList);
    }

    public Command insertParse(String str) throws Exception {
        Insert insert = (Insert) parserManager.parse(new StringReader(str));

        String table = insert.getTable().getName();

        TableManager tableManager = Command.getTableManager();
        Table tableStruct = tableManager.getTable(table);
        if (tableStruct == null) {
            throw new IllegalArgumentException("unknown table");
        }
        String[] tableNames = tableStruct.getNames();
        Type[] tableTypes = tableStruct.getTypes();

        ArrayList<Object> args = new ArrayList<Object>();

        //System.out.print("Tables:");
        //System.out.println(tableNames.length);
        for (int i = 0; i < tableNames.length; i++) {
            boolean find = false;
            for (int columnId = 0; columnId < tableNames.length; ++columnId) {

                if (((Column) insert.getColumns().get(columnId)).getColumnName().compareTo(tableNames[i]) == 0) {
                    String insertValue = ((StringValue) ((ExpressionList) insert.getItemsList()).getExpressions().get(columnId)).getValue();
                    //System.out.println(insertValue);
                    switch (tableTypes[i].getId()) {
                        case Type.INT:
                            throw new IllegalArgumentException("Integer insert not supported by parser");
                        case Type.DOUBLE:
                            throw new IllegalArgumentException("Double insert not supported by parser");
                        default:
                            //Varchar
                            if (tableTypes[i].getSize() < insertValue.length()) {
                                throw new IllegalArgumentException("To long argument for field");
                            } else {
                                args.add(insertValue);
                                find = true;
                                break;
                            }
                    }
                }
            }
            if (!find) {
                throw new IllegalArgumentException("unknown row name");
            }
        }
        return new InsertCommand(table, args.toArray());
    }

    public Command createTable(String str) throws Exception {
        System.out.println(str);
        CreateTable createTable = (CreateTable) parserManager.parse(new StringReader(str));
        int columsCount = createTable.getColumnDefinitions().size();
        String table = createTable.getTable().toString();

        Type[] typesColumn = new Type[columsCount];
        String[] nameColumn = new String[columsCount];

        for (int i = 0; i < columsCount; ++i) {
            ColumnDefinition column = (ColumnDefinition) createTable.getColumnDefinitions().get(i);
            nameColumn[i] = (column.getColumnName());
            String currentType = column.getColDataType().toString().toLowerCase();
            if (currentType.compareTo("int") == 0) {
                typesColumn[i] = (new IntegerType(Type.INT));
            } else if (currentType.compareTo("double") == 0) {
                typesColumn[i] = (new DoubleType(Type.DOUBLE));
            } else {
                String[] lenArr = currentType.split("([()])");
                if (lenArr.length != 2) {
                    throw new IllegalArgumentException("error in VARCHAR len");
                }
                int val;
                try {
                    val = Integer.parseInt(lenArr[1]);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("illegal int in VARCHAR len");
                }
                byte bytesBuf = (byte) val;
                typesColumn[i] = (new VarcharType(bytesBuf));
            }
        }

        return new CreateTableCommand(table, typesColumn, nameColumn);
    }

    public Command createManager(String str) throws Exception {
        String cmd = str.substring(Math.min(str.length(), 7), Math.min(str.length(), 12)).toLowerCase();
        if (cmd.compareTo("table") == 0) {
            return createTable(str);
        } else {
            throw new Exception("Unsupported action");
        }
    }
    public Command getCommand(String str) throws Exception {
        //str = "SELECT `id`, `password` FROM `table1` WHERE (`id` = '3' and `hyj`='dz')";
        //str = "INSERT INTO table1 (number, text) VALUES ('33', 'sadfsd')";
        //System.out.println("Parser had \"" + str + "\" on input and ignored it.");
        //System.out.println("FullScan for table1 returned instead.");

        String cmd = str.substring(0, Math.min(str.length(), 6)).toLowerCase();

        //if (cmd.compareTo("insert") != 0) {
            //str = "select * from table1 where (id > 4) and (id < 5)";
            //cmd = "select";
        //}

        if (cmd.compareTo("select") == 0) {
            return selectParse(str);
        } else if (cmd.compareTo("insert") == 0) {
            return insertParse(str);
        } else if (cmd.compareTo("create") == 0) {
            return createManager(str);
        } else throw new Exception("Unsupported action");
    }

}