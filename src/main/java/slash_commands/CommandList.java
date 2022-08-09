package slash_commands;

import java.util.concurrent.ConcurrentHashMap;

import slash_commands.administration.Configure;
import slash_commands.administration.Rolesorting;
import slash_commands.administration.Webhook;
import slash_commands.assets.SlashCommandEventHandler;
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

	public ConcurrentHashMap<String, SlashCommandEventHandler> slashCommandEventHandlers = new ConcurrentHashMap<String, SlashCommandEventHandler>();
	
	public CommandList() {
		//Administration
		this.slashCommandEventHandlers.put("clear", new Clear());
		this.slashCommandEventHandlers.put("rolesort", new Rolesorting());
		this.slashCommandEventHandlers.put("move", new Move());
		this.slashCommandEventHandlers.put("warning", new Warning());
		this.slashCommandEventHandlers.put("configure", new Configure());
		
		//Music
		this.slashCommandEventHandlers.put("nowplaying", new Nowplaying());
		this.slashCommandEventHandlers.put("play", new Play());
		this.slashCommandEventHandlers.put("queue", new Queue());
		this.slashCommandEventHandlers.put("skip", new Skip());
		this.slashCommandEventHandlers.put("stop", new Stop());
		
		//Utilities
		this.slashCommandEventHandlers.put("embed", new Embed());
		this.slashCommandEventHandlers.put("level", new Level());
		this.slashCommandEventHandlers.put("levelbackground", new Levelbackground());
		this.slashCommandEventHandlers.put("suggest", new Suggest());
		this.slashCommandEventHandlers.put("userinfo", new Userinfo());
		this.slashCommandEventHandlers.put("report", new Report());
		this.slashCommandEventHandlers.put("serverinfo", new Serverinfo());
		this.slashCommandEventHandlers.put("language", new Language());
		this.slashCommandEventHandlers.put("webhook", new Webhook());
		this.slashCommandEventHandlers.put("createchannel", new CreateChannel());
		this.slashCommandEventHandlers.put("channelpermission", new Channelpermission());
		this.slashCommandEventHandlers.put("pingandmove", new PingAndMove());
		this.slashCommandEventHandlers.put("leave", new Leave());
	}
}