package components.music;

import java.util.concurrent.ConcurrentHashMap;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import net.dv8tion.jda.api.entities.Guild;

public class PlayerCache {
	
	private ConcurrentHashMap<AudioPlayer, Guild> pc = new ConcurrentHashMap<>();
	private static PlayerCache INSTANCE;
	
	public PlayerCache() {
		
	}
	
	public static PlayerCache getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new PlayerCache();
		}
		return INSTANCE;
	}
	
	public void addEntry(AudioPlayer ap, Guild guild) {
		pc.put(ap, guild);
	}
	
	public Guild getGuild(AudioPlayer ap) {
		return pc.get(ap);
	}
	
	public void clearMap() {
		pc.clear();
	}
}
