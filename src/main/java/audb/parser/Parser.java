package audb.parser;

import audb.command.*;
import audb.index.Index;
import audb.table.*;
import audb.type.DoubleType;
import audb.type.IntegerType;
import audb.type.Type;
import audb.type.VarcharType;
import audb.util.Third;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.update.Update;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class Parser {
    public static HashSet<String> selectList;
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
            throw new IllegalArgumentException("unknown table " + from);
        }

        //System.out.println("");
        selectList = new HashSet<>();
        System.out.print("Select please: ");
        for (SelectItem x :
                selectItems) {
            if (x.toString().compareTo("*") == 0) {
                selectList.clear();
                break;
            }
            selectList.add(x.toString());
            //System.out.print(x.toString() + "|");
        }
        System.out.println();
        String[] tableNames = tableStruct.getNames();
        Type[] tableTypes = tableStruct.getTypes();

        ArrayList<Third<String, Constraint, String>> ConstraintsList = new ArrayList<>();
        if (where != null) {
            String[] whereStatements = where.toString().split("(?i) AND ");
            for (String statement : whereStatements) {
                String[] splitConstraint = statement.split("[<=>]+", 2);
                if (splitConstraint.length != 2) {
                    throw new IllegalArgumentException("bad where statement " + statement);
                }
                String fullFieldName = splitConstraint[0].replace('(', ' ').trim();
                String[] fieldList = fullFieldName.split("\\.");
                String fieldName, fieldTable;
                System.out.println(fullFieldName + "{}{}" + fieldList.length);
                if (fieldList.length == 1) {
                    fieldName = fieldList[0];
                    fieldTable = from;
                } else if (fieldList.length == 2) {
                    fieldTable = fieldList[0];
                    fieldName = fieldList[1];
                } else {
                    throw new IllegalArgumentException("unknown table in where " + fullFieldName);
                }
                System.out.println("! " + fieldName + "{}{}" + fieldTable);
                String value = splitConstraint[1].trim();
                if (value.charAt(value.length() - 1) == ')') {
                    value = value.substring(0, value.length() - 1);
                }


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
                System.out.println("FT" + fieldTable);
                Table currentWhereTable = tableManager.getTable(fieldTable);
                String[] currentWhereTableNames = currentWhereTable.getNames();
                Type[] currentWhereTableTypes = currentWhereTable.getTypes();
                for (int columnId = 0; columnId < currentWhereTableNames.length; ++columnId) {
                    if (currentWhereTableNames[columnId].compareTo(fieldName) == 0) {
                        fieldType = currentWhereTableTypes[columnId];
                    }
                }
                if (fieldType == null) {
                    throw new IllegalArgumentException("not found constraint " + fieldName + " in where" + statement);
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
                            throw new IllegalArgumentException("illegal double in where " + statement);
                        }
                    default:
                        if (value.length() >= fieldType.getSize()) {
                            throw new IllegalArgumentException("very long VARCHAR in where " + statement);
                        }
                        el = new VarcharElement(value, new VarcharType((byte) fieldType.getSize()));
                }
                ConstraintsList.add(Third.newThird(fieldName, new Constraint(curent, el), fieldTable));
            }
        }
        System.out.println("CONSTRAINTS");
        for (Third<String, Constraint, String> x : ConstraintsList) {
            System.out.print(x.first);
            System.out.print(" ");
            System.out.print(x.second);
            System.out.print(" ");
            System.out.println(x.third);
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

        for (int i = 0; i < tableNames.length; i++) {
            boolean find = false;
            for (int columnId = 0; columnId < tableNames.length; ++columnId) {

                if (((Column) insert.getColumns().get(columnId)).getColumnName().compareTo(tableNames[i]) == 0) {
                    switch (tableTypes[i].getId()) {
                        case Type.INT:
                            try {
                                int val = (int) ((LongValue) ((ExpressionList) insert.getItemsList()).getExpressions().get(columnId)).getValue();
                                args.add(val);
                            } catch (NumberFormatException nfe) {
                                throw new IllegalArgumentException("illegal int in where ");
                            }
                            find = true;
                            break;
                        case Type.DOUBLE:
                            try {

                                double val = ((DoubleValue) ((ExpressionList) insert.getItemsList()).getExpressions().get(columnId)).getValue();
                                args.add(val);
                            } catch (NumberFormatException nfe) {
                                throw new IllegalArgumentException("illegal double in where ");
                            }
                            find = true;
                            break;
                        default:
                            //Varchar
                            String insertValue = ((StringValue) ((ExpressionList) insert.getItemsList()).getExpressions().get(columnId)).getValue();
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
                throw new IllegalArgumentException("unknown row name" + tableNames[i]);
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

    public Command createConstraint(String str) throws Exception {
        str = str.substring(7).trim();
        Boolean unique = false;
        if (str.toLowerCase().startsWith("unique")) {
            unique = true;
            str = str.substring(7).trim();
        }


        if (!str.toLowerCase().startsWith("index")) {
            throw new Exception("Unsupported action in create query");
        }
        str = str.substring(6).trim();
        String[] indexSplit = str.split(" ", 2);
        if (indexSplit.length != 2) {
            throw new Exception("Wrong indexname in create query");
        }

        String indexName = indexSplit[0].trim();
        str = indexSplit[1].trim();


        if (!str.toLowerCase().startsWith("on ")) {
            throw new Exception("Unsupported ON in create query");
        }
        str = str.substring(3).trim();

        int tableNameIndex = str.indexOf('(');
        String tableName = str.substring(0, tableNameIndex).trim();
        str = str.substring(tableNameIndex + 1);


        TableManager tableManager = Command.getTableManager();
        Table tableStruct = tableManager.getTable(tableName);
        if (tableStruct == null) {
            throw new IllegalArgumentException("unknown table");
        }
        String[] tableNamesList = tableStruct.getNames();


        int tableNameIndexEnd = str.indexOf(')');

        String[] columsList = str.substring(0, tableNameIndexEnd).trim().split(",");
        str = str.substring(tableNameIndexEnd + 1).trim();
        Index.Order[] orders = new Index.Order[columsList.length];
        String[] indexColumnNames = new String[columsList.length];
        for (int i = 0; i < columsList.length; i++) {
            String[] items = columsList[i].trim().split(" ");
            if (items.length != 2) {
                throw new Exception("Wrong column constraint in create query");
            }
            String currentColumn = items[0].trim().toLowerCase();
            boolean find = false;

            for (String __x : tableNamesList) {
                if (currentColumn.compareTo(__x) == 0) {
                    find = true;
                    break;
                }
            }
            if (!find) {
                throw new IllegalArgumentException("Not find column " + currentColumn);
            }
            indexColumnNames[i] = currentColumn;
            if (items[1].trim().toLowerCase().compareTo("ASC") == 0) {
                orders[i] = Index.Order.ASC;
            } else {
                orders[i] = Index.Order.DESC;
            }
            //System.out.println(items[0].trim() + "|" + items[1].trim());

        }


        if (!str.toLowerCase().startsWith("using ")) {
            throw new Exception("Not enter index type in create index query");
        }

        str = str.substring(6).trim();

        if (!str.toLowerCase().startsWith("btree")) {
            throw new Exception("Support only BTREE index");
        }


        tableStruct.addBTreeIndex(indexColumnNames, orders);

        return new EmptyCommand(tableName);
    }
    public Command createManager(String str) throws Exception {
        String cmd = str.substring(Math.min(str.length(), 7), Math.min(str.length(), 12)).toLowerCase();
        if (cmd.compareTo("table") == 0) {
            return createTable(str);
        } else {
            return createConstraint(str);
        }
    }

    public Command deleteParser(String str) throws Exception {
        Delete delete = (Delete) parserManager.parse(new StringReader(str));
        String from = delete.getTable().getName();

        TableManager tableManager = Command.getTableManager();
        Table tableStruct = tableManager.getTable(from);
        if (tableStruct == null) {
            throw new IllegalArgumentException("unknown table " + from);
        }

        Expression where = delete.getWhere();
        String[] tableNames = tableStruct.getNames();
        Type[] tableTypes = tableStruct.getTypes();

        ArrayList<Third<String, Constraint, String>> ConstraintsList = new ArrayList<>();
        if (where != null) {
            String[] whereStatements = where.toString().split("(?i) AND ");
            for (String statement : whereStatements) {
                String[] splitConstraint = statement.split("[<=>]+", 2);
                if (splitConstraint.length != 2) {
                    throw new IllegalArgumentException("bad where statement " + statement);
                }
                String fullFieldName = splitConstraint[0].replace('(', ' ').trim();
                String[] fieldList = fullFieldName.split("\\.");
                String fieldName, fieldTable;
                System.out.println(fullFieldName + "{}{}" + fieldList.length);
                if (fieldList.length == 1) {
                    fieldName = fieldList[0];
                    fieldTable = from;
                } else if (fieldList.length == 2) {
                    fieldTable = fieldList[0];
                    fieldName = fieldList[1];
                } else {
                    throw new IllegalArgumentException("unknown table in where " + fullFieldName);
                }
                System.out.println("! " + fieldName + "{}{}" + fieldTable);
                String value = splitConstraint[1].trim();
                if (value.charAt(value.length() - 1) == ')') {
                    value = value.substring(0, value.length() - 1);
                }


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
                System.out.println("FT" + fieldTable);
                Table currentWhereTable = tableManager.getTable(fieldTable);
                String[] currentWhereTableNames = currentWhereTable.getNames();
                Type[] currentWhereTableTypes = currentWhereTable.getTypes();
                for (int columnId = 0; columnId < currentWhereTableNames.length; ++columnId) {
                    if (currentWhereTableNames[columnId].compareTo(fieldName) == 0) {
                        fieldType = currentWhereTableTypes[columnId];
                    }
                }
                if (fieldType == null) {
                    throw new IllegalArgumentException("not found constraint " + fieldName + " in where" + statement);
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
                            throw new IllegalArgumentException("illegal double in where " + statement);
                        }
                    default:
                        if (value.length() >= fieldType.getSize()) {
                            throw new IllegalArgumentException("very long VARCHAR in where " + statement);
                        }
                        el = new VarcharElement(value, new VarcharType((byte) fieldType.getSize()));
                }
                ConstraintsList.add(Third.newThird(fieldName, new Constraint(curent, el), fieldTable));
            }
        }
        System.out.println("CONSTRAINTS");
        for (Third<String, Constraint, String> x : ConstraintsList) {
            System.out.print(x.first);
            System.out.print(" ");
            System.out.print(x.second);
            System.out.print(" ");
            System.out.println(x.third);
        }
        //throw new IllegalArgumentException("delete parser unimplemented");
        SelectCommand select = new SelectCommand(from, ConstraintsList);
        return new DeleteCommand(select.exec().second);
    }

    public Command updateParser(String str) throws Exception {
        System.out.println(str);
        Update update = (Update) parserManager.parse(new StringReader(str));

        String from = update.getTable().getName();

        TableManager tableManager = Command.getTableManager();
        Table tableStruct = tableManager.getTable(from);
        if (tableStruct == null) {
            throw new IllegalArgumentException("unknown table " + from);
        }

        Expression where = update.getWhere();
        String[] tableNames = tableStruct.getNames();
        Type[] tableTypes = tableStruct.getTypes();

        HashMap<String, Object> nwValuesList = new HashMap<>();

        int countColums = update.getColumns().size();

        for (int i = 0; i < countColums; ++i) {
            Column column = ((Column) update.getColumns().get(i));
            Boolean find = false;
            String currentColumnName = column.getColumnName();

            Type fieldType = null;
            for (int columnId = 0; columnId < tableNames.length; ++columnId) {
                if (tableNames[columnId].compareTo(currentColumnName) == 0) {
                    fieldType = tableTypes[columnId];
                    break;
                }
            }


            if (fieldType == null) {
                throw new Exception("Not found column " + currentColumnName);
            }

            String value = ((StringValue) update.getExpressions().get(i)).getValue();
            switch (fieldType.getId()) {
                case Type.INT:
                    try {
                        int val = Integer.parseInt(value);
                        nwValuesList.put(currentColumnName, val);
                    } catch (NumberFormatException nfe) {
                        throw new IllegalArgumentException("illegal int in expession list " + value);
                    }
                case Type.DOUBLE:
                    try {
                        double val = Double.parseDouble(value);
                        nwValuesList.put(currentColumnName, val);
                    } catch (NumberFormatException nfe) {
                        throw new IllegalArgumentException("illegal int in expession list " + value);
                    }
                default:
                    if (value.length() >= fieldType.getSize()) {
                        throw new IllegalArgumentException("very long VARCHAR in expession list  " + value);
                    }
                    nwValuesList.put(currentColumnName, value);
            }


        }

        ArrayList<Third<String, Constraint, String>> ConstraintsList = new ArrayList<>();
        if (where != null) {
            String[] whereStatements = where.toString().split("(?i) AND ");
            for (String statement : whereStatements) {
                String[] splitConstraint = statement.split("[<=>]+", 2);
                if (splitConstraint.length != 2) {
                    throw new IllegalArgumentException("bad where statement " + statement);
                }
                String fullFieldName = splitConstraint[0].replace('(', ' ').trim();
                String[] fieldList = fullFieldName.split("\\.");
                String fieldName, fieldTable;
                System.out.println(fullFieldName + "{}{}" + fieldList.length);
                if (fieldList.length == 1) {
                    fieldName = fieldList[0];
                    fieldTable = from;
                } else if (fieldList.length == 2) {
                    fieldTable = fieldList[0];
                    fieldName = fieldList[1];
                } else {
                    throw new IllegalArgumentException("unknown table in where " + fullFieldName);
                }
                System.out.println("! " + fieldName + "{}{}" + fieldTable);
                String value = splitConstraint[1].trim();
                if (value.charAt(value.length() - 1) == ')') {
                    value = value.substring(0, value.length() - 1);
                }


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
                System.out.println("FT" + fieldTable);
                Table currentWhereTable = tableManager.getTable(fieldTable);
                String[] currentWhereTableNames = currentWhereTable.getNames();
                Type[] currentWhereTableTypes = currentWhereTable.getTypes();
                for (int columnId = 0; columnId < currentWhereTableNames.length; ++columnId) {
                    if (currentWhereTableNames[columnId].compareTo(fieldName) == 0) {
                        fieldType = currentWhereTableTypes[columnId];
                    }
                }
                if (fieldType == null) {
                    throw new IllegalArgumentException("not found constraint " + fieldName + " in where" + statement);
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
                            throw new IllegalArgumentException("illegal double in where " + statement);
                        }
                    default:
                        if (value.length() >= fieldType.getSize()) {
                            throw new IllegalArgumentException("very long VARCHAR in where " + statement);
                        }
                        el = new VarcharElement(value, new VarcharType((byte) fieldType.getSize()));
                }
                ConstraintsList.add(Third.newThird(fieldName, new Constraint(curent, el), fieldTable));
            }
        }
        System.out.println("CONSTRAINTS");
        for (Third<String, Constraint, String> x : ConstraintsList) {
            System.out.print(x.first);
            System.out.print(" ");
            System.out.print(x.second);
            System.out.print(" ");
            System.out.println(x.third);
        }

        SelectCommand select = new SelectCommand(from, ConstraintsList);
        //return new DeleteCommand(select.exec().second);
        return new UpdateCommand(select.exec().second, nwValuesList);
        //throw new IllegalArgumentException("unimplmented");
    }

    public Command getCommand(String str) throws Exception {
        //str = "SELECT `id`, `password` FROM `table1` WHERE (`id` = '3' and `hyj`='dz')";
        //str = "INSERT INTO table1 (number, text) VALUES ('33', 'sadfsd')";
        //System.out.println("Parser had \"" + str + "\" on input and ignored it.");
        //System.out.println("FullScan for table1 returned instead.");

        String cmd = str.substring(0, Math.min(str.length(), 6)).toLowerCase();


        if (cmd.compareTo("insert") != 0) {
            //str = "UPDATE table1 set col1='as', col2=?, col3=565 Where number >= 3";
            //cmd = "update";
        //str = "CREATE[UNIQUE] INDEX indexname ON tablename(col [ASC|DESC],[col]...) USING BTREE|HASH;"
        //str = "CREATE UNIQUE INDEX indexname ON table1(number DESC, text ASC) USING BTREE;";
            //str = "select * from table1 where (id > 4) and (id < 5)";
            //cmd = "select";
        }

        if (cmd.compareTo("select") == 0) {
            return selectParse(str);
        } else if (cmd.compareTo("insert") == 0) {
            return insertParse(str);
        } else if (cmd.compareTo("create") == 0) {
            return createManager(str);
        } else if (cmd.compareTo("delete") == 0) {
            return deleteParser(str);
        } else if (cmd.compareTo("update") == 0) {
            return updateParser(str);
        } else throw new Exception("Unsupported action");
    }

}