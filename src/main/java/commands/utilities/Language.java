package commands.utilities;

import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.commands.Command;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

public class Language implements Command {

	@Override
	public void perform(SlashCommandInteractionEvent event) {
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
		InteractionHook reply = event.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/utilities/language:chooselang").convert())
				.addActionRow(menu)
				.complete();
		EventWaiter waiter = Bot.run.getWaiter();
		waiter.waitForEvent(SelectMenuInteractionEvent.class,
				e -> {if(!e.getChannel().getId().equals(event.getChannel().getId())) {return false;} 
				  	  return e.getUser().getIdLong() == user.getIdLong();},
				e -> {ActionRow newRow = ActionRow.of(menu.asDisabled());
					  reply.editOriginalComponents(newRow).queue();
					  switch (e.getSelectedOptions().get(0).getValue()) {
				      	case "en":
				      		ConfigLoader.getMemberConfig(guild, user).put("language", "en");
				      		e.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/utilities/language:successen").convert()).queue();
				      		break;
				      	case "de":
				      		ConfigLoader.getMemberConfig(guild, user).put("language", "de");
				      		e.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/utilities/language:successde").convert()).queue();
				      		break;
				      	case "es":
				      		ConfigLoader.getMemberConfig(guild, user).put("language", "es");
				      		e.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/utilities/language:successes").convert()).queue();
				      		break;
				      	case "fr":
				      		ConfigLoader.getMemberConfig(guild, user).put("language", "fr");
				      		e.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/utilities/language:successfr").convert()).queue();
				      		break;
				      	case "nl":
				      		ConfigLoader.getMemberConfig(guild, user).put("language", "nl");
				      		e.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/utilities/language:successnl").convert()).queue();
				      		break;
				      	default:
				      		e.replyEmbeds(LanguageEngine.fetchMessage(guild, user,"general:fatal").convert()).queue();
						}
					},
				1, TimeUnit.MINUTES,
				() -> {ActionRow newRow = ActionRow.of(menu.asDisabled());
				  	   reply.editOriginalComponents(newRow).queue();
					   event.getChannel().sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user,"general:timeout").convert()).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("language", "Sets your preferred language in which the bot should answer you in on this server");
		return command;
	}

	@Override
	public boolean canBeAccessedBy(Member member) {
		return true;
	}
}