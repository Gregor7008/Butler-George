package assets.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import assets.base.exceptions.EntityNotFoundException;
import assets.base.exceptions.EntityNotFoundException.ReferenceType;
import assets.data.single.ModMailData;
import assets.data.single.ModMailSelectionData;
import base.Bot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class UserData {

    private final long user_id;
    private ModMailData modmail_selection;
    private ConcurrentHashMap<Long, MemberData> member_datas = new ConcurrentHashMap<>();

    public UserData(User user) throws EntityNotFoundException {
        if (user == null) {
            throw new EntityNotFoundException(ReferenceType.USER, "Couldn't find guild, aborting config creation");
        } else {
            this.user_id = user.getIdLong();
        }
    }
    
	public UserData(JSONObject data) {
	    this.user_id = data.getLong(Key.USER_ID);
	    List<Guild> saved_guilds = new ArrayList<>();
        data.keySet().forEach(key -> {
            try {
                Guild guild_candidate = Bot.getAPI().getGuildById(key);
                if (guild_candidate != null) {
                    saved_guilds.add(guild_candidate);
                }
            } catch (NumberFormatException e) {}
        });
        saved_guilds.forEach(guild -> member_datas.put(guild.getIdLong(), new MemberData(guild, data.getJSONObject(guild.getId()))));

        JSONObject modmail_selection_data = data.getJSONObject(Key.MODMAIL_SELECTION);
        if (!modmail_selection_data.isEmpty()) {
            ModMailSelectionData selection_data = new ModMailSelectionData(modmail_selection_data);
            this.modmail_selection = member_datas.get(selection_data.getGuildId()).getModmail(selection_data.getTicketId());
        }
    }
	
    public JSONObject compileToJSON() {
        JSONObject compiledData = new JSONObject();
        
        member_datas.forEach((guild, data) -> compiledData.put(String.valueOf(guild), data.compileToJSON()));
        
        compiledData.put(Key.USER_ID, this.getUser().getIdLong());
        compiledData.put(Key.USER_NAME, this.getUser().getName());
        
        ModMailSelectionData modmail_selection_data = new ModMailSelectionData();
        if (modmail_selection != null) {
            modmail_selection_data.setGuild(modmail_selection.getGuild()).setTicketId(modmail_selection.getTicketId());            
        }
        compiledData.put(Key.MODMAIL_SELECTION, modmail_selection_data);
        
        return compiledData;
    }
    
    public User getUser() {
        return Bot.getAPI().retrieveUserById(this.user_id).complete();
    }
    
    public ModMailData getSelectedModMail() {
        return this.modmail_selection;
    }
    
    public void setSelectedModMail(ModMailData ticket_selection) {
        this.modmail_selection = ticket_selection;
    }
    
    public void setSelectedModMail(Guild guild, int ticket_id) {
        this.modmail_selection = member_datas.get(guild.getIdLong()).getModmail(ticket_id);
    }
    
    public ConcurrentHashMap<Long, MemberData> getMemberDataByIds() {
        return this.member_datas;
    }
    
    public ConcurrentHashMap<Guild, MemberData> getMemberData() {
        ConcurrentHashMap<Guild, MemberData> return_value = new ConcurrentHashMap<>();
        for (Map.Entry<Long, MemberData> entry : this.member_datas.entrySet()) {
            return_value.put(Bot.getAPI().getGuildById(entry.getKey()), entry.getValue());
        }
        return return_value;
    }
    
    public MemberData getMemberDataById(long guild_id) {
        return this.member_datas.get(guild_id);
    }
    
    public MemberData getMemberData(Guild guild) {
        return this.member_datas.get(guild.getIdLong());
    }
    
    public void setMemberDatas(ConcurrentHashMap<Guild, MemberData> member_datas) {
        ConcurrentHashMap<Long, MemberData> converted_map = new ConcurrentHashMap<>();
        if (this.member_datas != null) {
            for (Map.Entry<Guild, MemberData> entry : member_datas.entrySet()) {
                converted_map.put(entry.getKey().getIdLong(), entry.getValue());
            }
        }
        this.member_datas = converted_map;
    }
    
    public void addMemberDatas(MemberData... datas) {
        for (MemberData data : datas) {
            this.member_datas.put(data.getGuildId(), data);
        }
    }
    
    public void removeMemberDatas(long... guild_ids) {
        for (int i = 0; i < guild_ids.length; i++) {
            this.member_datas.remove(guild_ids[i]);
        }
    }
    
    public void removeMemberDatas(Guild... guilds) {
        for (int i = 0; i < guilds.length; i++) {
            this.member_datas.remove(guilds[i].getIdLong());
        }
    }
    
    public void removeMemberDatasByData(MemberData... datas) {
        for (MemberData data : datas) {
            this.member_datas.remove(data.getGuildId());
        }
    }
    
    private static abstract class Key {
        public static final String USER_ID = "id";
        public static final String USER_NAME = "name";
        public static final String MODMAIL_SELECTION = "modmail_selection";
    }
}