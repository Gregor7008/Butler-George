package miscellaneous;

import java.util.HashMap;

import miscellaneous.assets.ServerUtils;
import miscellaneous.server_utils.FunAndGamesUtils;

public abstract class ServerUtilsList {
	
	private static final HashMap<Long, ServerUtils> engines = new HashMap<>();
	
	public static void create() {
		add(new FunAndGamesUtils());
	}
	
	public static void add(ServerUtils utils) {
		engines.put(utils.getGuildId(), utils);
	}
	
	public static void remove(long guildId) {
		engines.remove(guildId);
	}
	
	public static HashMap<Long, ServerUtils> getEngines() {
		return engines;
	}
	
	public static ServerUtils getEngine(long guildId) {
		return engines.get(guildId);
	}
}