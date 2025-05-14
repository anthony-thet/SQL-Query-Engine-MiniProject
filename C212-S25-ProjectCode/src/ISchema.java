import java.util.Map;

public interface ISchema {
    Map<String, Integer> getKeys();
    Map<Integer, String> getAttributes();
    Map<Integer, String> getNames();
    String getName(int index);
    String getType(int index);
}