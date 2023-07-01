package assets.data.single;

import org.json.JSONObject;

import assets.data.DataContainer;

public class SystemData implements DataContainer {

    public SystemData() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public JSONObject compileToJSON() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DataContainer instanciateFromJSON(JSONObject data) {
        // TODO Auto-generated method stub
        return null;
    }
    
    public String getBotToken() {
        return null;
    }
    
    public long getBotId() {
        return 0L;
    }
    
    public String getLicenseOwner() {
        return null;
    }
    
    public String getDatabaseUsername() {
        return null;
    }
    
    public String getDatabasePassword() {
        return null;
    }
    
    public String getDatabaseName() {
        return null;
    }
}