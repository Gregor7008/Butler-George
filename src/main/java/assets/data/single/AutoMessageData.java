package assets.data.single;

import java.time.OffsetDateTime;

import org.json.JSONObject;

import base.Bot;
import engines.base.LanguageEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

public class AutoMessageData {

    private final long guild_id;
    private long  text_channel_id;
    private String title, message = "N/A";
    private boolean embedded, activated = false;
    
	public static String processRawMessage(Guild guild, User user, boolean mentions, String input) {
		String output =  input.replace("{server}", guild.getName())
				.replace("{membercount}", Integer.toString(guild.getMemberCount()))
				.replace("{date}", OffsetDateTime.now().format(LanguageEngine.DEFAULT_TIME_FORMAT))
				.replace("{boosts}", String.valueOf(guild.getBoostCount()));
		if (user != null) {
	        if (mentions) {
	            output = output.replace("{user}", user.getAsMention());
	        } else {
	            output = output.replace("{user}", user.getName());
	        }
		}
		return output;
	}
	
    public AutoMessageData(Guild guild, JSONObject data) {
        guild_id = guild.getIdLong();
        if (!data.isEmpty()) {
            this.instanciateFromJSON(data);
        }
	}
	
	public AutoMessageData(Guild guild, TextChannel channel) {
	    guild_id = guild.getIdLong();
	    text_channel_id = channel.getIdLong();
	}

    public AutoMessageData instanciateFromJSON(JSONObject data) {
        text_channel_id = data.getLong(Key.CHANNEL_ID);
        
        title = data.getString(Key.TITLE);
        message = data.getString(Key.MESSAGE);
        
        embedded = data.getBoolean(Key.EMBEDDED);
        activated = data.getBoolean(Key.ACTIVATED);
        
        return this;
    }

    public JSONObject compileToJSON() {
        JSONObject compiledData = new JSONObject();

        compiledData.put(Key.CHANNEL_ID, text_channel_id);

        compiledData.put(Key.TITLE, title);
        compiledData.put(Key.MESSAGE, message);

        compiledData.put(Key.EMBEDDED, embedded);
        compiledData.put(Key.ACTIVATED, activated);

        return compiledData;
    }
    
    public Guild getGuild() {
        return Bot.getAPI().getGuildById(guild_id);
    }
    
    public long getTextChannelId() {
    	return text_channel_id;
    }
    
    public TextChannel getTextChannel() {
    	return this.getGuild().getTextChannelById(text_channel_id);
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public AutoMessageData setTitle(String title) {
        this.title = title;
        return this;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public AutoMessageData setMessage(String message) {
        this.message = message;
        return this;
    }
    
    public boolean isEmbedded() {
        return this.embedded;
    }
    
    public AutoMessageData setEmbedded(boolean embedded) {
        this.embedded = embedded;
        return this;
    }
    
    public boolean isActivated() {
        return this.activated;
    }
    
    public AutoMessageData setActivated(boolean activated) {
        this.activated = activated;
        return this;
    }
    
    public MessageCreateAction buildMessage(User user) {
        Guild guild = this.getGuild();
        TextChannel text_channel = guild.getTextChannelById(text_channel_id);
        if (activated && (!title.isBlank() || !message.isBlank()) && text_channel != null) {
            String title_edit = processRawMessage(guild, user, false, title);
            String message_edit = processRawMessage(guild, user, true, message);
            if (embedded) {
                return text_channel.sendMessageEmbeds(LanguageEngine.buildMessageEmbed(title_edit, message_edit));
            } else {
                return text_channel.sendMessage(LanguageEngine.buildMessage(title_edit, message_edit));
            }
        } else {
            return null;
        }
    }
    
    private static abstract class Key {
        public static final String CHANNEL_ID = "channel_id";
        public static final String TITLE = "title";
        public static final String MESSAGE = "message";
        public static final String EMBEDDED = "embedded";
        public static final String ACTIVATED = "activated";
    }
}