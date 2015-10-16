package audb.command;

import audb.result.Result;


public class CreateTableCommand implements Command {

    public Result exec() {

        System.out.println("Fake CreateTableCommand returned fake CreateTableResult.");

        return new CreateTableResult();

    }

}
