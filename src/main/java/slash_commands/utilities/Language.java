package slash_commands.utilities;

import java.util.List;
import java.util.concurrent.TimeUnit;

import base.engines.ConfigLoader;
import base.engines.LanguageEngine;
import base.engines.ResponseDetector;
import base.engines.Toolbox;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import slash_commands.assets.CommandEventHandler;

public class Language implements CommandEventHandler {

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
		InteractionHook reply = event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "chooselang").convert())
				.addActionRow(menu)
				.complete();
		ResponseDetector.waitForMenuSelection(guild, user, reply.retrieveOriginal().complete(), menu,
				e -> {e.editSelectMenu(menu.asDisabled()).queue();
					  switch (e.getSelectedOptions().get(0).getValue()) {
				      	case "en":
				      		ConfigLoader.INSTANCE.getMemberConfig(guild, user).put("language", "en");
				      		e.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "successen").convert()).queue();
				      		break;
				      	case "de":
				      		ConfigLoader.INSTANCE.getMemberConfig(guild, user).put("language", "de");
				      		e.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "successde").convert()).queue();
				      		break;
				      	case "es":
				      		ConfigLoader.INSTANCE.getMemberConfig(guild, user).put("language", "es");
				      		e.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "successes").convert()).queue();
				      		break;
				      	case "fr":
				      		ConfigLoader.INSTANCE.getMemberConfig(guild, user).put("language", "fr");
				      		e.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "successfr").convert()).queue();
				      		break;
				      	case "nl":
				      		ConfigLoader.INSTANCE.getMemberConfig(guild, user).put("language", "nl");
				      		e.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "successnl").convert()).queue();
				      		break;
				      	default:
				      		e.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "fatal").convert()).queue();
						}
					},
				() -> {Toolbox.disableActionRows(reply.retrieveOriginal().complete());
					   event.getChannel().sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "timeout").convert()).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("language", "Sets your preferred language in which the bot should answer you in on this server");
		command.setDefaultPermissions(DefaultMemberPermissions.ENABLED)
		   	   .setGuildOnly(true);
		return command;
	}

	@Override
	public List<Role> additionalWhitelistedRoles(Guild guild) {
		return null;
	}
}