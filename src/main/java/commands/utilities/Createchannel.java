package commands.utilities;

import java.util.concurrent.TimeUnit;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

public class Createchannel implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		String name = event.getOption("name").getAsString();
		if (Configloader.INSTANCE.getGuildConfig(guild, "ccrole").equals("")) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/utilities/createchannel:norole")).queue();
			return;
		}
		if (!event.getMember().getRoles().contains(guild.getRoleById(Configloader.INSTANCE.getGuildConfig(guild, "ccrole")))) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/utilities/createchannel:nopermission")).queue();
			return;
		}
		SelectionMenu menu = SelectionMenu.create("selct")
				.setPlaceholder("Select the channel type")
				.setRequiredRange(1, 1)
				.addOption("Text Channel", "tc")
				.addOption("Voice Channel", "vc")
				.addOption("Stage Channel", "sc")
				.build();
		event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/createchannel:selecttype"))
				.setEphemeral(true)
				.addActionRow(menu)
				.queue();
		Bot.INSTANCE.getWaiter().waitForEvent(SelectionMenuEvent.class,
				e -> {if(!e.getChannel().getId().equals(event.getChannel().getId())) {return false;} 
				  	  return e.getUser().getIdLong() == user.getIdLong();},
				e -> {switch (e.getSelectedOptions().get(0).getValue()) {
				      case "tc":
				    	  this.createTextChannel(guild, user, name);
				    	  e.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/utilities/createchannel:success")).queue();
				    	  break;
				      case "vc":
				    	  this.createVoiceChannel(guild, user, name);
				    	  e.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/utilities/createchannel:success")).queue();
				    	  break;
				      case "sc":
				    	  this.createStageChannel(guild, user, name);
				    	  e.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/utilities/createchannel:success")).queue();
				    	  break;
				      default:
						  e.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"general:fatal")).queue();
				      }},
				1, TimeUnit.MINUTES,
				() -> {event.getChannel().sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"general:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("createchannel", "Create a custom channel for you and your friends!")
				.addOption(OptionType.STRING, "name", "The name of the new channel", true);
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/utilities/createchannel:help");
	}
	
	private void createTextChannel(Guild guild, User user, String name) {
		Category cgy;
		if (Configloader.INSTANCE.getUserConfig(guild, user, "cccategory").equals("")) {
			cgy = guild.createCategory(user.getName() + "'s channels").complete();
			cgy.putPermissionOverride(guild.getMember(user)).setAllow(Permission.ALL_PERMISSIONS).queue();
			cgy.putPermissionOverride(guild.getPublicRole()).setDeny(Permission.VIEW_CHANNEL).queue();
			if (!Configloader.INSTANCE.getGuildConfig(guild, "ccdefaccess").equals("")) {
				String[] defroles = Configloader.INSTANCE.getGuildConfig(guild, "ccdefaccess").split(";");
				for (int i = 0; i < defroles.length; i++) {
					cgy.putPermissionOverride(guild.getRoleById(defroles[i])).setAllow(Permission.ALL_PERMISSIONS).queue();
				}
			}
			Configloader.INSTANCE.setUserConfig(guild, user, "cccategory", cgy.getId());
  	    } else {
  	    	cgy = guild.getCategoryById(Configloader.INSTANCE.getUserConfig(guild, user, "cccategory"));
  	    }
		guild.createTextChannel(name, cgy).queue();
	}
	
	private void createVoiceChannel(Guild guild, User user, String name) {
		Category cgy;
		if (Configloader.INSTANCE.getUserConfig(guild, user, "cccategory").equals("")) {
			cgy = guild.createCategory(user.getName() + "'s channels").complete();
			cgy.putPermissionOverride(guild.getMember(user)).setAllow(Permission.ALL_PERMISSIONS).queue();
			cgy.putPermissionOverride(guild.getPublicRole()).setDeny(Permission.VIEW_CHANNEL).queue();
			if (!Configloader.INSTANCE.getGuildConfig(guild, "ccdefaccess").equals("")) {
				String[] defroles = Configloader.INSTANCE.getGuildConfig(guild, "ccdefaccess").split(";");
				for (int i = 0; i < defroles.length; i++) {
					cgy.putPermissionOverride(guild.getRoleById(defroles[i])).setAllow(Permission.ALL_PERMISSIONS).queue();
				}
			}
			Configloader.INSTANCE.setUserConfig(guild, user, "cccategory", cgy.getId());
  	    } else {
  	    	cgy = guild.getCategoryById(Configloader.INSTANCE.getUserConfig(guild, user, "cccategory"));
  	    }
		guild.createVoiceChannel(name, cgy).queue();
	}
	
	private void createStageChannel(Guild guild, User user, String name) {
		Category cgy;
		if (Configloader.INSTANCE.getUserConfig(guild, user, "cccategory").equals("")) {
			cgy = guild.createCategory(user.getName() + "'s channels").complete();
			cgy.putPermissionOverride(guild.getMember(user)).setAllow(Permission.ALL_PERMISSIONS).queue();
			cgy.putPermissionOverride(guild.getPublicRole()).setDeny(Permission.VIEW_CHANNEL).queue();
			if (!Configloader.INSTANCE.getGuildConfig(guild, "ccdefaccess").equals("")) {
				String[] defroles = Configloader.INSTANCE.getGuildConfig(guild, "ccdefaccess").split(";");
				for (int i = 0; i < defroles.length; i++) {
					cgy.putPermissionOverride(guild.getRoleById(defroles[i])).setAllow(Permission.ALL_PERMISSIONS).queue();
				}
			}
			Configloader.INSTANCE.setUserConfig(guild, user, "cccategory", cgy.getId());
  	    } else {
  	    	cgy = guild.getCategoryById(Configloader.INSTANCE.getUserConfig(guild, user, "cccategory"));
  	    }
		guild.createStageChannel(name, cgy).queue();
	}
}