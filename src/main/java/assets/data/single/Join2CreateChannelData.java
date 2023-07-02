package assets.data.single;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import assets.base.exceptions.EntityNotFoundException.ReferenceType;
import assets.data.DataContainer;
import assets.data.DataTools;
import engines.base.Check;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

public class Join2CreateChannelData implements DataContainer {

    private static final ConcurrentHashMap<Guild, List<Join2CreateChannelData>> JOIN2CREATE_CHANNEL_LIST = new ConcurrentHashMap<>();
    
    private final Guild guild;
    private final VoiceChannel channel;
    private String name_format = "{member}'s channel";
    private int limit_preset = -1;
    private boolean configurable = false;
    private List<VoiceChannel> children = new ArrayList<>();
    
    public static Join2CreateChannelData getParentOf(VoiceChannel children) {
        List<Join2CreateChannelData> join2create_channel_guild_data = JOIN2CREATE_CHANNEL_LIST.get(children.getGuild());
        for (int i = 0; i < join2create_channel_guild_data.size(); i++) {
            Join2CreateChannelData current = join2create_channel_guild_data.get(i);
            if (current.isParentOf(children)) {
                return current;
            }
        }
        return null;
    }
    
	public Join2CreateChannelData(VoiceChannel channel, JSONObject data) {
	    this.channel = channel;
	    this.guild = channel.getGuild();
	    this.instanciateFromJSON(data);
	    JOIN2CREATE_CHANNEL_LIST.get(channel.getGuild()).add(this);
	}
	
	public Join2CreateChannelData(VoiceChannel channel) {
	    this.channel = channel;
        this.guild = channel.getGuild();
        JOIN2CREATE_CHANNEL_LIST.get(channel.getGuild()).add(this);
	}

    @Override
    public DataContainer instanciateFromJSON(JSONObject data) {
        this.name_format = data.getString(Key.NAME_FORMAT);
        this.limit_preset = data.getInt(Key.LIMIT_PRESET);
        this.configurable = data.getBoolean(Key.CONFIGURABLE);
        
        JSONArray chilren_array = data.getJSONArray(Key.CHILDREN);
        for (int i = 0; i < chilren_array.length(); i++) {
            this.children.add(guild.getVoiceChannelById(chilren_array.getLong(i)));
            this.children.removeAll(Collections.singleton(null));
        }
        
        return this;
    }

    @Override
    public JSONObject compileToJSON() {
        JSONObject compiledData = new JSONObject();

        if (Check.isValidChannel(channel)) {
            compiledData.put(Key.NAME_FORMAT, this.name_format);
            compiledData.put(Key.LIMIT_PRESET, this.limit_preset);
            compiledData.put(Key.CONFIGURABLE, configurable);

            JSONArray children_data = new JSONArray();
            children.forEach(channel -> {
                if (Check.isValidChannel(channel)) {
                    children_data.put(channel.getIdLong());
                }
            });
            compiledData.put(Key.CHILDREN, children_data);
        }

        return compiledData;
    }

    @Override
    public boolean verify(ReferenceType type) {
        // TODO Auto-generated method stub
        return false;
    }
    
    public List<VoiceChannel> getChildren() {
        return this.children;
    }
    
    public Join2CreateChannelData setChildren(List<VoiceChannel> children) {
        DataTools.setList(this.children, children);
        return this;
    }
    
    public Join2CreateChannelData addChildren(VoiceChannel... childrens) {
        DataTools.addToList(this.children, childrens);
        return this;
    }
    
    public Join2CreateChannelData removeChildren(int... indicies) {
        DataTools.removeIndiciesFromList(this.children, indicies);
        return this;
    }
    
    public Join2CreateChannelData removeChildrenByChannel(VoiceChannel... childrens) {
        DataTools.removeValuesFromList(this.children, childrens);
        return this;
    }
    
    public boolean isParentOf(VoiceChannel children) {
        return this.children.contains(children);
    }
    
    public Guild getGuild() {
        return this.guild;
    }

    public VoiceChannel getVoiceChannel() {
        return this.channel;
    }
    
    public long getVoiceChannelId() {
        return this.channel.getIdLong();
    }
    
    public String getNameFormat() {
        return this.name_format;
    }
    
    public Join2CreateChannelData setNameFormat(String name_format) {
        this.name_format = name_format;
        return this;
    }
    
    public int getLimitPreset() {
        return this.limit_preset;
    }
    
    public Join2CreateChannelData setLimitPreset(Integer limit_preset) {
        if (limit_preset == null || limit_preset <= 0) {
            this.limit_preset = -1;
        } else {
            this.limit_preset = limit_preset;
        }
        return this;
    }
    
    public boolean isConfigurable() {
        return this.configurable;
    }
    
    public Join2CreateChannelData setConfigurable(boolean configurable) {
        this.configurable = configurable;
        return this;
    }
    
 /* TODO Transfer & Adapt tool methods from 'EventProcessor.class' to here
  * This includes....
  * 1. Creation of a new J2C-Channel incl. Formatting
  * 2. Deletion of an old J2C-Channel including the...
  * 3. Processing of remaining J2C-Channels  
  */
    
    private static abstract class Key {
        public static final String NAME_FORMAT = "name_format";
        public static final String LIMIT_PRESET = "limit_preset";
        public static final String CONFIGURABLE = "configurable";
        public static final String CHILDREN = "children";
    }

    public List<Long> getChildrenIds() {
        // TODO Auto-generated method stub
        return null;
    }
}