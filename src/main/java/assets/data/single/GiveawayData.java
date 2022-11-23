package assets.data.single;

import org.json.JSONObject;

import assets.data.DataContainer;
import base.Bot;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class GiveawayData implements DataContainer {
    
    private final TextChannel text_channel;
    private final Message message;
    private final User user;
    

	public GiveawayData(Message message, JSONObject data) {
	    this.text_channel = message.getChannel().asTextChannel();
	    this.message = message;
	    this.user = Bot.INSTANCE.jda.retrieveUserById(data.getLong(Key.USER_ID)).complete();
	    this.instanciateFromJSON(data);
	}
	
	public GiveawayData(Message message, User user) {
	    this.text_channel = message.getChannel().asTextChannel();
	    this.message = message;
	    this.user = user;
	}

    @Override
    public DataContainer instanciateFromJSON(JSONObject data) {
        return this;
    }

    @Override
    public JSONObject compileToJSON() {
        JSONObject compiledData = new JSONObject();
        return compiledData;
    }

    public TextChannel getChannel() {
        return this.text_channel;
    }

    public Message getMessage() {
        return this.message;
    }
    
    public User getUser() {
        return this.user;
    }
    
    private static abstract class Key {
        public static final String USER_ID = "user_id";
        public static final String TITLE = "title";
        public static final String PRIZES = "prizes";
        public static final String SIGN_UPS = "sign_ups";
        public static final String ANONYMOUS = "anonymous";
        public static final String ALLOWED_ROLES = "allowed_roles";
        public static final String TIME_LIMIT = "time_limit";
    }
}