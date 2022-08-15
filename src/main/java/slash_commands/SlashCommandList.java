package slash_commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
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

public abstract class SlashCommandList {

	private static final HashMap<String, SlashCommandEventHandler> handlers = new HashMap<>();
	private static final HashMap<String, CommandData> data = new HashMap<>();
	
	public static void create() {
		//Administration
		add(new Configure());
		add(new Rolesorting());
		add(new Webhook());
		
		//Moderation
		add(new Clear());
		add(new Move());
		add(new Warning());
		
		//Music
		add(new Nowplaying());
		add(new Play());
		add(new Queue());
		add(new Skip());
		add(new Stop());

		//Utilities
		add(new Embed());
		add(new Level());
		add(new Levelbackground());
		add(new Suggest());
		add(new Userinfo());
		add(new Report());
		add(new Serverinfo());
		add(new Language());
		add(new CreateChannel());
		add(new Channelpermission());
		add(new PingAndMove());
		add(new Leave());
	}
	
	public static void add(SlashCommandEventHandler handler) {
		CommandData commandData = handler.initialize();
		String name = commandData.getName();
		handlers.put(name, handler);
		data.put(name, commandData);
	}
	
	public static void remove(String name) {
		handlers.remove(name);
		data.remove(name);
	}
	
	public static HashMap<String, SlashCommandEventHandler> getHandlers() {
		return handlers;
	}
	
	public static SlashCommandEventHandler getHandler(String name) {
		return handlers.get(name);
	}
	
	public static List<CommandData> getCommandData() {
		List<CommandData> commandDataList =  new ArrayList<>();
		commandDataList.addAll(data.values());
		return commandDataList;
	}
	
	public static CommandData getCommandData(String name) {
		return data.get(name);
	}
}