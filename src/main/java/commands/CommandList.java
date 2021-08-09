package commands;

import java.util.concurrent.ConcurrentHashMap;

import commands.utilities.Embed;
import components.Test;

public class CommandList {
	
	public ConcurrentHashMap<String, Command> commands = new ConcurrentHashMap<>();
	
	public CommandList() {
		//Utilities
		this.commands.put("embed", new Embed());
		
		//Moderation
		
		//Music
		
		//Developement
		this.commands.put("test", new Test());
	}

}
