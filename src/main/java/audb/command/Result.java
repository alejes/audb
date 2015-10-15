package audb.command;

import audb.type.Type;

import java.util.HashMap;


public interface Result {

    public Object[] getNext();

    public boolean hasNext();

    public Type[] getColumns();

}
