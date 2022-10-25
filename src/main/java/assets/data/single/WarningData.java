package assets.data.single;

import org.json.JSONObject;

import assets.data.DataContainer;

public class WarningData implements DataContainer {

    public WarningData(JSONObject data) {
        this.instanciateFromJSON(data);
    }
    
    public WarningData() {}
    
    @Override
    public DataContainer instanciateFromJSON(JSONObject data) {
        return null;
    }
    
    @Override
    public JSONObject compileToJSON() {
        return null;
    }
}