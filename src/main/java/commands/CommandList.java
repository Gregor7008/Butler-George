package commands;

import java.util.concurrent.ConcurrentHashMap;

import commands.moderation.Autopunish;
import commands.moderation.Autorole;
import commands.moderation.Botautorole;
import commands.moderation.Clear;
import commands.moderation.Goodbye;
import commands.moderation.Join2Create;
import commands.moderation.Levelreward;
import commands.moderation.Mute;
import commands.moderation.Rolecheck;
import commands.moderation.Rolesorting;
import commands.moderation.Tempban;
import commands.moderation.Tempmute;
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
import commands.utilities.Levelbackground;
import commands.utilities.Suggest;
import commands.utilities.Userinfo;
import components.utilities.Test;

public class CommandList {
	
	public ConcurrentHashMap<String, Command> commands = new ConcurrentHashMap<>();
	
	public CommandList() {
		//Utilities
		this.commands.put("embed", new Embed());
		this.commands.put("help", new Help());
		this.commands.put("level", new Level());
		this.commands.put("levelbackground", new Levelbackground());
		this.commands.put("suggest", new Suggest());
		this.commands.put("userinfo", new Userinfo());
		
		//Moderation
		this.commands.put("autopunish", new Autopunish());
		this.commands.put("autorole", new Autorole());
		this.commands.put("botautorole", new Botautorole());
		this.commands.put("clear", new Clear());
		this.commands.put("goodbye", new Goodbye());
		this.commands.put("join2create", new Join2Create());
		this.commands.put("levelreward", new Levelreward());
		this.commands.put("mute", new Mute());
		this.commands.put("rolecheck", new Rolecheck());
		this.commands.put("rolesort", new Rolesorting());
		this.commands.put("tempban", new Tempban());
		this.commands.put("tempmute", new Tempmute());
		this.commands.put("warning", new Warning());
		this.commands.put("welcome", new Welcome());
		
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
