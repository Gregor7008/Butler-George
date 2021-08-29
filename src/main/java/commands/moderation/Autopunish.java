package commands.moderation;

import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
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
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/autopunish:nopunishements")).queue();;
			return;
		}
		if (!currentraw.contains(";")) {
			String[] temp1 = currentraw.split(":");
			event.replyEmbeds(AnswerEngine.getInstance().buildMessage("Current punishements:", "#" + temp1[1] + "\s\s" + temp1[0])).queue();
		} else {
			for (int i = 1; i <= current.length; i++) {
				String[] temp2 = current[i].split(":");
				sB.append('#')
				  .append(temp2[1] + "\s\s");
				if (i == current.length) {
				sB.append(temp2[0]);
				} else {
				sB.append(temp2[0] + "\n");
				}
			}
			event.replyEmbeds(AnswerEngine.getInstance().buildMessage("Current punishements: (Reply with warning count to remove)", sB.toString())).queue();
		}
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
				e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
				  	  return e.getAuthor().getIdLong() == user.getIdLong();},
				e -> {for (int i = 1; i <= current.length; i++) {
					   	  String[] temp3 = current[i].split(":");
					   	  if (temp3[1].contains(e.getMessage().getContentRaw())) {
					   		  Configloader.INSTANCE.deleteGuildConfig(event.getGuild(), "autopunish", current[i]);
					   		  e.getMessage().addReaction(":white_check_mark:");
					   	  }
					  }},
				1, TimeUnit.MINUTES,
				() -> {channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/autopunish:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void addpunishements1(SlashCommandEvent event) {
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/autopunish:add1")).queue();
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
				e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
				  	  return e.getAuthor().getIdLong() == user.getIdLong();},
				e -> {int plannedpunish = Integer.parseInt(e.getMessage().getContentRaw());
					  this.addpunishements2(event, plannedpunish);},
				1, TimeUnit.MINUTES,
				() -> {channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/autopunish:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void addpunishements2(SlashCommandEvent event, int plannedpunish) {
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/autopunish:add2")).queue();
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
				e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
				  	  return e.getAuthor().getIdLong() == user.getIdLong();},
				e -> {int warnings = Integer.parseInt(e.getMessage().getContentRaw());
					  this.addpunishements3(event, plannedpunish, warnings);},
				1, TimeUnit.MINUTES,
				() -> {channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/autopunish:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void addpunishements3(SlashCommandEvent event, int plannedpunish, int warnings) {
		if (plannedpunish == 2 || plannedpunish == 5) {
			EventWaiter waiter = Bot.INSTANCE.getWaiter();
			channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/autopunish:add3time")).queue();
			waiter.waitForEvent(GuildMessageReceivedEvent.class,
					e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
					  	  return e.getAuthor().getIdLong() == user.getIdLong();},
					e -> {if (plannedpunish == 2) {
						  String punishement = String.valueOf(warnings) + "_tempmute:" + e.getMessage().getContentRaw();
						  Configloader.INSTANCE.addGuildConfig(event.getGuild(), "autopunish", punishement);
						  event.getChannel().sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/autopunish:successtempmute")).queue();
						  }
						  if (plannedpunish == 2) {
						  String punishement = String.valueOf(warnings) + "_tempban:" + e.getMessage().getContentRaw();
						  Configloader.INSTANCE.addGuildConfig(event.getGuild(), "autopunish", punishement);
						  event.getChannel().sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/autopunish:successtempban")).queue();
						  }},
					1, TimeUnit.MINUTES,
					() -> {channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/autopunish:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
			return;
		}
		if (plannedpunish == 1) {
			EventWaiter waiter = Bot.INSTANCE.getWaiter();
			channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/autopunish:add3role")).queue();
			waiter.waitForEvent(GuildMessageReceivedEvent.class,
					e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
					  	  return e.getAuthor().getIdLong() == user.getIdLong();},
					e -> {String punishement = String.valueOf(warnings) + "_removerole:" + e.getMessage().getContentRaw();
						  Configloader.INSTANCE.addGuildConfig(event.getGuild(), "autopunish", punishement);
						  event.getChannel().sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/autopunish:successrole")).queue();
						  },
					1, TimeUnit.MINUTES,
					() -> {channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/autopunish:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
			return;
		}
		event.getChannel().sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/autopunish:success")).queue();
	}
	
	private void listpunishements(SlashCommandEvent event) {
		Guild guild = event.getGuild();
		final StringBuilder sB = new StringBuilder();
		final String currentraw = Configloader.INSTANCE.getGuildConfig(guild, "autopunish");
		if (currentraw.equals("")) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/autopunish:nopunishements")).queue();;
			return;
		}
		if (!currentraw.contains(";")) {
			String[] temp1 = currentraw.split(":");
			event.replyEmbeds(AnswerEngine.getInstance().buildMessage("Current punishements:", "#" + temp1[1] + "\s\s" + temp1[0])).queue();
			return;
		}
		String[] current = currentraw.split(";");
		for (int i = 1; i <= current.length; i++) {
			String[] temp2 = current[i].split(":");
			sB.append('#')
			  .append(temp2[1] + "\s\s");
			if (i == current.length) {
				sB.append(temp2[0]);
			} else {
				sB.append(temp2[0] + "\n");
			}
		}
		event.replyEmbeds(AnswerEngine.getInstance().buildMessage("Current punishements:", sB.toString())).queue();
	}
}
