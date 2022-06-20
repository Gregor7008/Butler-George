package commands;

import java.util.concurrent.ConcurrentHashMap;

import commands.administration.Clear;
import commands.administration.Move;
import commands.administration.Rolesorting;
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
import commands.utilities.Webhook;
import components.commands.Command;

public class CommandList {

	public ConcurrentHashMap<String, Command> commands = new ConcurrentHashMap<>();
	
	public CommandList() {
		//Utilities
		this.commands.put("embed", new Embed());
		this.commands.put("level", new Level());
		this.commands.put("levelbackground", new Levelbackground());
		this.commands.put("suggest", new Suggest());
		this.commands.put("userinfo", new Userinfo());
		this.commands.put("report", new Report());
		this.commands.put("serverinfo", new Serverinfo());
		this.commands.put("language", new Language());
		this.commands.put("webhook", new Webhook());
		this.commands.put("createchannel", new CreateChannel());
		this.commands.put("channelpermission", new Channelpermission());
		this.commands.put("pingandmove", new PingAndMove());
		this.commands.put("leave", new Leave());
		this.commands.put("clear", new Clear());
		this.commands.put("rolesort", new Rolesorting());
		this.commands.put("move", new Move());
		
		//Music
		this.commands.put("nowplaying", new Nowplaying());
		this.commands.put("play", new Play());
		this.commands.put("queue", new Queue());
		this.commands.put("skip", new Skip());
		this.commands.put("stop", new Stop());
	}
}