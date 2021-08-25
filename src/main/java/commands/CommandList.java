package commands;

import java.util.concurrent.ConcurrentHashMap;

import commands.moderation.Autorole;
import commands.moderation.Clear;
import commands.moderation.Goodbye;
import commands.moderation.Rolecheck;
import commands.moderation.Rolesorting;
import commands.moderation.Warning;
import commands.moderation.Welcome;
import commands.music.Nowplaying;
import commands.music.Play;
import commands.music.Queue;
import commands.music.Skip;
import commands.music.Stop;
import commands.utilities.Embed;
import commands.utilities.Help;
import commands.utilities.Level;
import components.utilities.Test;

public class CommandList {
	
	public ConcurrentHashMap<String, Command> commands = new ConcurrentHashMap<>();
	
	public CommandList() {
		//Utilities
		this.commands.put("embed", new Embed());
		this.commands.put("help", new Help());
		this.commands.put("level", new Level());
		
		//Moderation
		this.commands.put("autorole", new Autorole());
		this.commands.put("clear", new Clear());
		this.commands.put("rolecheck", new Rolecheck());
		this.commands.put("rolesort", new Rolesorting());
		this.commands.put("welcome", new Welcome());
		this.commands.put("goodbye", new Goodbye());
		this.commands.put("warn", new Warning());
		
		//Music
		this.commands.put("nowplaying", new Nowplaying());
		this.commands.put("play", new Play());
		this.commands.put("queue", new Queue());
		this.commands.put("skip", new Skip());
		this.commands.put("stop", new Stop());
		
		//Developement
		this.commands.put("test", new Test());
	}

}
