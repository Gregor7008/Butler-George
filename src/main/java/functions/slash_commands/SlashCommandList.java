package functions.slash_commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import assets.functions.SlashCommandEventHandler;
import functions.slash_commands.administration.Configure;
import functions.slash_commands.administration.Rolesorting;
import functions.slash_commands.administration.Webhook;
import functions.slash_commands.moderation.Clear;
import functions.slash_commands.moderation.Move;
import functions.slash_commands.moderation.Warning;
import functions.slash_commands.music.Play;
import functions.slash_commands.music.Queue;
import functions.slash_commands.music.Skip;
import functions.slash_commands.music.Stop;
import functions.slash_commands.support.Modmail;
import functions.slash_commands.support.Report;
import functions.slash_commands.support.Suggest;
import functions.slash_commands.utilities.Embed;
import functions.slash_commands.utilities.Language;
import functions.slash_commands.utilities.PingAndMove;
import functions.slash_commands.utilities.Serverinfo;
import functions.slash_commands.utilities.Userinfo;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

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
		add(new Play());
		add(new Queue());
		add(new Skip());
		add(new Stop());

		//Support
		add(new Report());
		add(new Suggest());
		add(new Modmail());
		
		//Utilities
		add(new Embed());
		add(new Userinfo());
		add(new Serverinfo());
		add(new Language());
		add(new PingAndMove());
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