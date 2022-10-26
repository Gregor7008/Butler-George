package assets.data.single;

import org.json.JSONObject;

import assets.data.DataContainer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

public class Join2CreateChannelData implements DataContainer {

    private final Guild guild;
    private String name;
    private int limit;
    private boolean configurable;
    
	public Join2CreateChannelData(Guild guild, JSONObject data) {
	    this.guild = guild;
	    this.instanciateFromJSON(data);
	}
	
	public Join2CreateChannelData(Guild guild) {
	    this.guild = guild;
	}

    @Override
    public DataContainer instanciateFromJSON(JSONObject data) {
        return this;
    }

    @Override
    public JSONObject compileToJSON() {
        return null;
    }

    public VoiceChannel getVoiceChannel() {
        return null;
    }
}