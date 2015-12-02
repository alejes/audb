package net.sf.jsqlparser.statement.create.table;

import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.List;

/**
 * An index (unique, primary etc.) in a CREATE TABLE statement
 */
public class Index {

    private String type;
    private List<String> columnsNames;
    private String name;

    /**
     * A list of strings of all the columns regarding this index
     *
     * @return The String name of all columns indexed by this index.
     */
    public List<String> getColumnsNames() {
        return columnsNames;
    }

    public void setColumnsNames(List<String> list) {
        columnsNames = list;
    }

    public String getName() {
        return name;
    }

    public void setName(String string) {
        name = string;
    }

    /**
     * The type of this index: "PRIMARY KEY", "UNIQUE", "INDEX"
     *
     * @return the String type of this index.
     */
    public String getType() {
        return type;
    }

    public void setType(String string) {
        type = string;
    }

    public String toString() {
        return type + " " + PlainSelect.getStringList(columnsNames, true, true) + (name != null ? " " + name : "");
    }
}