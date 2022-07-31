package slash_commands;

import java.util.concurrent.ConcurrentHashMap;

import slash_commands.administration.Configure;
import slash_commands.administration.Rolesorting;
import slash_commands.administration.Webhook;
import slash_commands.assets.CommandEventHandler;
import slash_commands.moderation.Clear;
import slash_commands.moderation.Move;
import slash_commands.moderation.Warning;
import slash_commands.music.Nowplaying;
import slash_commands.music.Play;
import slash_commands.music.Queue;
import slash_commands.music.Skip;
import slash_commands.music.Stop;
import slash_commands.utilities.Channelpermission;
import slash_commands.utilities.CreateChannel;
import slash_commands.utilities.Embed;
import slash_commands.utilities.Language;
import slash_commands.utilities.Leave;
import slash_commands.utilities.Level;
import slash_commands.utilities.Levelbackground;
import slash_commands.utilities.PingAndMove;
import slash_commands.utilities.Report;
import slash_commands.utilities.Serverinfo;
import slash_commands.utilities.Suggest;
import slash_commands.utilities.Userinfo;

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