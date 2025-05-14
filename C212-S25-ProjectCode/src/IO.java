import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * this is the IO utility class
 */
public class IO {

    /**
     * Reads the table's data from a csv file
     *
     * Implement the following algorithm
     *
     * Open the csv file from the folder (corresponding to the tablename)
     *   For each line in the csv file
     *     Parse the line to get attribute values
     *     Create a new tuple with the schema of the table
     *     Set the tuple values to the attribute values
     *     Add the tuple to the table
     * Close file
     *
     * Return table
     * @param tablename
     * @param schema
     * @param folder
     * @return Table
     */
    public static ITable readTable(String tablename, ISchema schema, String folder) {
        String filelocation = folder + "/" + tablename + ".csv";
        Table table = new Table(tablename, schema);
        try (BufferedReader reader = new BufferedReader(new FileReader(filelocation))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                for (int i = 0; i < fields.length; i++) {
                    fields[i] = clean(fields[i]);
                }
                Tuple tuple = new Tuple(schema);
                tuple.setValues(fields);
                table.addTuple(tuple);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return table;
    }



    /**
     * Writes the tables' data to a csv file
     *
     * Implement the following algorithm
     *
     * Open the csv file from the folder (corresponding to the tablename)
     * Clear all file content
     * For each tuple in table
     *   Write the tuple values to the file in csv format
     *
     * @param table
     * @param folder
     */
    public static void writeTable(ITable table, String folder) {
        String filelocation = folder + "/" + table.getName() + ".csv";
        try (FileWriter writer = new FileWriter(filelocation)) {
            List<ITuple> tuples = table.getTuples();
            for (ITuple tuple : tuples) {
                Object[] values = tuple.getValues();
                for (int i = 0; i < values.length; i++) {
                    writer.write(values[i].toString());
                    if (i != values.length - 1) {
                        writer.write(",");
                    }
                }
                writer.write("\n");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prints the table to console (mainly used to print the output of the select query)
     *
     * Implements the following algorithm
     *
     * Print the attribute names from the schema as tab separated values
     * For each tuple in the table
     *   Print the values in tab separated format
     *
     *
     * @param table
     * @param schema
     */
    public static void printTable(ITable table, ISchema schema) {
        for (int i = 0; i < schema.getAttributes().size(); i++) {
            System.out.print(schema.getName(i) + "\t");
        }
        System.out.println();
        for (ITuple tuple : table.getTuples()) {
            Object[] values = tuple.getValues();
            for (int i = 0; i < values.length; i++) {
                System.out.print(values[i]);
                if (i != values.length - 1) {
                    System.out.print("\t");
                }
            }
            System.out.print("\n");
        }
    }


    /**
     * Writes a tuple to a csv file
     *
     * Implements the following algorithm
     *
     * Open the csv file from the folder (corresponding to the tablename)
     * Append the tuple (as array of strings) in the csv format to the file
     *
     * @param tableName
     * @param values
     * @param folder
     */
    public static void writeTuple(String tableName, Object[] values, String folder) {
        String filelocation = folder + "/" + tableName + ".csv";
        try (FileWriter writer = new FileWriter(filelocation, true)) {
            for (int i = 0; i < values.length; i++) {
                writer.write(values[i].toString());
                if (i != values.length - 1) {
                    writer.write(",");
                }
            }
            writer.write("\n");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads and parses the schema, creates schema objects and (empty) tables and adds them to the provided database
     * The schema is stored in a text file:
     *
     * Implements the following algorithm
     *
     * Open the schema file
     * For each line
     *   Parse the line to get the table name, attribute names and attribute types
     *   Create an attribute map of (index, att_name:att_type) pairs
     *   For each attribute
     *     Store the index and name:type pair in the map (index represents the position of attribute in the schema)
     *   Create a new schema object with this attribute map
     *   Add the schema object to the database
     *   Create a new table object with the table name and the schema object
     *   Add the table to the database
     *
     * @param schemaFileName
     * @param folderName
     * @param db
     */
    public static void readSchema(String schemaFileName, String folderName, Database db) {
        String filelocation = folderName + "/" + schemaFileName;
        try (BufferedReader reader = new BufferedReader(new FileReader(filelocation))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int index = 0;
                Map<Integer, String> attributes = new HashMap<>();
                String tableName = clean(line.substring(0, line.indexOf("(")));
                line = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
                String[] fields = line.split(",");
                for (String attribute : fields) {
                    attributes.put(index++, clean(attribute.trim()));
                }
                Schema currentSchema = new Schema(attributes);
                db.addSchema(currentSchema);
                db.addTable(new Table(tableName, currentSchema));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String clean(String s) {
        if (s == null) return null;
        return s.replace("\uFEFF", "") // BOM
                .replace("\u0000", "") // Null char
                .trim();
    }
}
