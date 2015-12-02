package net.sf.jsqlparser.statement.create.table;

import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.List;

public class ColDataType {

    private String dataType;
    private List<String> argumentsStringList;

    public List<String> getArgumentsStringList() {
        return argumentsStringList;
    }

    public void setArgumentsStringList(List<String> list) {
        argumentsStringList = list;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String string) {
        dataType = string;
    }

    public String toString() {
        return dataType + (argumentsStringList != null ? " " + PlainSelect.getStringList(argumentsStringList, true, true) : "");
    }
}