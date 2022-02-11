package commands.utilities;

import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

public class Language implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		SelectionMenu menu = SelectionMenu.create("sellang")
				.setPlaceholder("Select your language")
				.setRequiredRange(1, 1)
				.addOption("English", "en")
				.addOption("Deutsch", "de")
				.addOption("Español", "es")
				.addOption("Français", "fr")
				.addOption("Dutch", "nl")
				.addOption("Pусский", "ru")
				.build();
		InteractionHook reply = event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/language:chooselang"))
				.addActionRow(menu)
				.complete();
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		waiter.waitForEvent(SelectionMenuEvent.class,
				e -> {if(!e.getChannel().getId().equals(event.getChannel().getId())) {return false;} 
				  	  return e.getUser().getIdLong() == user.getIdLong();},
				e -> {reply.deleteOriginal().queue();
					  switch (e.getSelectedOptions().get(0).getValue()) {
				      case "en":
				    	  Configloader.INSTANCE.setUserConfig(guild, user, "language", "en");
				    	  e.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/language:successen")).queue();
				    	  break;
				      case "de":
				    	  Configloader.INSTANCE.setUserConfig(guild, user, "language", "de");
				    	  e.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/language:successde")).queue();
				    	  break;
				      case "es":
				    	  Configloader.INSTANCE.setUserConfig(guild, user, "language", "es");
				    	  e.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/language:successes")).queue();
				    	  break;
				      case "fr":
				    	  Configloader.INSTANCE.setUserConfig(guild, user, "language", "fr");
				    	  e.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/language:successfr")).queue();
				    	  break;
				      case "nl":
				    	  Configloader.INSTANCE.setUserConfig(guild, user, "language", "nl");
				    	  e.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/language:successnl")).queue();
				    	  break;
				      case "ru":
				    	  Configloader.INSTANCE.setUserConfig(guild, user, "language", "ru");
				    	  e.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/language:successru")).queue();
				    	  break;
				      default:
						  e.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"general:fatal")).queue();
				      }},
				1, TimeUnit.MINUTES,
				() -> {event.getChannel().sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"general:timeout")).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("language", "Sets your preferred language in which the bot should answer you in on this server");
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/utilities/language:help");
	}
}