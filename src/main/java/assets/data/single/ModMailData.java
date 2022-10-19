package assets.data.single;

import org.json.JSONObject;

import assets.data.DataContainer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class ModMailData implements DataContainer {

	public ModMailData(Guild guild, JSONObject rawData) {

	}

    @Override
    public DataContainer instanciateFromJSON(JSONObject data) {
        return this;
    }

    @Override
    public JSONObject compileToJSON() {
        return null;
    }

    public TextChannel getGuildChannel() {
        return null;
    }
}