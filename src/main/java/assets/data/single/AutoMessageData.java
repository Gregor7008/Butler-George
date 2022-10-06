package assets.data.single;

import org.json.JSONObject;

import assets.data.DataContainer;
import net.dv8tion.jda.api.entities.Guild;

public class AutoMessageData implements DataContainer {

	public AutoMessageData(Guild guild, JSONObject rawData) {
		
	}

    @Override
    public JSONObject compileToJSON() {
        return null;
    }
}