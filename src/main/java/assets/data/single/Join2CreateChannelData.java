package assets.data.single;

import org.json.JSONObject;

import assets.data.DataContainer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

public class Join2CreateChannelData implements DataContainer {

	public Join2CreateChannelData(Guild guild, JSONObject rawData) {

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