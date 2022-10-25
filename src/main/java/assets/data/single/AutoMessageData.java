package assets.data.single;

import org.json.JSONObject;

import assets.data.DataContainer;

public class AutoMessageData implements DataContainer {

	public AutoMessageData(JSONObject data) {
	    this.instanciateFromJSON(data);
	}
	
	public AutoMessageData() {}

    @Override
    public DataContainer instanciateFromJSON(JSONObject data) {
        return this;
    }

    @Override
    public JSONObject compileToJSON() {
        return null;
    }
}