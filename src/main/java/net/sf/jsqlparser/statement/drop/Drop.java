package net.sf.jsqlparser.statement.drop;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.List;

public class Drop implements Statement {
    private String type;
    private String name;
    private List parameters;

    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String string) {
        name = string;
    }

    public List getParameters() {
        return parameters;
    }

    public void setParameters(List list) {
        parameters = list;
    }

    public String getType() {
        return type;
    }

    public void setType(String string) {
        type = string;
    }

    public String toString() {
        String sql = "DROP " + type + " " + name;

        if (parameters != null && parameters.size() > 0) {
            sql += " " + PlainSelect.getStringList(parameters);
        }

        return sql;
    }
}
