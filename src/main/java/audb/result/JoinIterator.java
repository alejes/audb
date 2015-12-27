package audb.result;

import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.lang.ref.WeakReference;

import audb.command.Constraint;
import audb.page.Page;
import audb.page.PageStructure;
import audb.table.PageIterator;
import audb.table.Table;
import audb.table.TableElement;
import audb.type.Type;
import audb.util.*;

public class JoinIterator implements TableIterator {

    protected WeakReference<Table> table;


    protected HashMap<String, TableElement> next;
    private HashMap<String, TableElement> currentElement;
    private Iterator<HashMap<String, TableElement>> mainIterator;
    private Iterator<HashMap<String, TableElement>> secondIterator;
    private List<Pair<String, String>> columnNames;
    private String[] names;
    private List<Third<String, Constraint, String>> constraints;


    public JoinIterator(TableIterator it, List<Pair<String, String>> columnNames, 
        List<Third<String, Constraint, String>> constraints, Table table) {

        this.table = new WeakReference<Table>(table);
        this.columnNames = columnNames;
        this.constraints = constraints;


        mainIterator = it;
        next = getNext();

        names = new String[it.getNames().length + table.getNames().length];
        int ind = 0;
        for (; ind < it.getNames().length; ++ind) {
            names[ind] = it.getNames()[ind];
        }
        for (int i = 0; i < table.getNames().length; ++i) {
            names[ind++] = table.getNames()[i];
        }

    }

    public String[] getNames() {
        return names;
    }

    public void close() {
        // mainIterator.close();
        // secondIterator.close();
    }

    private HashMap<String, TableElement> getNext() {

        while (secondIterator == null || !secondIterator.hasNext()) {
            if (!mainIterator.hasNext())
                return null;
            currentElement = mainIterator.next();
            LinkedList<Third<String, Constraint, String>> ll = 
                new LinkedList<Third<String, Constraint, String>>(constraints);
            for (Pair<String, String> el: columnNames) {
                Constraint c = new Constraint(Constraint.ConstraintType.EQUAL, currentElement.get(el.first));
                ll.add(Third.newThird(el.second, c, ""));
            }
            secondIterator = new ConditionalTableIterator(table.get(), ll);
        }
        HashMap<String, TableElement> res = new HashMap<String, TableElement>(currentElement);
        res.putAll(secondIterator.next());
        return res;
    }
    
    public HashMap<String, TableElement> next() {
        HashMap<String, TableElement> tmp = next;
        next = getNext();
        return tmp;
    }

    public boolean hasNext() {
        return next != null;
    }
}
