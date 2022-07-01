package commands;

import java.util.concurrent.ConcurrentHashMap;

import commands.administration.Rolesorting;
import commands.administration.Webhook;
import commands.moderation.Clear;
import commands.moderation.Move;
import commands.moderation.Warning;
import commands.administration.Configure;
import commands.music.Nowplaying;
import commands.music.Play;
import commands.music.Queue;
import commands.music.Skip;
import commands.music.Stop;
import commands.utilities.Channelpermission;
import commands.utilities.CreateChannel;
import commands.utilities.Embed;
import commands.utilities.Language;
import commands.utilities.Leave;
import commands.utilities.Level;
import commands.utilities.Levelbackground;
import commands.utilities.PingAndMove;
import commands.utilities.Report;
import commands.utilities.Serverinfo;
import commands.utilities.Suggest;
import commands.utilities.Userinfo;
import components.commands.CommandEventHandler;

public class CommandList {

	public ConcurrentHashMap<String, CommandEventHandler> commandEventHandlers = new ConcurrentHashMap<String, CommandEventHandler>();
	
	public CommandList() {
		//Administration
		this.commandEventHandlers.put("clear", new Clear());
		this.commandEventHandlers.put("rolesort", new Rolesorting());
		this.commandEventHandlers.put("move", new Move());
		this.commandEventHandlers.put("warning", new Warning());
		this.commandEventHandlers.put("configure", new Configure());
		
		//Music
		this.commandEventHandlers.put("nowplaying", new Nowplaying());
		this.commandEventHandlers.put("play", new Play());
		this.commandEventHandlers.put("queue", new Queue());
		this.commandEventHandlers.put("skip", new Skip());
		this.commandEventHandlers.put("stop", new Stop());
		
		//Utilities
		this.commandEventHandlers.put("embed", new Embed());
		this.commandEventHandlers.put("level", new Level());
		this.commandEventHandlers.put("levelbackground", new Levelbackground());
		this.commandEventHandlers.put("suggest", new Suggest());
		this.commandEventHandlers.put("userinfo", new Userinfo());
		this.commandEventHandlers.put("report", new Report());
		this.commandEventHandlers.put("serverinfo", new Serverinfo());
		this.commandEventHandlers.put("language", new Language());
		this.commandEventHandlers.put("webhook", new Webhook());
		this.commandEventHandlers.put("createchannel", new CreateChannel());
		this.commandEventHandlers.put("channelpermission", new Channelpermission());
		this.commandEventHandlers.put("pingandmove", new PingAndMove());
		this.commandEventHandlers.put("leave", new Leave());
	}
}