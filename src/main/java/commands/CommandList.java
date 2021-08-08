package commands;

import java.util.concurrent.ConcurrentHashMap;

import commands.moderation.Autorole;
import commands.moderation.Clear;
import commands.moderation.Rolecheck;
import commands.moderation.Rolesorting;
import commands.music.Nowplaying;
import commands.music.Play;
import commands.music.Queue;
import commands.music.Skip;
import commands.music.Stop;
import commands.utilities.Embed;

public class CommandList {
	
public ConcurrentHashMap<String, Commands> CommandList= new ConcurrentHashMap<String, Commands>();
	
	public CommandList () {
		//Utilities
		this.CommandList.put("embed", new Embed());
		
		//Moderation
		this.CommandList.put("role-check", new Rolecheck());
		this.CommandList.put("role-sort", new Rolesorting());
		this.CommandList.put("clear", new Clear());
		this.CommandList.put("autorole", new Autorole());
		
		//Music
		this.CommandList.put("play", new Play());
		this.CommandList.put("stop", new Stop());
		this.CommandList.put("skip", new Skip());
		this.CommandList.put("nowplaying", new Nowplaying());
		this.CommandList.put("queue", new Queue());
	}

}
