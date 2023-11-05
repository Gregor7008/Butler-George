package assets.data.single;

import java.time.OffsetDateTime;

import org.json.JSONObject;

import net.dv8tion.jda.api.entities.User;

public class WarningData {
    
//  TODO
    
    private String reason = "~ Unknown ~";
    private long creator_id = 0L;
    private OffsetDateTime creation_time;
    
    public static WarningData create(String reason, User creator) {
        return null;
    }
    
    public WarningData(JSONObject data) {
        
    }
    
    public JSONObject compileToJSON() {
        return null;
    }
}