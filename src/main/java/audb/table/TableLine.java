package audb.table;

import audb.type.Type;

import java.util.HashMap;


public class TableLine extends HashMap<String, TableElement> {

    private String tableName;
    private int pageNumber;
    private int offset;

    public TableLine(String name, int page, int off) {
        super();
        tableName = name;
        pageNumber = page;
        offset = off;
    }

    public String getTableName() {
        return tableName;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getOffset() {
        return offset;
    }
}
