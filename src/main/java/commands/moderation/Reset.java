package commands.moderation;

import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

public class Reset implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/reset:nopermission")).queue();
			return;
		}
		SelectionMenu menu = SelectionMenu.create("menu:class")
				.setPlaceholder("Select the value to reset")
				.setRequiredRange(1, 1)
				.addOption("Level channel", "lc")
				.addOption("Report channel", "rc")
				.addOption("Suggestion channel", "sgc")
				.addOption("Support channel", "spc")
				.addOption("Support talk", "st")
				.addOption("Moderator role", "mr")
				.addOption("Support role", "sr")
				.build();
		event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/reset:choosevalue"))
				.setEphemeral(true)
				.addActionRow(menu)
				.queue();
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		waiter.waitForEvent(SelectionMenuEvent.class,
				e -> {if(!e.getChannel().getId().equals(event.getChannel().getId())) {return false;} 
				  	  return e.getUser().getIdLong() == user.getIdLong();},
				e -> {switch (e.getSelectedOptions().get(0).getValue()) {
				      case "lc":
				    	  Configloader.INSTANCE.setGuildConfig(guild, "levelmsgch", "");
				    	  e.replyEmbeds(AnswerEngine.getInstance().buildMessage(AnswerEngine.getInstance().getTitle(guild, user,"/commands/moderation/reset:success"),
				    			  AnswerEngine.getInstance().getDescription(guild, user, "/commands/moderation/reset:success").replace("{value}", "level channel"))).queue();
				    	  break;
				      case "rc":
				    	  Configloader.INSTANCE.setGuildConfig(guild, "reportchannel", "");
				    	  e.replyEmbeds(AnswerEngine.getInstance().buildMessage(AnswerEngine.getInstance().getTitle(guild, user,"/commands/moderation/reset:success"),
				    			  AnswerEngine.getInstance().getDescription(guild, user, "/commands/moderation/reset:success").replace("{value}", "report channel"))).queue();
				    	  break;
				      case "sgc":
				    	  Configloader.INSTANCE.setGuildConfig(guild, "suggest", "");
				    	  e.replyEmbeds(AnswerEngine.getInstance().buildMessage(AnswerEngine.getInstance().getTitle(guild, user,"/commands/moderation/reset:success"),
				    			  AnswerEngine.getInstance().getDescription(guild, user, "/commands/moderation/reset:success").replace("{value}", "suggestion channel"))).queue();
				    	  break;
				      case "spc":
				    	  Configloader.INSTANCE.setGuildConfig(guild, "supportchannel", "");
				    	  e.replyEmbeds(AnswerEngine.getInstance().buildMessage(AnswerEngine.getInstance().getTitle(guild, user,"/commands/moderation/reset:success"),
				    			  AnswerEngine.getInstance().getDescription(guild, user, "/commands/moderation/reset:success").replace("{value}", "support channel"))).queue();
				    	  break;
				      case "st":
				    	  Configloader.INSTANCE.setGuildConfig(guild, "supporttalk", "");
				    	  e.replyEmbeds(AnswerEngine.getInstance().buildMessage(AnswerEngine.getInstance().getTitle(guild, user,"/commands/moderation/reset:success"),
				    			  AnswerEngine.getInstance().getDescription(guild, user, "/commands/moderation/reset:success").replace("{value}", "support talk"))).queue();
				    	  break;
				      case "mr":
				    	  Configloader.INSTANCE.setGuildConfig(guild, "modrole", "");
				    	  e.replyEmbeds(AnswerEngine.getInstance().buildMessage(AnswerEngine.getInstance().getTitle(guild, user,"/commands/moderation/reset:success"),
				    			  AnswerEngine.getInstance().getDescription(guild, user, "/commands/moderation/reset:success").replace("{value}", "moderator role"))).queue();
				    	  break;
				      case "sr":
				    	  Configloader.INSTANCE.setGuildConfig(guild, "supportrole", "");
				    	  e.replyEmbeds(AnswerEngine.getInstance().buildMessage(AnswerEngine.getInstance().getTitle(guild, user,"/commands/moderation/reset:success"),
				    			  AnswerEngine.getInstance().getDescription(guild, user, "/commands/moderation/reset:success").replace("{value}", "support role"))).queue();
				    	  break;
				      default:
						  e.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"general:fatal")).queue();
				      }},
				1, TimeUnit.MINUTES,
				() -> {event.getChannel().sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"general:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("reset", "Resets a specific channel or role definition of the server");
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/moderation/reset:help");
	}

}
