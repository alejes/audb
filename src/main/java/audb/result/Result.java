package audb.result;

import audb.type.Type;

import java.util.HashMap;


public interface Result {

    public Object[] getNext();

    public boolean hasNext();

    public Type[] getTypes();

    public void close();

}
