package commands.utilities;

import java.io.File;
import java.net.MalformedURLException;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Levelbackground implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		if (event.getSubcommandName().equals("set")) {
			Level lv = new Level();
			if (Integer.parseInt(event.getOption("number").getAsString()) > 4 || Integer.parseInt(event.getOption("number").getAsString()) < 0) {
				event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/utilities/levelbackground:wrongarg")).queue();
			} else {
				Configloader.INSTANCE.setUserConfig(event.getMember(), "levelbackground", event.getOption("number").getAsString());
				event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/utilities/levelbackground:success")).queue();
				event.getChannel().sendMessage("").addFile(lv.renderLevelcard(event.getMember())).queue();
			}
			return;
		}
		if (event.getSubcommandName().equals("list")) {
			this.listlevelrewards(event);
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("levelbackground", "Configure your personal levelbackground")
									.addSubcommands(new SubcommandData("set", "Set your new levelbackground").addOption(OptionType.INTEGER, "number", "The number of your new levelbackground", true))
									.addSubcommands(new SubcommandData("list", "List all possible backgrounds"));
		return command;
	}

	@Override
	public String getHelp() {
		return "Configure your personal levelbackground so it will be displayed, whenever you use /level";
	}
	
	private void listlevelrewards(SlashCommandEvent event) {
		EmbedBuilder eb0 = new EmbedBuilder();
		EmbedBuilder eb1 = new EmbedBuilder();
		EmbedBuilder eb2 = new EmbedBuilder();
		EmbedBuilder eb3 = new EmbedBuilder();
		EmbedBuilder eb4 = new EmbedBuilder();
		
		eb0.setTitle("Levelbackground no. 0");
		eb1.setTitle("Levelbackground no. 1");
		eb2.setTitle("Levelbackground no. 2");
		eb3.setTitle("Levelbackground no. 3");
		eb4.setTitle("Levelbackground no. 4");
		
		eb0.setFooter("Official NoLimits-Bot! - discord.gg/qHA2vUs");
		eb1.setFooter("Official NoLimits-Bot! - discord.gg/qHA2vUs");
		eb2.setFooter("Official NoLimits-Bot! - discord.gg/qHA2vUs");
		eb3.setFooter("Official NoLimits-Bot! - discord.gg/qHA2vUs");
		eb4.setFooter("Official NoLimits-Bot! - discord.gg/qHA2vUs");
		
		try {
			eb0.setImage(new File(Bot.INSTANCE.getBotConfig("resourcepath" + "/levelcards/0.png")).toURI().toURL().toString());
			eb1.setImage(new File(Bot.INSTANCE.getBotConfig("resourcepath" + "/levelcards/1.png")).toURI().toURL().toString());
			eb2.setImage(new File(Bot.INSTANCE.getBotConfig("resourcepath" + "/levelcards/2.png")).toURI().toURL().toString());
			eb3.setImage(new File(Bot.INSTANCE.getBotConfig("resourcepath" + "/levelcards/3.png")).toURI().toURL().toString());
			eb4.setImage(new File(Bot.INSTANCE.getBotConfig("resourcepath" + "/levelcards/4.png")).toURI().toURL().toString());
		} catch (MalformedURLException e) {}
		
		event.replyEmbeds(eb0.build(), eb1.build(), eb2.build(), eb3.build(), eb4.build()).queue();
	}
}
