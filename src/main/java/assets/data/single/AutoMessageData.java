package assets.data.single;

import org.json.JSONObject;

import base.Bot;
import engines.base.LanguageEngine;
import engines.base.Toolbox;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

public class AutoMessageData {

    private final long guild_id;
    private long  text_channel_id;
    private String title, message = "N/A";
    private boolean embedded, activated = false;
   
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
    
    public TextChannel getTextChannel() {
        Guild guild = this.getGuild();
        if (guild != null) {
            return guild.getTextChannelById(text_channel_id);
        } else {
            return null;
        }
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
        if (activated && (!title.isBlank() || !message.isBlank()) && text_channel != null) {
            String title_edit = Toolbox.processAutoMessage(title, guild, user, false);
            String message_edit = Toolbox.processAutoMessage(message, guild, user, true);
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