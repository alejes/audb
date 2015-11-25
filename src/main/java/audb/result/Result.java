package audb.result;

import audb.type.Type;


public interface Result {

    public Object[] getNext();

    public boolean hasNext();

    public Type[] getTypes();

    public void close();

}
