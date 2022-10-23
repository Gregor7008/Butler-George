package assets.data;

import org.json.JSONObject;

import base.Bot;
import net.dv8tion.jda.api.entities.User;

public class UserData implements DataContainer {

    private final User user;
    
	public UserData(JSONObject data) {
	    this.user = Bot.INSTANCE.jda.retrieveUserById(data.getLong(DataKey.User.USER_ID)).complete();
        this.instanciateFromJSON(data);
    }
    
    public UserData(User user) {
        this.user = user;
    }

    @Override
    public DataContainer instanciateFromJSON(JSONObject data) {
        return this;
    }

    @Override
    public JSONObject compileToJSON() {
        return null;
    }
}