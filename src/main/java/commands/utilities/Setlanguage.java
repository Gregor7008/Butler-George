package commands.utilities;

import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

public class Setlanguage implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		SelectionMenu menu = SelectionMenu.create("menu:class")
				.setPlaceholder("Select your language")
				.setRequiredRange(1, 1)
				.addOption("English", "en")
				.addOption("Deutsch", "de")
				.addOption("Español", "es")
				.addOption("Français", "fr")
				.addOption("Italiano", "it")
				.addOption("日本", "jp")
				.addOption("Nederlands", "nl")
				.addOption("Pусский", "ru")
				.build();
		event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/utilities/setlanguage:chooselang"))
				.setEphemeral(true)
				.addActionRow(menu)
				.queue();
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		waiter.waitForEvent(SelectionMenuEvent.class,
				e -> {if(!e.getChannel().getId().equals(event.getChannel().getId())) {return false;} 
				  	  return e.getUser().getIdLong() == event.getUser().getIdLong();},
				e -> {switch (e.getSelectedOptions().get(0).getValue()) {
				      case "en":
				    	  Configloader.INSTANCE.setUserConfig(event.getMember(), "language", "en");
				    	  e.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/utilities/setlanguage:successen")).queue();
				    	  break;
				      case "de":
				    	  Configloader.INSTANCE.setUserConfig(event.getMember(), "language", "de");
				    	  e.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/utilities/setlanguage:successde")).queue();
				    	  break;
				      case "es":
				    	  Configloader.INSTANCE.setUserConfig(event.getMember(), "language", "es");
				    	  e.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/utilities/setlanguage:successes")).queue();
				    	  break;
				      case "fr":
				    	  Configloader.INSTANCE.setUserConfig(event.getMember(), "language", "fr");
				    	  e.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/utilities/setlanguage:successfr")).queue();
				    	  break;
				      case "it":
				    	  Configloader.INSTANCE.setUserConfig(event.getMember(), "language", "it");
				    	  e.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/utilities/setlanguage:successit")).queue();
				    	  break;
				      case "jp":
				    	  Configloader.INSTANCE.setUserConfig(event.getMember(), "language", "jp");
				    	  e.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/utilities/setlanguage:successjp")).queue();
				    	  break;
				      case "nl":
				    	  Configloader.INSTANCE.setUserConfig(event.getMember(), "language", "nl");
				    	  e.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/utilities/setlanguage:successnl")).queue();
				    	  break;
				      case "ru":
				    	  Configloader.INSTANCE.setUserConfig(event.getMember(), "language", "ru");
				    	  e.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/utilities/setlanguage:successru")).queue();
				    	  break;
				      default:
						  e.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/utilities/setlanguage:error")).queue();
				      }},
				1, TimeUnit.MINUTES,
				() -> {event.getChannel().sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/utilities/setlanguage:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("setlanguage", "Set your preferred language the bot should answer in!");
		return command;
	}

	@Override
	public String getHelp() {
		return "Use this command to set the language the bot should answer you in from now on.\nJust execute it and it will guide you through the setup process!";
	}
}
