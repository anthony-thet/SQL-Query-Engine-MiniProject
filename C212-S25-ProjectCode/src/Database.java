import java.util.*;

/**
 * The main database class
 * Database as a list of tables, list of schemas and a folder name where the database is stored
 * Database is stored (on the disk) in the form of three csv files and schema text file
 */
class Database {
    private List<ITable> tables;
    private List<ISchema> schemas;
    private String folderName;

    /**
     * Constructor
     * Creates the empty tables and schema lists
     * Reads the schema file to add schemas to the database
     * Populates the database table (with the data read from the csv files)
     * @param folderName
     * @param schemaFileName
     */
    public Database(String folderName, String schemaFileName) {
        this.tables = new ArrayList<>();
        this.schemas = new ArrayList<>();
        this.folderName = folderName;

        IO.readSchema(schemaFileName, folderName, this);
        populateDB();
    }

    /**
     * Adds a table to the database
     * @param table
     */
    public void addTable(ITable table) {
        this.tables.add(table);
    }

    /**
     * Adds a table schema to the database
     * @param schema
     */
    public void addSchema(ISchema schema) {
        this.schemas.add(schema);
    }

    /**
     * Return the list of tables in the database
     * @return
     */
    public List<ITable> getTables() {
        return tables;
    }

    /**
     * Returns the list of schemas in the database
     * @return
     */
    public List<ISchema> getSchemas() {
        return schemas;
    }

    /**
     * The list of tables in the database is initialized with empty tables in the constructor
     * An empty table has a name and an empty list of tuples
     * This method sets the empty table in the list to the one provided as a parameter
     * @param table
     */
    public void updateTable(ITable table) {
        for (int i = 0; i < tables.size(); i++) {
            if (tables.get(i).getName().equals(table.getName())) {
                tables.set(i, table);
                return;
            }
        }
    }

    /**
     * Populates the database
     *
     * Implements the following algorithm
     *
     * For each table in the db (tables are initially empty)
     *   Get the table's data from the csv file (by calling the read table method)
     *   Update the table (by calling the udpate table method)
     */
    public void populateDB() {
        for (int i = 0; i < tables.size(); i++) {
            updateTable(IO.readTable(tables.get(i).getName(), schemas.get(i), folderName));
        }
    }

    /**
     * Insert data into a table based upon the insert query
     * If the query is invalid throws an InvalidQueryException
     *
     * Implements the following algorithm
     *
     * Parse the insert into clause to get the table name, attribute name(s) and value(s)
     * If the query in not valid
     *   Throw an invalid query exception
     *   Exit
     * Create a new tuple with the schema of the table
     * Set the tuple values to the values from the query
     * Open the file corresponding to the table name
     * Append the tuple values (as comma separated values) to the end of the file
     *
     * @param query
     * @throws InvalidQueryException
     */
    public void insertData(String query) throws InvalidQueryException {
        query = query.trim();

        if (!query.startsWith("INSERT INTO")) {
            throw new InvalidQueryException("Not an INSERT query.");
        }

        query = query.substring(11).trim();

        int parenIndex = query.indexOf('(');
        if (parenIndex == -1) {
            throw new InvalidQueryException("Missing parentheses.");
        }
        String tableName = query.substring(0, parenIndex).trim();
        String rest = query.substring(parenIndex).trim();
        int valuesIndex = rest.indexOf("VALUES");
        if (valuesIndex == -1) {
            throw new InvalidQueryException("Missing VALUES keyword.");
        }
        String attrPart = rest.substring(0, valuesIndex).trim();
        String valPart = rest.substring(valuesIndex + 6).trim();

        attrPart = attrPart.replace("(", "").replace(")", "").trim();
        valPart = valPart.replace("(", "").replace(")", "").trim();
        String[] attributes = attrPart.split(",");
        String[] values = valPart.split(",");
        if (attributes.length != values.length) {
            throw new InvalidQueryException("Mismatch between attributes and values.");
        }

        ITable table = null;
        for (ITable t : tables) {
            if (t.getName().equalsIgnoreCase(tableName)) {
                table = t;
                break;
            }
        }
        if (table == null) {
            throw new InvalidQueryException("Table not found: " + tableName);
        }
        ISchema schema = table.getSchema();
        Tuple tuple = new Tuple(schema);
        Object[] tupleValues = new Object[schema.getAttributes().size()];
        for (int i = 0; i < attributes.length; i++) {
            String attr = attributes[i].trim();
            String value = values[i].trim().replaceAll("^'+|'+$", "");
            boolean found = false;
            for (Map.Entry<Integer, String> entry : schema.getAttributes().entrySet()) {
                String attrName = entry.getValue().split(":")[0];
                if (attrName.equalsIgnoreCase(attr)) {
                    tupleValues[entry.getKey()] = value;
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new InvalidQueryException("Attribute not found: " + attr);
            }
        }
        tuple.setValues(tupleValues);
        table.addTuple(tuple);
        IO.writeTuple(table.getName(), tuple.getValues(), folderName);
    }

    /**
     * Selects data from a table (and returns it in the form of a results table)
     * If the query in not valid, throws an InvalidQueryException
     *
     * A query is valid if
     *
     /* 1.	It has a select clause (select keyword followed by at least one attribute name)
     /* 2.	It has a from clause (from keyword followed by a table name)
     /* 3.	All the attribute names in the select clause are in the schema
     /* 4.	The table name in the from clause is in the schema
     * 5.	All the attribute names in the where clause (if present) are in the schema
     * 6.	The attribute name in the order by clause (if present) is in the schema
     *
     * Implements the following algorithm
     *
     * Parse the query to get the select, from, where and order by clauses and the attribute and table names and condition
     * If the query is not valid
     *   Throw an invalid query exception
     *   Exit
     * Create a new results schema based with the attributes from the select clause
     * Create a new result table
     * For each tuple in the table
     *   If the tuple matches the where clause condition(s)
     *     Create a new results tuple using the result schema
     *     Set the results tuple values to the current tuple corresponding values
     *     Add the results tuple to the result table
     * Return results table
     *
     *
     * @param query
     * @return
     * @throws InvalidQueryException
     */
    public ITable selectData(String query) throws InvalidQueryException {
        if (!query.startsWith("SELECT")) {
            throw new InvalidQueryException("Not an select query.");
        }

        query = query.substring(6).trim();

        int fromIndex = query.indexOf("FROM");
        if (fromIndex == 0) {
            throw new InvalidQueryException("Missing SELECT attributes");
        }
        if (fromIndex == -1) {
            throw new InvalidQueryException("Missing FROM.");
        }

        String attr = query.substring(0, fromIndex).trim();
        String[] attributes = attr.split("\\s*,\\s*");
        query = query.substring(fromIndex + 4).trim();

        String tableName = null;
        ITable table = null;

        int whereIndex = query.indexOf("WHERE");
        if (whereIndex == 0) {
            throw new InvalidQueryException("Missing FROM table name");
        }

        if (whereIndex == -1) {
            tableName = query;
            for (ITable t: tables) {
                if (t.getName().equalsIgnoreCase(tableName)) {
                    table = t;
                    break;
                }
            }
            if (table == null) {
                throw new InvalidQueryException("Table not found.");
            }
            Map<Integer, String> attrForSchema = new HashMap<>();
            for (int i = 0; i < attributes.length; i++) {
                if (!table.getSchema().getNames().containsValue(attributes[i])) {
                    throw new InvalidQueryException("Attribute " + attributes[i] + " not found in table.");
                }
                int colIndex = table.getSchema().getKeys().get(attributes[i]);
                String type = table.getSchema().getType(colIndex);
                attrForSchema.put(i, attributes[i] + ":" + type);
            }
            ISchema resSchema = new Schema(attrForSchema);
            ITable resTable = new Table("Res", resSchema);
            for (ITuple t: table.getTuples()) {
                ITuple resTuple = new Tuple(resSchema);
                for (int i = 0; i < attributes.length; i++) {
                    int valueIndex = table.getSchema().getKeys().get(attributes[i]);
                    resTuple.setValue(i, t.getValue(valueIndex));
                }
                resTable.addTuple(resTuple);
            }
            return resTable;
        }
        else {
            tableName = query.substring(0, whereIndex).trim();
            String whereAttr = query.substring(whereIndex + 5).trim();
            String[] whereAttrs = whereAttr.split("\\s+");
            for (ITable t : tables) {
                if (t.getName().equalsIgnoreCase(tableName)) {
                    table = t;
                    break;
                }
            }
            if (table == null) {
                throw new InvalidQueryException("Table not found.");
            }
            Map<Integer, String> attrForSchema = new HashMap<>();
            for (int i = 0; i < attributes.length; i++) {
                if (!table.getSchema().getNames().containsValue(attributes[i])) {
                    throw new InvalidQueryException("Attribute " + attributes[i] + " not found in table.");
                }
                int colIndex = table.getSchema().getKeys().get(attributes[i]);
                String type = table.getSchema().getType(colIndex);
                attrForSchema.put(i, attributes[i] + ":" + type);

            }
            ISchema resSchema = new Schema(attrForSchema);
            ITable resTable = new Table("Res", resSchema);
            for (ITuple t: table.getTuples()) {
                ITuple resTuple = new Tuple(resSchema);
                Condition condition = new Condition(whereAttrs[0], whereAttrs[2], whereAttrs[1]);
                if (condition.checkCondition(t, table.getSchema())) {
                    for (int i = 0; i < attributes.length; i++) {
                        int valueIndex = table.getSchema().getKeys().get(attributes[i]);
                        resTuple.setValue(i, t.getValue(valueIndex));
                    }
                    resTable.addTuple(resTuple);
                }
            }
            return resTable;
        }
    }

    /**
     * Delete data from a table
     * If the query in not valid, throws an InvalidQueryException
     *
     * Implements the following algorithm
     *
     * Parse the query to get the from and where clauses
     * Parse the from clause to get the table name
     * If the query in not valid
     *   Throw an invalid query exception
     *   Exit
     * If where clause is not empty
     *   Parse the where clause to get the the condition
     *   For each tuple in the table
     *     If the where clause condition is true
     *       Remove the tuple from the table
     * Else
     *   For each tuple in the table
     *     Remove the tuple from the table
     * Write the table to the file
     *
     * @param query
     * @throws InvalidQueryException
     */
    public void deleteData(String query) throws InvalidQueryException {
        if (!query.startsWith("DELETE FROM")) {
            throw new InvalidQueryException("Not an delete query.");
        }

        query = query.substring(11).trim();

        int whereIndex = query.indexOf("WHERE");
        ITable table = null;
        if (whereIndex == -1) {
            String fromClause = query.trim();
            for (ITable t: tables) {
                if (t.getName().equalsIgnoreCase(fromClause)) {
                    table = t;
                    break;
                }
            }
            if (table == null) {
                throw new InvalidQueryException("Table not found.");
            }
            table.getTuples().clear();
            return;
        }
        else {
            String fromClause = query.substring(0, whereIndex).trim();
            String whereClause = query.substring(whereIndex + 6).trim();
            for (ITable t : tables) {
                if (t.getName().equalsIgnoreCase(fromClause)) {
                    table = t;
                    break;
                }
            }
            if (table == null) {
                throw new InvalidQueryException("Table not found.");
            }
            if (!whereClause.isEmpty()) {
                String[] conditionStr = whereClause.split("\\s+");
                Condition condition = new Condition(conditionStr[0], conditionStr[2], conditionStr[1]);
                List<ITuple> toDelete = new ArrayList<>();
                for (ITuple t : table.getTuples()) {
                    if (condition.checkCondition(t, table.getSchema())) {
                        toDelete.add(t);
                    }
                }
                table.getTuples().removeAll(toDelete);
            }
        }
        IO.writeTable(table, folderName);
        updateTable(table);
    }

}
