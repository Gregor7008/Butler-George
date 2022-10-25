package assets.data.single;

import org.json.JSONObject;

import assets.data.DataContainer;

public class TicketSelectionData implements DataContainer {
    
    public TicketSelectionData(JSONObject data) {
        this.instanciateFromJSON(data);
    }
    
    public TicketSelectionData() {}
    
    @Override
    public DataContainer instanciateFromJSON(JSONObject data) {
        return null;
    }
    
    @Override
    public JSONObject compileToJSON() {
        return null;
    }
}