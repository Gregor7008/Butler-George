package commands.moderation;

import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.ConfigLoader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

public class Penalty implements Command{
	
	private TextChannel channel;
	private User user;
	private Guild guild;

	@Override
	public void perform(SlashCommandInteractionEvent event) {
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
		CommandData command = Commands.slash("penalty", "0")
											  .addSubcommands(new SubcommandData("add", "Adds a penalty that's executed on reaching a certain amount of warnings"))
											  .addSubcommands(new SubcommandData("remove", "Removes a penalty"))
											  .addSubcommands(new SubcommandData("list", "List all currently defined penalties"));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/moderation/penalty:help");
	}
	
	private void removepenalties(SlashCommandInteractionEvent event) {
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		StringBuilder sB = new StringBuilder();
		String currentraw = ConfigLoader.run.getGuildConfig(guild, "penalties");
		String[] current = currentraw.split(";");
		if (currentraw.equals("")) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/penalty:nopenalties").convert()).queue();;
			return;
		}
		if (!currentraw.contains(";")) {
			String[] temp1 = currentraw.split("_");
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/penalty:remlist").replaceDescription("{list}", "#" + temp1[1] + "\s\s" + temp1[0]).convert()).queue();
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
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/penalty:remlist").replaceDescription("{list}", sB.toString()).convert()).queue();
		}
		waiter.waitForEvent(MessageReceivedEvent.class,
				e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
				  	  return e.getAuthor().getIdLong() == user.getIdLong();},
				e -> {this.removefinal(e, current);},
				1, TimeUnit.MINUTES,
				() -> {channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"general:timeout").convert()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void removefinal(MessageReceivedEvent e, String[] current) {
		for (int i = 1; i <= current.length; i++) {
		   	 String[] temp3 = current[i-1].split("_", 2);
		   	if (temp3[0].contains(e.getMessage().getContentRaw())) {
		   	    ConfigLoader.run.removeGuildConfig(e.getGuild(), "penalty", current[i-1]);
		   	    e.getMessage().addReaction("U+2705").queue();
		    } else {
		    	e.getMessage().addReaction("U+0078").queue();
		    }
		}
	}
	
	private void addpenalties1(SlashCommandInteractionEvent event) {
		SelectMenu menu = SelectMenu.create("menu:class")
				.addOption("Removal of role", "rr")
				.addOption("Temporary mute", "tm")
				.addOption("Permanent mute", "pm")
				.addOption("Kick", "kk")
				.addOption("Temporary ban", "tb")
				.addOption("Permanent ban", "pb")
				.build();
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/penalty:add1").convert())
				.setEphemeral(true)
				.addActionRow(menu)
				.queue();
		waiter.waitForEvent(SelectMenuInteractionEvent.class,
				e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
				  	  return e.getUser().getIdLong() == user.getIdLong();},
				e -> {String plannedpunish = e.getSelectedOptions().get(0).getValue();
					  this.addpenalties2(plannedpunish);},
				1, TimeUnit.MINUTES,
				() -> {event.getHook().deleteOriginal().queue();
					   channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "general:timeout").convert()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void addpenalties2(String plannedpunish) {
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/penalty:add2").convert()).queue();
		waiter.waitForEvent(MessageReceivedEvent.class,
				e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
				  	  return e.getAuthor().getIdLong() == user.getIdLong();},
				e -> {int warnings = Integer.parseInt(e.getMessage().getContentRaw());
					  this.addpenalties3(plannedpunish, warnings);},
				1, TimeUnit.MINUTES,
				() -> {channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "general:timeout").convert()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void addpenalties3(String plannedpunish, int warnings) {
		if (ConfigLoader.run.getGuildConfig(guild, "penalties").contains(String.valueOf(warnings))) {
			channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/penalty:error").convert()).queue();
			return;
		}
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		switch (plannedpunish) {
		case "rr":
			channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/penalty:add3role").convert()).queue();
			waiter.waitForEvent(MessageReceivedEvent.class,
					e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
						  if(e.getMessage().getMentionedRoles().isEmpty()) {return false;}
					  	  return e.getAuthor().getIdLong() == user.getIdLong();},
					e -> {String penalty = String.valueOf(warnings) + "_removerole_" + e.getMessage().getContentRaw();
						  ConfigLoader.run.addGuildConfig(guild, "penalties", penalty);
						  channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/penalty:successrole").convert()).queue();},
					1, TimeUnit.MINUTES,
					() -> {channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "general:timeout").convert()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
			break;
		case "tm":
			channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/penalty:add3time").convert()).queue();
			waiter.waitForEvent(MessageReceivedEvent.class,
					e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
					  	  return e.getAuthor().getIdLong() == user.getIdLong();},
					e -> {String penalty = String.valueOf(warnings) + "_tempmute_" + e.getMessage().getContentRaw();
						  ConfigLoader.run.addGuildConfig(guild, "penalties", penalty);
						  channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/penalty:successtempmute").convert()).queue();},
					1, TimeUnit.MINUTES,
					() -> {channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"general:timeout").convert()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
			break;
		case "pm":
			ConfigLoader.run.addGuildConfig(guild, "penalties", String.valueOf(warnings) + "_mute");
			channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/penalty:successmute").convert()).queue();
			break;
		case "kk":
			ConfigLoader.run.addGuildConfig(guild, "penalties", String.valueOf(warnings) + "_kick");
			channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/penalty:successkick").convert()).queue();
			break;
		case "tb":
			channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/penalty:add3time").convert()).queue();
			waiter.waitForEvent(MessageReceivedEvent.class,
					e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
					  	  return e.getAuthor().getIdLong() == user.getIdLong();},
					e -> {String penalty = String.valueOf(warnings) + "_tempban_" + e.getMessage().getContentRaw();
						  ConfigLoader.run.addGuildConfig(guild, "penalties", penalty);
						  channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/penalty:successtempban").convert()).queue();},
					1, TimeUnit.MINUTES,
					() -> {channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"general:timeout").convert()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
			break;
		case "pb":
			ConfigLoader.run.addGuildConfig(guild, "penalties", String.valueOf(warnings) + "_ban");
			channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/penalty:successban").convert()).queue();
			break;
		default:
			channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "general:fatal").convert()).queue();
		}
	}
	
	private void listpenalties(SlashCommandInteractionEvent event) {
		StringBuilder sB = new StringBuilder();
		String currentraw = ConfigLoader.run.getGuildConfig(guild, "penalties");
		if (currentraw.equals("")) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/penalty:nopenalties").convert()).queue();;
			return;
		}
		if (!currentraw.contains(";")) {
			String[] temp1 = currentraw.split("_");
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/penalty:list").replaceDescription("{list}", "#" + temp1[1] + "\s\s" + temp1[0]).convert()).queue();
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
		event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/penalty:list").replaceDescription("{list}", sB.toString()).convert()).queue();
	}
}