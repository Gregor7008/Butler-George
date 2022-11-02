package functions.guild_utilities;

import java.util.HashMap;

import assets.functions.GuildUtilities;

public abstract class GuildUtilitiesList {
	
	private static final HashMap<Long, GuildUtilities> engines = new HashMap<>();
	
	public static void create() {
		add(new FunAndGames_Utilities());
	}
	
	public static void add(GuildUtilities utils) {
		engines.put(utils.getGuildId(), utils);
	}
	
	public static void remove(long guildId) {
		engines.remove(guildId);
	}
	
	public static HashMap<Long, GuildUtilities> getEngines() {
		return engines;
	}
	
	public static GuildUtilities getEngine(long guildId) {
		return engines.get(guildId);
	}
}