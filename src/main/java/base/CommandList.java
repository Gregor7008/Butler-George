package base;

import java.util.concurrent.ConcurrentHashMap;

import commands.moderation.Rolecheck;
import commands.moderation.Rolesorting;
import commands.music.Play;
import commands.utilities.Embed;

public class CommandList {
	
public ConcurrentHashMap<String, Commands> CommandList= new ConcurrentHashMap<String, Commands>();
	
	public CommandList () {
		//Utilities
		this.CommandList.put("embed", new Embed());
		
		//Moderation
		this.CommandList.put("role-check", new Rolecheck());
		this.CommandList.put("role-sort", new Rolesorting());
		
		//Music
		this.CommandList.put("play", new Play());
	}

}
