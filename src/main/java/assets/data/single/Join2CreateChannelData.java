package assets.data.single;

import org.json.JSONObject;

import assets.data.DataContainer;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

public class Join2CreateChannelData implements DataContainer {

	public Join2CreateChannelData(JSONObject data) {
	    this.instanciateFromJSON(data);
	}
	
	public Join2CreateChannelData() {}

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