package audb.command;

import java.util.HashMap;


public interface Result {

    public HashMap<String, String> getNext();

    public boolean hasNext();

}
