package assets.data.single;

import org.json.JSONObject;

import assets.base.exceptions.EntityNotFoundException.ReferenceType;
import assets.data.DataContainer;
import base.Bot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class ModMailData implements DataContainer {

    private final User user;
    private final Guild guild;
    private final TextChannel text_channel;
    private int ticket_id;
    private Message lastGuildMessage, lastUserMessage, feedbackMessage;
    private String title;
    
	public ModMailData(Guild guild, TextChannel channel, JSONObject data) {
	    this.guild = guild;
        this.text_channel = channel;
	    this.user = Bot.getAPI().retrieveUserById(data.getLong(Key.USER_ID)).complete();
	    this.instanciateFromJSON(data);
	}

	public ModMailData(Guild guild, User user, TextChannel channel) {
	    this.guild = guild;
	    this.user = user;
	    this.text_channel = channel;
	}
	
    @Override
    public DataContainer instanciateFromJSON(JSONObject data) {
        this.ticket_id = data.getInt(Key.TICKET_ID);
        
        this.lastGuildMessage = this.text_channel.retrieveMessageById(data.getLong(Key.LAST_GUILD_MESSAGE_ID)).complete();
        this.feedbackMessage = this.text_channel.retrieveMessageById(data.getLong(Key.FEEDBACK_MESSAGE_ID)).complete();
        this.lastUserMessage = this.user.openPrivateChannel().complete().retrieveMessageById(data.getLong(Key.LAST_USER_MESSAGE_ID)).complete();
        
        this.title = data.getString(Key.TITLE);
        
        return this;
    }

    @Override
    public JSONObject compileToJSON() {
        JSONObject compiledData = new JSONObject();
        
        compiledData.put(Key.TICKET_ID, this.ticket_id);
        
        compiledData.put(Key.USER_ID, user.getIdLong());
        
        compiledData.put(Key.LAST_GUILD_MESSAGE_ID, this.lastGuildMessage.getIdLong());
        compiledData.put(Key.LAST_USER_MESSAGE_ID, this.lastUserMessage.getIdLong());
        compiledData.put(Key.FEEDBACK_MESSAGE_ID, this.feedbackMessage.getIdLong());

        compiledData.put(Key.TITLE, this.title);
        
        return compiledData;
    }

    @Override
    public boolean verify(ReferenceType type) {
        // TODO Auto-generated method stub
        return false;
    }
    
    public long getGuildChannelId() {
        return this.text_channel.getIdLong();
    }

    public TextChannel getGuildChannel() {
        return this.text_channel;
    }

    public long getGuildId() {
        return this.guild.getIdLong();
    }
    
    public Guild getGuild() {
        return this.guild;
    }

    public long getUserId() {
        return this.user.getIdLong();
    }
    
    public User getUser() {
        return this.user;
    }

    public int getTicketId() {
        return this.ticket_id;
    }
    
    public ModMailData setTicketId(int ticket_id) {
        this.ticket_id = ticket_id;
        return this;
    }
    
    public Message getLastGuildMessage() {
        return this.lastGuildMessage;
    }
    
    public ModMailData setLastGuildMessage(Message lastGuildMessage) {
        this.lastGuildMessage = lastGuildMessage;
        return this;
    }
    
    public Message getLastUserMessage() {
        return this.lastUserMessage;
    }
    
    public ModMailData setLastUserMessage(Message lastUserMessage) {
        this.lastUserMessage = lastUserMessage;
        return this;
    }
    
    public Message getFeedbackMessage() {
        return this.feedbackMessage;
    }
    
    public ModMailData setFeedbackMessage(Message feedbackMessage) {
        this.feedbackMessage = feedbackMessage;
        return this;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public ModMailData setTitle(String title) {
        this.title = title;
        return this;
    }
    
    private static abstract class Key {
        public static final String USER_ID = "user_id";
        public static final String TICKET_ID = "ticket_id";
        public static final String LAST_GUILD_MESSAGE_ID = "last_guild_message_id";
        public static final String LAST_USER_MESSAGE_ID = "last_user_message_id";
        public static final String FEEDBACK_MESSAGE_ID = "feedback_message_id";
        public static final String TITLE = "title";
    }

    public long getLastUserMessageId() {
        return 0L;
    }

    public long getLastGuildMessageId() {
        // TODO Auto-generated method stub
        return 0L;
    }
}