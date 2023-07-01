package assets.data.single;

import org.json.JSONObject;

import assets.base.exceptions.EntityNotFoundException.ReferenceType;
import assets.data.DataContainer;
import assets.data.MessageConnection;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class ReactionRoleData implements DataContainer, MessageConnection {

//  TODO Implement completely
    
	public ReactionRoleData(TextChannel channel, Message message, JSONObject data) {
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

    @Override
    public TextChannel getChannel() {
        return null;
    }

    @Override
    public Long getChannelId() {
        return 0L;
    }

    @Override
    public Message getMessage() {
        return null;
    }

    @Override
    public Long getMessageId() {
        return 0L;
    }
}