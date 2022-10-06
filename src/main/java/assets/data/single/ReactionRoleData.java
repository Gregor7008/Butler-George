package assets.data.single;

import org.json.JSONObject;

import assets.data.DataContainer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class ReactionRoleData implements DataContainer {

	public ReactionRoleData(Guild guild, JSONObject rawData) {

	}

    @Override
    public JSONObject compileToJSON() {
        return null;
    }

    public TextChannel getChannel() {
        return null;
    }

    public Message getMessage() {
        return null;
    }
}