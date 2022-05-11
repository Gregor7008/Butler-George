package commands.moderation;

import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

public class Reset implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		SelectMenu menu = SelectMenu.create("selvalre")
				.setPlaceholder("Select the value to reset")
				.setRequiredRange(1, 1)
				.addOption("Level channel", "lc")
				.addOption("Report channel", "rc")
				.addOption("Suggestion channel", "sgc")
				.addOption("Support channel", "spc")
				.addOption("Support talk", "st")
				.addOption("Moderator role", "mr")
				.addOption("Support role", "sr")
				.addOption("User channel role", "ucr")
				.build();
		event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/reset:choosevalue").convert())
				.setEphemeral(true)
				.addActionRow(menu)
				.queue();
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		waiter.waitForEvent(SelectMenuInteractionEvent.class,
				e -> {if(!e.getChannel().getId().equals(event.getChannel().getId())) {return false;} 
				  	  return e.getUser().getIdLong() == user.getIdLong();},
				e -> {switch (e.getSelectedOptions().get(0).getValue()) {
				      case "lc":
				    	  Configloader.INSTANCE.setGuildConfig(guild, "levelmsgch", "");
				    	  e.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/reset:success").replaceDescription("{value}", "level channel").convert()).queue();
				    	  break;
				      case "rc":
				    	  Configloader.INSTANCE.setGuildConfig(guild, "reportchannel", "");
				    	  e.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/reset:success").replaceDescription("{value}", "report channel").convert()).queue();
				    	  break;
				      case "sgc":
				    	  Configloader.INSTANCE.setGuildConfig(guild, "suggest", "");
				    	  e.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/reset:success").replaceDescription("{value}", "suggestion channel").convert()).queue();
				    	  break;
				      case "spc":
				    	  Configloader.INSTANCE.setGuildConfig(guild, "supportchannel", "");
				    	  e.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/reset:success").replaceDescription("{value}", "support channel").convert()).queue();
				    	  break;
				      case "st":
				    	  Configloader.INSTANCE.setGuildConfig(guild, "supporttalk", "");
				    	  e.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/reset:success").replaceDescription("{value}", "support talk").convert()).queue();
				    	  break;
				      case "mr":
				    	  Configloader.INSTANCE.setGuildConfig(guild, "modrole", "");
				    	  e.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/reset:success").replaceDescription("{value}", "moderator role").convert()).queue();
				    	  break;
				      case "sr":
				    	  Configloader.INSTANCE.setGuildConfig(guild, "supportrole", "");
				    	  e.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/reset:success").replaceDescription("{value}", "support role").convert()).queue();
				    	  break;
				      case "ucr":
				    	  Configloader.INSTANCE.setGuildConfig(guild, "ccrole", "");
				    	  e.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/reset:success").replaceDescription("{value}", "user channel role").convert()).queue();
				    	  break;
				      default:
						  e.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"general:fatal").convert()).queue();
				      }},
				1, TimeUnit.MINUTES,
				() -> {event.getChannel().sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"general:timeout").convert()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("reset", "Resets a specific channel or role definition of the server");
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/moderation/reset:help");
	}

}
