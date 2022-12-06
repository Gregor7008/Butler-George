package assets.data;

import org.json.JSONObject;

import assets.base.exceptions.EntityNotFoundException.ReferenceType;

public interface DataContainer {
    
    public JSONObject compileToJSON();
    public DataContainer instanciateFromJSON(JSONObject data);
    public boolean verify(ReferenceType type);
    
}