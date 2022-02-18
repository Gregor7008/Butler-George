package commands.moderation;

import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

public class Penalty implements Command{
	
	private TextChannel channel;
	private User user;
	private Guild guild;

	@Override
	public void perform(SlashCommandEvent event) {
		if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/penalty:nopermission")).queue();
			return;
		}
		channel = event.getTextChannel();
		user = event.getUser();
		guild = event.getGuild();
		if (event.getSubcommandName().equals("add")) {
			this.addpenalties1(event);
		}
		if (event.getSubcommandName().equals("remove")) {
			this.removepenalties(event);
		}
		if (event.getSubcommandName().equals("list")) {
			this.listpenalties(event);
		}
		
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("penalty", "0")
											  .addSubcommands(new SubcommandData("add", "Adds a penalty that's executed on reaching a certain amount of warnings"))
											  .addSubcommands(new SubcommandData("remove", "Removes a penalty"))
											  .addSubcommands(new SubcommandData("list", "List all currently defined penalties"));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/moderation/penalty:help");
	}
	
	private void removepenalties(SlashCommandEvent event) {
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		StringBuilder sB = new StringBuilder();
		String currentraw = Configloader.INSTANCE.getGuildConfig(guild, "penalties");
		String[] current = currentraw.split(";");
		if (currentraw.equals("")) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/penalty:nopenalties")).queue();;
			return;
		}
		if (!currentraw.contains(";")) {
			String[] temp1 = currentraw.split("_");
			event.replyEmbeds(AnswerEngine.ae.buildMessage("Current penalties:", "#" + temp1[1] + "\s\s" + temp1[0])).queue();
		} else {
			for (int i = 1; i <= current.length; i++) {
				 String[] temp2 = current[i-1].split("_");
				 sB.append(temp2[1] + "\son\s");
				 if (i == current.length) {
				 	 sB.append(temp2[0] + "\swarnings");
				 } else {
					 sB.append(temp2[0] + "\swarnings,\n");
				 }
			}
			event.replyEmbeds(AnswerEngine.ae.buildMessage("Current penalties: (Reply with warning count to remove)", sB.toString())).queue();
		}
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
				e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
				  	  return e.getAuthor().getIdLong() == user.getIdLong();},
				e -> {this.removefinal(e, current);},
				1, TimeUnit.MINUTES,
				() -> {channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"general:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void removefinal(GuildMessageReceivedEvent e, String[] current) {
		for (int i = 1; i <= current.length; i++) {
		   	 String[] temp3 = current[i-1].split("_", 2);
		   	if (temp3[0].contains(e.getMessage().getContentRaw())) {
		   	    Configloader.INSTANCE.deleteGuildConfig(e.getGuild(), "penalty", current[i-1]);
		   	    e.getMessage().addReaction("U+2705").queue();
		    } else {
		    	e.getMessage().addReaction("U+0078").queue();
		    }
		}
	}
	
	private void addpenalties1(SlashCommandEvent event) {
		SelectionMenu menu = SelectionMenu.create("menu:class")
				.addOption("Removal of role", "rr")
				.addOption("Temporary mute", "tm")
				.addOption("Permanent mute", "pm")
				.addOption("Kick", "kk")
				.addOption("Temporary ban", "tb")
				.addOption("Permanent ban", "pb")
				.build();
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/penalty:add1"))
				.setEphemeral(true)
				.addActionRow(menu)
				.queue();
		waiter.waitForEvent(SelectionMenuEvent.class,
				e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
				  	  return e.getUser().getIdLong() == user.getIdLong();},
				e -> {String plannedpunish = e.getSelectedOptions().get(0).getValue();
					  this.addpenalties2(plannedpunish);},
				1, TimeUnit.MINUTES,
				() -> {event.getHook().deleteOriginal().queue();
					   channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "general:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void addpenalties2(String plannedpunish) {
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/penalty:add2")).queue();
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
				e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
				  	  return e.getAuthor().getIdLong() == user.getIdLong();},
				e -> {int warnings = Integer.parseInt(e.getMessage().getContentRaw());
					  this.addpenalties3(plannedpunish, warnings);},
				1, TimeUnit.MINUTES,
				() -> {channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "general:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void addpenalties3(String plannedpunish, int warnings) {
		if (Configloader.INSTANCE.getGuildConfig(guild, "penalties").contains(String.valueOf(warnings))) {
			channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/penalty:error")).queue();
			return;
		}
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		switch (plannedpunish) {
		case "rr":
			channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/penalty:add3role")).queue();
			waiter.waitForEvent(GuildMessageReceivedEvent.class,
					e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
					  	  return e.getAuthor().getIdLong() == user.getIdLong();},
					e -> {String penalty = String.valueOf(warnings) + "_removerole_" + e.getMessage().getContentRaw();
						  Configloader.INSTANCE.addGuildConfig(guild, "penalties", penalty);
						  channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/penalty:successrole")).queue();},
					1, TimeUnit.MINUTES,
					() -> {channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "general:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
			break;
		case "tm":
			channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/penalty:add3time")).queue();
			waiter.waitForEvent(GuildMessageReceivedEvent.class,
					e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
					  	  return e.getAuthor().getIdLong() == user.getIdLong();},
					e -> {String penalty = String.valueOf(warnings) + "_tempmute_" + e.getMessage().getContentRaw();
						  Configloader.INSTANCE.addGuildConfig(guild, "penalties", penalty);
						  channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/penalty:successtempmute")).queue();},
					1, TimeUnit.MINUTES,
					() -> {channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"general:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
			break;
		case "pm":
			Configloader.INSTANCE.addGuildConfig(guild, "penalties", String.valueOf(warnings) + "_mute");
			channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/penalty:successmute")).queue();
			break;
		case "kk":
			Configloader.INSTANCE.addGuildConfig(guild, "penalties", String.valueOf(warnings) + "_kick");
			channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/penalty:successkick")).queue();
			break;
		case "tb":
			channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/penalty:add3time")).queue();
			waiter.waitForEvent(GuildMessageReceivedEvent.class,
					e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
					  	  return e.getAuthor().getIdLong() == user.getIdLong();},
					e -> {String penalty = String.valueOf(warnings) + "_tempban_" + e.getMessage().getContentRaw();
						  Configloader.INSTANCE.addGuildConfig(guild, "penalties", penalty);
						  channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/penalty:successtempban")).queue();},
					1, TimeUnit.MINUTES,
					() -> {channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"general:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
			break;
		case "pb":
			Configloader.INSTANCE.addGuildConfig(guild, "penalties", String.valueOf(warnings) + "_ban");
			channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/penalty:successban")).queue();
			break;
		default:
			channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "general:fatal")).queue();
		}
	}
	
	private void listpenalties(SlashCommandEvent event) {
		StringBuilder sB = new StringBuilder();
		String currentraw = Configloader.INSTANCE.getGuildConfig(guild, "penalties");
		if (currentraw.equals("")) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/penalty:nopenalties")).queue();;
			return;
		}
		if (!currentraw.contains(";")) {
			String[] temp1 = currentraw.split("_");
			event.replyEmbeds(AnswerEngine.ae.buildMessage(AnswerEngine.ae.getTitle(guild, user, "/commands/moderation/penalty:nopenalties"), "#" + temp1[1] + "\s\s" + temp1[0])).queue();
			return;
		}
		String[] current = currentraw.split(";");
		for (int i = 1; i <= current.length; i++) {
			String[] temp2 = current[i-1].split("_");
			sB.append(temp2[1] + "\son\s");
			if (i == current.length) {
				sB.append(temp2[0] + "\swarnings");
			} else {
				sB.append(temp2[0] + "\swarnings,\n");
			}
		}
		event.replyEmbeds(AnswerEngine.ae.buildMessage(AnswerEngine.ae.getTitle(guild, user, "/commands/moderation/penalty:nopenalties"), sB.toString())).queue();
	}
}