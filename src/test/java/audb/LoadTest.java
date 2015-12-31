// package audb;

// import audb.command.Command;
// import audb.page.PageStructure;
// import audb.parser.Parser;
// import audb.table.TableManager;
// import junit.framework.Test;
// import junit.framework.TestCase;
// import junit.framework.TestSuite;

// import java.util.Random;

// public class LoadTest extends TestCase {
//     final Random random = new Random();

//     public LoadTest(String testName) {
//         super(testName);
//     }

//     public static Test suite() {
//         return new TestSuite(LoadTest.class);
//     }

//     public void testDummy() {
//         assertTrue(true);
//     }

//     public void testInsertWithoutIndex() throws Exception {
//         assertTrue(true);
//         Parser parser = new Parser();
//         TableManager tableManager = new TableManager();
//         Command.setTableManager(tableManager);


//         parser.getCommand("CREATE TABLE tableload4m (number VARCHAR (15), text VARCHAR (9))").exec();


//         for (int i = 0; i < 100_00; i++) {
//             parser.getCommand(String.format("INSERT INTO tableload4m (number, text) VALUES ('%09d', 'sadfsd')", i)).exec();
//             if (i % 10000 == 25) {
//                 System.out.println("Insert " + i + " items");
//             }
//         }

//         PageStructure.flush();
//     }

//     public void testInsertWithPreIndex() throws Exception {
//         assertTrue(true);
//         Parser parser = new Parser();
//         TableManager tableManager = new TableManager();
//         Command.setTableManager(tableManager);

//         parser.getCommand("CREATE TABLE tableload4mpreindex (number VARCHAR (15), text VARCHAR (9))").exec();
//         parser.getCommand("CREATE UNIQUE INDEX indexname ON tableload4mpreindex (number DESC) USING BTREE;").exec();

//         for (int i = 0; i < 100_00; i++) {
//             parser.getCommand(String.format("INSERT INTO tableload4mpreindex (number, text) VALUES ('%09d', 'sadfsd')", i)).exec();
//             if (i % 10000 == 25) {
//                 System.out.println("Insert " + i + " items");
//             }
//         }

//         PageStructure.flush();
//     }

//     public void testInsertWithPostIndex() throws Exception {
//         assertTrue(true);
//         Parser parser = new Parser();
//         TableManager tableManager = new TableManager();
//         Command.setTableManager(tableManager);

//         parser.getCommand("CREATE TABLE tableload4mpostindex (number VARCHAR (15), text VARCHAR (9))").exec();

//         for (int i = 0; i < 100_00; i++) {
//             parser.getCommand(String.format("INSERT INTO tableload4mpostindex (number, text) VALUES ('%09d', 'sadfsd')", i)).exec();
//             if (i % 10000 == 25) {
//                 System.out.println("Insert " + i + " items");
//             }
//         }
//         parser.getCommand("CREATE UNIQUE INDEX indexname ON tableload4mpostindex (number DESC) USING BTREE;").exec();

//         PageStructure.flush();
//     }

//     public void testInsertWithoutIndexSpeedSelect() throws Exception {
//         assertTrue(true);
//         Parser parser = new Parser();
//         TableManager tableManager = new TableManager();
//         Command.setTableManager(tableManager);

//         for (int i = 0; i < 100_00; i++) {
//             parser.getCommand(String.format("SELECT * FROM tableload4m WHERE number = '%09d'", random.nextInt(120_000_0))).exec();
//         }

//         PageStructure.flush();
//     }

//     public void testInsertWithPreIndexSpeedSelect() throws Exception {
//         assertTrue(true);
//         Parser parser = new Parser();
//         TableManager tableManager = new TableManager();
//         Command.setTableManager(tableManager);

//         for (int i = 0; i < 100_00; i++) {
//             parser.getCommand(String.format("SELECT * FROM tableload4mpreindex WHERE number = '%09d'", random.nextInt(120_000_0))).exec();
//         }

//         PageStructure.flush();
//     }

//     public void testInsertWithPostIndexSpeedSelect() throws Exception {
//         assertTrue(true);
//         Parser parser = new Parser();
//         TableManager tableManager = new TableManager();
//         Command.setTableManager(tableManager);

//         for (int i = 0; i < 100_00; i++) {
//             parser.getCommand(String.format("SELECT * FROM tableload4mpostindex WHERE number = '%09d'", random.nextInt(120_000_0))).exec();
//         }

//         PageStructure.flush();
//     }

// }
