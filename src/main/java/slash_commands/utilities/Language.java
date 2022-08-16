package slash_commands.utilities;

import base.assets.AwaitTask;
import base.engines.LanguageEngine;
import base.engines.configs.ConfigLoader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import slash_commands.assets.SlashCommandEventHandler;

public class Language implements SlashCommandEventHandler {

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		SelectMenu menu = SelectMenu.create("sellang")
				.setPlaceholder("Select your language")
				.setRequiredRange(1, 1)
				.addOption("English", "en")
				.addOption("Deutsch", "de")
				.addOption("Español", "es")
				.addOption("Français", "fr")
				.addOption("Dutch", "nl")
				.build();
		InteractionHook reply = event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "chooselang"))
				.addActionRow(menu)
				.complete();
		AwaitTask.forSelectMenuInteraction(guild, user, reply.retrieveOriginal().complete(),
				e -> {e.editSelectMenu(menu.asDisabled()).queue();
					  switch (e.getSelectedOptions().get(0).getValue()) {
				      	case "en":
				      		ConfigLoader.INSTANCE.getMemberConfig(guild, user).put("language", "en");
				      		e.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "successen")).queue();
				      		break;
				      	case "de":
				      		ConfigLoader.INSTANCE.getMemberConfig(guild, user).put("language", "de");
				      		e.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "successde")).queue();
				      		break;
				      	case "es":
				      		ConfigLoader.INSTANCE.getMemberConfig(guild, user).put("language", "es");
				      		e.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "successes")).queue();
				      		break;
				      	case "fr":
				      		ConfigLoader.INSTANCE.getMemberConfig(guild, user).put("language", "fr");
				      		e.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "successfr")).queue();
				      		break;
				      	case "nl":
				      		ConfigLoader.INSTANCE.getMemberConfig(guild, user).put("language", "nl");
				      		e.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "successnl")).queue();
				      		break;
				      	default:
				      		e.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "fatal")).queue();
						}
					})
		.addValidComponents(menu.getId()).append();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("language", "Sets your preferred language in which the bot should answer you in on this server");
		command.setDefaultPermissions(DefaultMemberPermissions.ENABLED)
		   	   .setGuildOnly(true);
		return command;
	}

	@Override
	public boolean checkBotPermissions(SlashCommandInteractionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAvailableTo(Member member) {
		// TODO Auto-generated method stub
		return false;
	}
}