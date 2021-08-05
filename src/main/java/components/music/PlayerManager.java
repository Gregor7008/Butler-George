package components.music;

import java.util.concurrent.ConcurrentHashMap;

import base.Bot;

public class PlayerManager {
	
	public ConcurrentHashMap<Long, MusicController> controller;
	
	public PlayerManager() {
		this.controller = new ConcurrentHashMap<Long, MusicController>();
	}
	
	public MusicController getController(long guildid) {
		MusicController mc = null;
		if(this.controller.containsKey(guildid)) {
			mc = this.controller.get(guildid);
		} else {
			mc = new MusicController(Bot.INSTANCE.jda.getGuildById(guildid));
			this.controller.put(guildid, mc);
		}
		return mc;
	}

}
