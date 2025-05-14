import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * the schema is stored as a map of (index, name:type) pairs
 */
public class Schema implements ISchema {

    private Map<Integer, String> attributes;
    private Map<String, Integer> keys;

    /**
     * constructor
     * @param attributes
     */
    public Schema(Map<Integer, String> attributes) {
        this.attributes = attributes;
        this.keys = new HashMap<>();
        for (Map.Entry<Integer, String> entry : attributes.entrySet()) {
            String full = entry.getValue();
            String name = full.split(":")[0];
            keys.put(name, entry.getKey());
        }
    }

    /**
     * getter
     * @return keys
     */
    @Override
    public Map<String, Integer> getKeys() {
        return keys;
    }

    /**
     * getter
     * @return attributes
     */
    @Override
    public Map<Integer, String> getAttributes() {
        return attributes;
    }

    /**
     * getter
     * @return name
     */
    @Override
    public Map<Integer, String> getNames() {
        Map<Integer, String> names = new HashMap<>();
        for (int i = 0; i < attributes.size(); i++) {
            names.put(i, getName(i));
        }
        return names;
    }

    /**
     * splits the name:type to return the attribute name
     * @param index
     * @return
     */
    @Override
    public String getName(int index) {
        return attributes.get(index).split(":")[0];
    }

    /**
     * splits the name:type to return the attribute type
     * @param index
     * @return
     */

    @Override
    public String getType(int index) {
        return attributes.get(index).split(":")[1];
    }
}