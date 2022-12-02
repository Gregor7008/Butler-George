package assets.data.single;

import org.json.JSONObject;

import assets.base.exceptions.ReferenceNotFoundException.ReferenceType;
import assets.data.DataContainer;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class ReactionRoleData implements DataContainer {

//  TODO Implement completely
    
	public ReactionRoleData(JSONObject data) {
	    this.instanciateFromJSON(data);
	}
	
	public ReactionRoleData() {}

    @Override
    public DataContainer instanciateFromJSON(JSONObject data) {
        return this;
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

    public TextChannel getChannel() {
        return null;
    }

    public Message getMessage() {
        return null;
    }
}