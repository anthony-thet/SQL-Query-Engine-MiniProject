import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Database db = new Database("db", "schema.txt");

        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the database");
        while(true) {
            System.out.print("> ");
            String query = scanner.nextLine();
            if (query.equalsIgnoreCase("exit")) {
                break;
            }
            runQuery(query, db);
        }
        scanner.close();
        System.out.println("Goodbye!");
    }

    /**
     * Runs the given query on the database
     *
     * Implements the following algorithm
     *
     * Determine the type of query (from select, insert or delete)
     * If select query
     *   Select data
     *   Print results
     * Else if insert query
     *   Insert data
     * Else if delete is given
     *   Delete data
     *
     * @param query
     * @param db
     */
    public static void runQuery(String query, Database db) {
        if (query == null || query.isEmpty()) {
            return;
        }
        try {
            if (query.startsWith("SELECT")) {
                ITable table = db.selectData(query);
                IO.printTable(table, table.getSchema());
            }
            else if (query.startsWith("INSERT")) {
                db.insertData(query);
                System.out.println("Inserted Successfully");
            }
            else if (query.startsWith("DELETE")) {
                db.deleteData(query);
                System.out.println("Deleted Successfully");
            }
        }
        catch (InvalidQueryException e) {
            System.out.println(e.getMessage());
        }
    }
}