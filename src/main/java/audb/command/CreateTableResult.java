package audb.command;

import java.util.HashMap;

public class CreateTableResult implements Result {

	private int num = 0;

    public HashMap<String, String> getNext() {

        
        HashMap<String, String> next = new HashMap<String, String>();
        next.put("val1", String.valueOf(num));
        next.put("val2", String.valueOf(9 - num));
        num += 1;

        return next;

    }

    public boolean hasNext() {

    	return num < 10;
    }

}
