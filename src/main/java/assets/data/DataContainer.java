package assets.data;

import org.json.JSONObject;

public interface DataContainer {
    
    public JSONObject compileToJSON();
    public DataContainer instanciateFromJSON(JSONObject data);

}