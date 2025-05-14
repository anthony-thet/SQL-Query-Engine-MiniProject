import java.util.HashMap;
import java.util.Map;

/**
 * A tuple is an ordered collection of Objects and their associated types (Integer, Double or String)
 * Objects are stored in an array while types are stored in a map of (index, type)
 *
 */
public class Tuple implements ITuple {
    private Object[] values;
    private Map<Integer, Class<?>> typeMap;

    /**
     * The constructor receives a schema and creates the object array and typemap (representing the tuple)
     * The schema has the types of attributes stored as strings ("Integer", "Double", "String")
     * Based upon these types the constructor stores the actual class (Integer.class, Double.class, String.class) to the typemap
     * @param schema
     */
    public Tuple(ISchema schema) {
        int size = schema.getAttributes().size();
        this.values = new Object[size];
        this.typeMap = new HashMap<>();
        for (int i = 0; i < size; i++) {

            switch (schema.getType(i)) {
                case "String":
                    this.typeMap.put(i, String.class);
                    break;
                case "Integer":
                    this.typeMap.put(i, Integer.class);
                    break;
                case "Double":
                    this.typeMap.put(i, Double.class);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported type: " + schema.getType(i));

            }

        }
    }

    /**
     * Stores the value at the given index in the (tuple) object
     * The value is converted from the object to to its actual class from the typemap
     * @param index
     * @param value
     */
    @Override

    public void setValue(int index, Object value) {
        Class<?> expectedType = typeMap.get(index);
        if (expectedType == Integer.class) {
            values[index] = Integer.parseInt(value.toString());
        } else if (expectedType == Double.class) {
            values[index] = Double.parseDouble(value.toString());
        } else if (expectedType == String.class) {
            values[index] = value.toString();
        }
    }

    /**
     * Returns the value at a given index from the tuple object
     * @param index
     * @return
     * @param <T>
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getValue(int index) {
        return (T) this.values[index];
    }

    /**
     * Returns the tuple as an array of Objects
     * @return
     */
    @Override
    public Object[] getValues() {
        return this.values;
    }

    /**
     * Sets the tuple values to the provided ones
     * The values are converted from objects to their actual classes from the typemap
     * @param values
     */
    @Override
    public void setValues(Object[] values) {
        for (int i = 0; i < values.length; i++) {
            setValue(i, values[i]);
        }
    }
}