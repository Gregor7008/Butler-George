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
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Autopunish implements Command{
	
	TextChannel channel;
	User user;

	@Override
	public void perform(SlashCommandEvent event) {
		if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/autopunish:nopermission")).queue();
			return;
		}
		channel = event.getTextChannel();
		user = event.getUser();
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
	public String getHelp() {
		return "Use this command to edit the punishements that should be automatically enforced, when a member hits a certain number of warnings!";
	}
	
	private void removepunishements(SlashCommandEvent event) {
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		final StringBuilder sB = new StringBuilder();
		final String currentraw = Configloader.INSTANCE.getGuildConfig(event.getGuild(), "autopunish");
		final String[] current = currentraw.split(";");
		if (currentraw.equals("")) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/autopunish:nopunishements")).queue();;
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
				() -> {channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/autopunish:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
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
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/autopunish:add1")).queue();
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
				e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
				  	  return e.getAuthor().getIdLong() == user.getIdLong();},
				e -> {int plannedpunish = Integer.parseInt(e.getMessage().getContentRaw());
					  this.addpunishements2(event, plannedpunish);},
				1, TimeUnit.MINUTES,
				() -> {channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/autopunish:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void addpunishements2(SlashCommandEvent event, int plannedpunish) {
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/autopunish:add2")).queue();
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
				e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
				  	  return e.getAuthor().getIdLong() == user.getIdLong();},
				e -> {int warnings = Integer.parseInt(e.getMessage().getContentRaw());
					  this.addpunishements3(event, plannedpunish, warnings);},
				1, TimeUnit.MINUTES,
				() -> {channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/autopunish:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void addpunishements3(SlashCommandEvent event, int plannedpunish, int warnings) {
		if (plannedpunish == 2 || plannedpunish == 5) {
			EventWaiter waiter = Bot.INSTANCE.getWaiter();
			channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/autopunish:add3time")).queue();
			waiter.waitForEvent(GuildMessageReceivedEvent.class,
					e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
					  	  return e.getAuthor().getIdLong() == user.getIdLong();},
					e -> {if (plannedpunish == 2) {
						  	String punishement = String.valueOf(warnings) + "_tempmute_" + e.getMessage().getContentRaw();
						  	Configloader.INSTANCE.addGuildConfig(event.getGuild(), "autopunish", punishement);
						  	event.getChannel().sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/autopunish:successtempmute")).queue();
						  	return;
						  }
						  if (plannedpunish == 5) {
							  String punishement = String.valueOf(warnings) + "_tempban_" + e.getMessage().getContentRaw();
							  Configloader.INSTANCE.addGuildConfig(event.getGuild(), "autopunish", punishement);
							  event.getChannel().sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/autopunish:successtempban")).queue();
							  return;
						  }},
					1, TimeUnit.MINUTES,
					() -> {channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/autopunish:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
			return;
		}
		if (plannedpunish == 1) {
			EventWaiter waiter = Bot.INSTANCE.getWaiter();
			channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/autopunish:add3role")).queue();
			waiter.waitForEvent(GuildMessageReceivedEvent.class,
					e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
					  	  return e.getAuthor().getIdLong() == user.getIdLong();},
					e -> {String punishement = String.valueOf(warnings) + "_removerole_" + e.getMessage().getContentRaw();
						  Configloader.INSTANCE.addGuildConfig(event.getGuild(), "autopunish", punishement);
						  event.getChannel().sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/autopunish:successrole")).queue();
						  },
					1, TimeUnit.MINUTES,
					() -> {channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/autopunish:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
			return;
		}
		if (plannedpunish == 4) {
			Configloader.INSTANCE.addGuildConfig(event.getGuild(), "autopunish", String.valueOf(warnings) + "_kick");
			event.getChannel().sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/autopunish:successkick")).queue();
			return;
		}
		if (plannedpunish == 3) {
			Configloader.INSTANCE.addGuildConfig(event.getGuild(), "autopunish", String.valueOf(warnings) + "_mute");
			event.getChannel().sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/autopunish:successmute")).queue();
			return;
		}
		if (plannedpunish == 6) {
			Configloader.INSTANCE.addGuildConfig(event.getGuild(), "autopunish", String.valueOf(warnings) + "_ban");
			event.getChannel().sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/autopunish:successban")).queue();
			return;
		}
		event.replyEmbeds(AnswerEngine.getInstance().buildMessage("Error!", ":x: | Something went horribly wrong!")).queue();
	}
	
	private void listpunishements(SlashCommandEvent event) {
		Guild guild = event.getGuild();
		final StringBuilder sB = new StringBuilder();
		final String currentraw = Configloader.INSTANCE.getGuildConfig(guild, "autopunish");
		if (currentraw.equals("")) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/autopunish:nopunishements")).queue();;
			return;
		}
		if (!currentraw.contains(";")) {
			String[] temp1 = currentraw.split("_");
			event.replyEmbeds(AnswerEngine.getInstance().buildMessage("Current punishements:", "#" + temp1[1] + "\s\s" + temp1[0])).queue();
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
		event.replyEmbeds(AnswerEngine.getInstance().buildMessage("Current punishements:", sB.toString())).queue();
	}
}
