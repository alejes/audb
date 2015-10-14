package audb.command;


public class CreateTableCommand implements Command {

    public Result exec() {

        System.out.println("Fake CreateTableCommand returned fake CreateTableResult.");

        return new CreateTableResult();

    }

}
