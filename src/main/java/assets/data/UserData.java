package assets.data;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import assets.data.single.TicketSelectionData;
import base.Bot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class UserData implements DataContainer {

    private final User user;
    private TicketSelectionData ticket_selection;
    private ConcurrentHashMap<Guild, MemberData> member_datas = new ConcurrentHashMap<>();
    
	public UserData(JSONObject data) {
	    this.user = Bot.INSTANCE.jda.retrieveUserById(data.getLong(Key.USER_ID)).complete();
        this.instanciateFromJSON(data);
    }
    
    public UserData(User user) {
        this.user = user;
    }

    @Override
    public DataContainer instanciateFromJSON(JSONObject data) {
        JSONObject ticket_selection_data = data.getJSONObject(Key.TICKET_SELECTION);
        if (!ticket_selection_data.isEmpty()) {
            this.ticket_selection = new TicketSelectionData(ticket_selection_data);
        }
        
        List<Guild> saved_guilds = new LinkedList<>();
        data.keySet().forEach(key -> {
            try {
                Guild guild_candidate = Bot.INSTANCE.jda.getGuildById(key);
                if (guild_candidate != null) {
                    saved_guilds.add(guild_candidate);
                }
            } catch (NumberFormatException e) {}
        });
        saved_guilds.forEach(guild -> member_datas.put(guild, new MemberData(data.getJSONObject(guild.getId()))));
        return this;
    }

    @Override
    public JSONObject compileToJSON() {
        JSONObject compiledData = new JSONObject();
        
        member_datas.forEach((guild, data) -> compiledData.put(guild.getId(), data.compileToJSON()));
        
        compiledData.put(Key.USER_ID, user.getIdLong());
        compiledData.put(Key.USER_NAME, user.getName());
        compiledData.put(Key.USER_DISCRIMINATOR, user.getDiscriminator());
        compiledData.put(Key.TICKET_SELECTION, this.ticket_selection.compileToJSON());
        
        return compiledData;
    }
    
    public TicketSelectionData getSelectedTicket() {
        return this.ticket_selection;
    }
    
    public UserData setSelectedTicket(TicketSelectionData ticket_selection) {
        this.ticket_selection = ticket_selection;
        return this;
    }
    
    public ConcurrentHashMap<Guild, MemberData> getMemberDatas() {
        return this.member_datas;
    }
    
    public MemberData getMemberData(Guild guild) {
        return this.member_datas.get(guild);
    }
    
    public UserData setMemberDatas(ConcurrentHashMap<Guild, MemberData> member_datas) {
        this.member_datas = member_datas;
        return this;
    }
    
    public UserData addMemberData(Guild guild, MemberData data) {
        this.member_datas.put(guild, data);
        return this;
    }
    
    public UserData addMemberDatas(ConcurrentHashMap<Guild, MemberData> member_datas) {
        this.member_datas.putAll(member_datas);
        return this;
    }
    
    public UserData removeMemberDatas(Guild... guilds) {
        for (int i = 0; i < guilds.length; i++) {
            this.member_datas.remove(guilds[i]);
        }
        return this;
    }
    
    public UserData removeMemberDatasByData(MemberData... datas) {
        DataTools.removeFromMap(this.member_datas, datas);
        return this;
    }
    
    private static class Key {
        public static final String USER_ID = "id";
        public static final String USER_NAME = "name";
        public static final String USER_DISCRIMINATOR = "discriminator";
        public static final String TICKET_SELECTION = "ticket_selection";
    }
}