package audb.result;

import audb.command.Constraint;
import audb.table.Table;
import audb.table.TableElement;
import audb.type.Type;
import audb.util.Pair;
import audb.util.Third;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class JoinIterator implements TableIterator {

    protected WeakReference<Table> table;


    protected HashMap<String, TableElement> next;
    private HashMap<String, TableElement> currentElement;
    private Iterator<HashMap<String, TableElement>> mainIterator;
    private Iterator<HashMap<String, TableElement>> secondIterator;
    private List<Pair<String, String>> columnNames;
    private String[] names;
    private Type[] types;
    private List<Third<String, Constraint, String>> constraints;


    public JoinIterator(TableIterator it, List<Pair<String, String>> columnNames, 
        List<Third<String, Constraint, String>> constraints, Table table) {

        this.table = new WeakReference<>(table);
        this.columnNames = columnNames;
        this.constraints = constraints;


        mainIterator = it;
        next = getNext();

        names = new String[it.getNames().length + table.getNames().length];
        types = new Type[it.getNames().length + table.getNames().length];
        int ind = 0;
        for (; ind < it.getNames().length; ++ind) {
            names[ind] = it.getNames()[ind];
            types[ind] = it.getTypes()[ind];
        }
        for (int i = 0; i < table.getNames().length; ++i) {
            names[ind] = table.getNames()[i];
            types[ind++] = table.getTypes()[i];
        }

    }

    public String[] getNames() {
        return names;
    }

    public Type[] getTypes() {
        return types;
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
                    new LinkedList<>(constraints);
            for (Pair<String, String> el: columnNames) {
                Constraint c = new Constraint(Constraint.ConstraintType.EQUAL, currentElement.get(el.first));
                ll.add(Third.newThird(el.second, c, ""));
            }
            secondIterator = new ConditionalTableIterator(table.get(), ll);
        }
        HashMap<String, TableElement> res = new HashMap<>(currentElement);
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
