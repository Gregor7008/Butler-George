package assets.data.single;

import org.json.JSONObject;

import assets.data.DataContainer;
import net.dv8tion.jda.api.entities.Guild;

public class PenaltyData implements DataContainer {

	public PenaltyData(Guild guild, JSONObject rawData) {

	}

    @Override
    public JSONObject compileToJSON() {
        return null;
    }

    public int getWarningCount() {
        return 0;
    }
}