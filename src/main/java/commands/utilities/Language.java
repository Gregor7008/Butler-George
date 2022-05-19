package commands.utilities;

import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.ConfigLoader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

public class Language implements Command{

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
				.addOption("Pусский", "ru")
				.build();
		InteractionHook reply = event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/utilities/language:chooselang").convert())
				.addActionRow(menu)
				.complete();
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		waiter.waitForEvent(SelectMenuInteractionEvent.class,
				e -> {if(!e.getChannel().getId().equals(event.getChannel().getId())) {return false;} 
				  	  return e.getUser().getIdLong() == user.getIdLong();},
				e -> {reply.deleteOriginal().queue();
					  switch (e.getSelectedOptions().get(0).getValue()) {
				      case "en":
				    	  ConfigLoader.run.setUserConfig(guild, user, "language", "en");
				    	  e.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/utilities/language:successen").convert()).queue();
				    	  break;
				      case "de":
				    	  ConfigLoader.run.setUserConfig(guild, user, "language", "de");
				    	  e.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/utilities/language:successde").convert()).queue();
				    	  break;
				      case "es":
				    	  ConfigLoader.run.setUserConfig(guild, user, "language", "es");
				    	  e.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/utilities/language:successes").convert()).queue();
				    	  break;
				      case "fr":
				    	  ConfigLoader.run.setUserConfig(guild, user, "language", "fr");
				    	  e.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/utilities/language:successfr").convert()).queue();
				    	  break;
				      case "nl":
				    	  ConfigLoader.run.setUserConfig(guild, user, "language", "nl");
				    	  e.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/utilities/language:successnl").convert()).queue();
				    	  break;
				      case "ru":
				    	  ConfigLoader.run.setUserConfig(guild, user, "language", "ru");
				    	  e.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/utilities/language:successru").convert()).queue();
				    	  break;
				      default:
						  e.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"general:fatal").convert()).queue();
				      }},
				1, TimeUnit.MINUTES,
				() -> {event.getChannel().sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"general:timeout").convert()).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("language", "Sets your preferred language in which the bot should answer you in on this server");
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/utilities/language:help");
	}
}