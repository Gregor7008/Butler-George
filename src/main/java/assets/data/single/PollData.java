package assets.data.single;

import org.json.JSONObject;

import assets.data.DataContainer;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class PollData implements DataContainer {

	public PollData(JSONObject data) {
	    this.instanciateFromJSON(data);
	}
	
	public PollData() {}

    @Override
    public DataContainer instanciateFromJSON(JSONObject data) {
        return this;
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