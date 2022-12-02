package assets.data.single;

import org.json.JSONObject;

import assets.base.exceptions.ReferenceNotFoundException.ReferenceType;
import assets.data.DataContainer;

public class WarningData implements DataContainer {

//  TODO Implement completely incl. ModController notification
    
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

    @Override
    public boolean verify(ReferenceType type) {
        // TODO Auto-generated method stub
        return false;
    }
}