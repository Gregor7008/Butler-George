package commands.utilities;

import java.util.List;

import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.commands.CommandEventHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Levelbackground implements CommandEventHandler {

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (event.getSubcommandName().equals("set")) {
			Level lv = new Level();
			if (Integer.parseInt(event.getOption("number").getAsString()) > 4 || Integer.parseInt(event.getOption("number").getAsString()) < 0) {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "wrongarg").convert()).queue();
			} else {
				ConfigLoader.getMemberConfig(guild, user).put("levelbackground", event.getOption("number").getAsInt());
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "success").convert()).addFile(lv.renderLevelcard(user, guild)).queue();
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
		
		try {
			eb0.setImage("https://i.ibb.co/J72srcG/image.png");
			eb1.setImage("https://i.ibb.co/HNgMsQT/1.png");
			eb2.setImage("https://i.ibb.co/GttGfPZ/2.png");
			eb3.setImage("https://i.ibb.co/r3rHJzV/3.png");
			eb4.setImage("https://i.ibb.co/jDwc9PG/4.png");
		} catch (IllegalArgumentException e) {}
		
		event.replyEmbeds(eb0.build(), eb1.build(), eb2.build(), eb3.build(), eb4.build()).queue();
	}

	@Override
	public List<Role> additionalWhitelistedRoles(Guild guild) {
		return null;
	}
}