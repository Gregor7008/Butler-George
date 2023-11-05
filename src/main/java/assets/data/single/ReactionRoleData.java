package assets.data.single;

import org.json.JSONObject;

import assets.data.MessageConnection;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class ReactionRoleData implements MessageConnection {
    
//  TODO
    
	public ReactionRoleData(TextChannel channel, Message message, JSONObject data) {
	    this.instanciateFromJSON(data);
	}
	
	public ReactionRoleData() {}

    public ReactionRoleData instanciateFromJSON(JSONObject data) {
        return this;
    }

    public JSONObject compileToJSON() {
        return null;
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
    
    public void onReactionAdd(String formatted_emoji, Member member) {
        
    }
    
    public void onReactionRemove(String formatted_emoji, Member member) {
        
    }
}