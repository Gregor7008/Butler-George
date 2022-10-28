package functions.slash_commands.utilities;

import assets.base.AwaitTask;
import assets.functions.SlashCommandEventHandler;
import engines.base.LanguageEngine;
import engines.data.ConfigLoader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

public class Language implements SlashCommandEventHandler {

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		StringSelectMenu menu = StringSelectMenu.create("sellang")
				.setPlaceholder("Select your language")
				.setRequiredRange(1, 1)
				.addOption("English", "en", Emoji.fromUnicode("\uD83C\uDDEC\uD83C\uDDE7"))
				.addOption("Deutsch", "de", Emoji.fromUnicode("\uD83C\uDDE9\uD83C\uDDEA"))
				.addOption("Español", "es", Emoji.fromUnicode("\uD83C\uDDEA\uD83C\uDDF8"))
				.addOption("Français", "fr", Emoji.fromUnicode("\uD83C\uDDEB\uD83C\uDDF7"))
				.addOption("Dutch", "nl", Emoji.fromUnicode("\uD83C\uDDF3\uD83C\uDDF1"))
				.build();
		InteractionHook reply = event.replyEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "chooselang"))
				.addActionRow(menu)
				.complete();
		AwaitTask.forStringSelectInteraction(guild, user, reply.retrieveOriginal().complete(),
				e -> {e.editSelectMenu(menu.asDisabled()).queue();
					  switch (e.getSelectedOptions().get(0).getValue()) {
				      	case "en":
				      		ConfigLoader.INSTANCE.getMemberData(guild, user).setLanguage(LanguageEngine.Language.ENGLISH);
				      		e.replyEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "successen")).queue();
				      		break;
				      	case "de":
				      		ConfigLoader.INSTANCE.getMemberData(guild, user).setLanguage(LanguageEngine.Language.GERMAN);
				      		e.replyEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "successde")).queue();
				      		break;
				      	case "es":
				      		ConfigLoader.INSTANCE.getMemberData(guild, user).setLanguage(LanguageEngine.Language.SPANISH);
				      		e.replyEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "successes")).queue();
				      		break;
				      	case "fr":
				      		ConfigLoader.INSTANCE.getMemberData(guild, user).setLanguage(LanguageEngine.Language.FRENCH);
				      		e.replyEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "successfr")).queue();
				      		break;
				      	case "nl":
				      		ConfigLoader.INSTANCE.getMemberData(guild, user).setLanguage(LanguageEngine.Language.DUTCH);
				      		e.replyEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "successnl")).queue();
				      		break;
				      	default:
				      		e.replyEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "fatal")).queue();
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
}