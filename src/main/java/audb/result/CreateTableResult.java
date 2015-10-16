package audb.result;

import audb.type.Type;
import audb.type.StringType;

import java.util.HashMap;


public class CreateTableResult implements Result {

	private int num;
    private Type[] params;

    public CreateTableResult() {
        num = 0;
        params = new Type[2];
        params[0] = new StringType();
        params[1] = params[0];
    }

    public Object[] getNext() {

        
        Object[] next = new Object[2];
        next[0] = "val" + num;
        next[1] = "val" + (9 - num);
        num += 1;

        return next;

    }

    public boolean hasNext() {

    	return num < 10;
    }

    public Type[] getTypes() {

        return params;
    }

    public close() {

    }

}
