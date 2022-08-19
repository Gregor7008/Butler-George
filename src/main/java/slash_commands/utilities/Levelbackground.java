package slash_commands.utilities;

import java.io.File;

import base.engines.LanguageEngine;
import base.engines.configs.ConfigLoader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import slash_commands.assets.SlashCommandEventHandler;

public class Levelbackground implements SlashCommandEventHandler {

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (event.getSubcommandName().equals("set")) {
			Level lv = new Level();
			if (Integer.parseInt(event.getOption("number").getAsString()) > 4 || Integer.parseInt(event.getOption("number").getAsString()) < 0) {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "wrongarg")).queue();
			} else {
				ConfigLoader.INSTANCE.getMemberConfig(guild, user).put("levelbackground", event.getOption("number").getAsInt());
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "success")).addFile(lv.renderLevelcard(user, guild)).queue();
			}
			return;
		}
		if (event.getSubcommandName().equals("list")) {
			this.listlevelcards(event);
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("levelbackground", "Configure your personal levelbackground")
									  .addSubcommands(new SubcommandData("set", "Set your new levelbackground")
											  					.addOption(OptionType.INTEGER, "number", "The number of your new levelbackground", true),
													  new SubcommandData("list", "List all possible backgrounds"));
		command.setDefaultPermissions(DefaultMemberPermissions.ENABLED)
		   .setGuildOnly(true);
		return command;
	}
	
	private void listlevelcards(SlashCommandInteractionEvent event) {
		EmbedBuilder eb0 = new EmbedBuilder();
		EmbedBuilder eb1 = new EmbedBuilder();
		EmbedBuilder eb2 = new EmbedBuilder();
		EmbedBuilder eb3 = new EmbedBuilder();
		EmbedBuilder eb4 = new EmbedBuilder();
		
		eb0.setTitle("Levelbackground 0 (default)");
		eb1.setTitle("Levelbackground 1");
		eb2.setTitle("Levelbackground 2");
		eb3.setTitle("Levelbackground 3");
		eb4.setTitle("Levelbackground 4");
		
		eb0.setColor(LanguageEngine.color);
		eb1.setColor(LanguageEngine.color);
		eb2.setColor(LanguageEngine.color);
		eb3.setColor(LanguageEngine.color);
		eb4.setColor(LanguageEngine.color);
		
		try {
			File file0 = new File(this.getClass().getClassLoader().getResource("levelcards/0.png").toURI());
			File file1 = new File(this.getClass().getClassLoader().getResource("levelcards/1.png").toURI());
			File file2 = new File(this.getClass().getClassLoader().getResource("levelcards/2.png").toURI());
			File file3 = new File(this.getClass().getClassLoader().getResource("levelcards/3.png").toURI());
			File file4 = new File(this.getClass().getClassLoader().getResource("levelcards/4.png").toURI());
			
			eb0.setImage("attachment://0.png");
			eb1.setImage("attachment://1.png");
			eb2.setImage("attachment://2.png");
			eb3.setImage("attachment://3.png");
			eb4.setImage("attachment://4.png");
			event.replyEmbeds(eb0.build(), eb1.build(), eb2.build(), eb3.build(), eb4.build())
				.addFile(file0, "0.png")
				.addFile(file1, "1.png")
				.addFile(file2, "2.png")
				.addFile(file3, "3.png")
				.addFile(file4, "4.png")
				.queue();
			return;
		} catch (Exception e) {}
		event.replyEmbeds(LanguageEngine.fetchMessage(null, null, null, "fatal")).queue();
	}
}