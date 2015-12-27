package audb.table;

import audb.type.Type;

import java.util.HashMap;


public class TableLine extends HashMap<String, TableElement> {

    private String tableName;
    private int pageNumber;
    private int offset;
    private boolean deleted;

    public TableLine(String name, int page, int off) {
        super();
        tableName = name;
        pageNumber = page;
        offset = off;
        deleted = false;
    }

    public String getTableName() {
        return tableName;
    }

    public void setDeleted() {
        deleted = true;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getOffset() {
        return offset;
    }
}
