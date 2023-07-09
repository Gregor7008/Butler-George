package assets.data.single;

import org.json.JSONObject;

import base.Bot;
import net.dv8tion.jda.api.entities.Guild;

public class ModMailSelectionData {
    
    private Guild guild;
    private int ticket_id = 0;
    
    public ModMailSelectionData(JSONObject data) {
        this.instanciateFromJSON(data);
    }
    
    public ModMailSelectionData() {}

    public ModMailSelectionData instanciateFromJSON(JSONObject data) {
        long guild_id = data.getLong(Key.GUILD_ID);
        if (guild_id != 0L) {
            this.guild = Bot.getAPI().getGuildById(guild_id);
        }
        this.ticket_id = data.getInt(Key.TICKET_ID);
        return this;
    }

    public JSONObject compileToJSON() {
        JSONObject compiledData = new JSONObject();
        
        if (guild == null) {
            compiledData.put(Key.GUILD_ID, 0L);
        } else {
            compiledData.put(Key.GUILD_ID, guild.getIdLong());
        }
        compiledData.put(Key.TICKET_ID, ticket_id);
        
        return compiledData;
    }
    
    public Guild getGuild() {
        return this.guild;
    }
    
    public Long getGuildId() {
        return this.guild.getIdLong();
    }
    
    public ModMailSelectionData setGuild(Guild guild) {
        this.guild = guild;
        return this;
    }
    
    public int getTicketId() {
        return ticket_id;
    }
    
    public ModMailSelectionData setTicketId(int ticket_id) {
        this.ticket_id = ticket_id;
        return this;
    }
    
    private static abstract class Key {
        public static final String GUILD_ID = "guild_id";
        public static final String TICKET_ID = "ticket_id";
    }
}