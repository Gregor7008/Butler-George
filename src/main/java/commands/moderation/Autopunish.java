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

public class Autopunish implements Command{
	
	private TextChannel channel;
	private User user;
	private Guild guild;

	@Override
	public void perform(SlashCommandEvent event) {
		if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/autopunish:nopermission")).queue();
			return;
		}
		channel = event.getTextChannel();
		user = event.getUser();
		guild = event.getGuild();
		if (event.getSubcommandName().equals("add")) {
			this.addpunishements1(event);
		}
		if (event.getSubcommandName().equals("remove")) {
			this.removepunishements(event);
		}
		if (event.getSubcommandName().equals("list")) {
			this.listpunishements(event);
		}
		
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("autopunish", "The automatic punishements executed, when a specified number of warnings is reached")
											  .addSubcommands(new SubcommandData("add", "Add an autopunishement"))
											  .addSubcommands(new SubcommandData("remove", "Remove an autopunishement"))
											  .addSubcommands(new SubcommandData("list", "List all autopunishements"));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/moderation/autopunish:help");
	}
	
	private void removepunishements(SlashCommandEvent event) {
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		StringBuilder sB = new StringBuilder();
		String currentraw = Configloader.INSTANCE.getGuildConfig(guild, "autopunish");
		String[] current = currentraw.split(";");
		if (currentraw.equals("")) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/autopunish:nopunishements")).queue();;
			return;
		}
		if (!currentraw.contains(";")) {
			String[] temp1 = currentraw.split("_");
			event.replyEmbeds(AnswerEngine.getInstance().buildMessage("Current punishements:", "#" + temp1[1] + "\s\s" + temp1[0])).queue();
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
			event.replyEmbeds(AnswerEngine.getInstance().buildMessage("Current punishements: (Reply with warning count to remove)", sB.toString())).queue();
		}
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
				e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
				  	  return e.getAuthor().getIdLong() == user.getIdLong();},
				e -> {this.removefinal(e, current);},
				1, TimeUnit.MINUTES,
				() -> {channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/autopunish:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void removefinal(GuildMessageReceivedEvent e, String[] current) {
		for (int i = 1; i <= current.length; i++) {
		   	 String[] temp3 = current[i-1].split("_", 2);
		   	if (temp3[0].contains(e.getMessage().getContentRaw())) {
		   	    Configloader.INSTANCE.deleteGuildConfig(e.getGuild(), "autopunish", current[i-1]);
		   	    e.getMessage().addReaction("U+2705").queue();
		    } else {
		    	e.getMessage().addReaction("U+0078").queue();
		    }
		}
	}
	
	private void addpunishements1(SlashCommandEvent event) {
		SelectionMenu menu = SelectionMenu.create("menu:class")
				.addOption("Removal of role", "rr")
				.addOption("Temporary mute", "tm")
				.addOption("Permanent mute", "pm")
				.addOption("Kick", "kk")
				.addOption("Temporary ban", "tb")
				.addOption("Permanent ban", "pb")
				.build();
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/autopunish:add1"))
				.setEphemeral(true)
				.addActionRow(menu)
				.queue();
		waiter.waitForEvent(SelectionMenuEvent.class,
				e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
				  	  return e.getUser().getIdLong() == user.getIdLong();},
				e -> {String plannedpunish = e.getSelectedOptions().get(0).getValue();
					  this.addpunishements2(plannedpunish);},
				1, TimeUnit.MINUTES,
				() -> {event.getHook().deleteOriginal().queue();
					   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/autopunish:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void addpunishements2(String plannedpunish) {
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/autopunish:add2")).queue();
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
				e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
				  	  return e.getAuthor().getIdLong() == user.getIdLong();},
				e -> {int warnings = Integer.parseInt(e.getMessage().getContentRaw());
					  this.addpunishements3(plannedpunish, warnings);},
				1, TimeUnit.MINUTES,
				() -> {channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/autopunish:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void addpunishements3(String plannedpunish, int warnings) {
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		switch (plannedpunish) {
		case "rr":
			channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/autopunish:add3role")).queue();
			waiter.waitForEvent(GuildMessageReceivedEvent.class,
					e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
					  	  return e.getAuthor().getIdLong() == user.getIdLong();},
					e -> {String punishement = String.valueOf(warnings) + "_removerole_" + e.getMessage().getContentRaw();
						  Configloader.INSTANCE.addGuildConfig(guild, "autopunish", punishement);
						  channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/autopunish:successrole")).queue();
						  },
					1, TimeUnit.MINUTES,
					() -> {channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/autopunish:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
			break;
		case "tm":
			channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/autopunish:add3time")).queue();
			waiter.waitForEvent(GuildMessageReceivedEvent.class,
					e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
					  	  return e.getAuthor().getIdLong() == user.getIdLong();},
					e -> {String punishement = String.valueOf(warnings) + "_tempmute_" + e.getMessage().getContentRaw();
						  Configloader.INSTANCE.addGuildConfig(guild, "autopunish", punishement);
						  channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/autopunish:successtempmute")).queue();},
					1, TimeUnit.MINUTES,
					() -> {channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/autopunish:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
			break;
		case "pm":
			Configloader.INSTANCE.addGuildConfig(guild, "autopunish", String.valueOf(warnings) + "_mute");
			channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/autopunish:successmute")).queue();
			break;
		case "kk":
			Configloader.INSTANCE.addGuildConfig(guild, "autopunish", String.valueOf(warnings) + "_kick");
			channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/autopunish:successkick")).queue();
			break;
		case "tb":
			channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/autopunish:add3time")).queue();
			waiter.waitForEvent(GuildMessageReceivedEvent.class,
					e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
					  	  return e.getAuthor().getIdLong() == user.getIdLong();},
					e -> {String punishement = String.valueOf(warnings) + "_tempban_" + e.getMessage().getContentRaw();
						  Configloader.INSTANCE.addGuildConfig(guild, "autopunish", punishement);
						  channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/autopunish:successtempban")).queue();},
					1, TimeUnit.MINUTES,
					() -> {channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/autopunish:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
			break;
		case "pb":
			Configloader.INSTANCE.addGuildConfig(guild, "autopunish", String.valueOf(warnings) + "_ban");
			channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/autopunish:successban")).queue();
			break;
		default:
			channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/autopunish:fatal")).queue();
		}
	}
	
	private void listpunishements(SlashCommandEvent event) {
		StringBuilder sB = new StringBuilder();
		String currentraw = Configloader.INSTANCE.getGuildConfig(guild, "autopunish");
		if (currentraw.equals("")) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/autopunish:nopunishements")).queue();;
			return;
		}
		if (!currentraw.contains(";")) {
			String[] temp1 = currentraw.split("_");
			event.replyEmbeds(AnswerEngine.getInstance().buildMessage(AnswerEngine.getInstance().getTitle(guild, user, "/commands/moderation/autopunish:nopunishements"), "#" + temp1[1] + "\s\s" + temp1[0])).queue();
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
		event.replyEmbeds(AnswerEngine.getInstance().buildMessage(AnswerEngine.getInstance().getTitle(guild, user, "/commands/moderation/autopunish:nopunishements"), sB.toString())).queue();
	}
}