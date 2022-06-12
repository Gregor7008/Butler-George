package commands.moderation;

import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
			String response = this.listpenalties(event);
			if (response != null) {
				event.replyEmbeds(AnswerEngine.build.fetchMessage(guild, user, "/commands/moderation/penalty:list").replaceDescription("{list}", response).convert()).queue();
			}
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
		return AnswerEngine.build.getRaw(guild, user, "/commands/moderation/penalty:help");
	}
	
	private void removepenalties(SlashCommandInteractionEvent event) {
		EventWaiter waiter = Bot.run.getWaiter();
		String response = this.listpenalties(event);
		if (response != null) {
			event.replyEmbeds(AnswerEngine.build.fetchMessage(guild, user, "/commands/moderation/penalty:remlist").replaceDescription("{list}", response).convert()).queue();
			JSONObject penalties = ConfigLoader.run.getGuildConfig(guild).getJSONObject("penalties");
			waiter.waitForEvent(MessageReceivedEvent.class,
					e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
					  	  return e.getAuthor().getIdLong() == user.getIdLong();},
					e -> {try {
						penalties.getJSONArray(e.getMessage().getContentRaw());
						penalties.remove(e.getMessage().getContentRaw());
					} catch (JSONException ex) {
						channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user, "/commands/moderation/penalty:norem").convert()).queue();
					}
					},
					1, TimeUnit.MINUTES,
					() -> {channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user,"general:timeout").convert()).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});
		}
	}
	
	private void addpenalties1(SlashCommandInteractionEvent event) {
		SelectMenu menu = SelectMenu.create("menu:class")
				.addOption("Removal of role", "rr")
				.addOption("Temporary mute", "tm")
				.addOption("Permanent mute", "pm")
				.addOption("Kick", "ki")
				.addOption("Temporary ban", "tb")
				.addOption("Permanent ban", "pm")
				.build();
		EventWaiter waiter = Bot.run.getWaiter();
		event.replyEmbeds(AnswerEngine.build.fetchMessage(guild, user, "/commands/moderation/penalty:add1").convert())
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
					   channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user, "general:timeout").convert()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void addpenalties2(String plannedpunish) {
		EventWaiter waiter = Bot.run.getWaiter();
		channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user, "/commands/moderation/penalty:add2").convert()).queue();
		waiter.waitForEvent(MessageReceivedEvent.class,
				e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;}
					  try {Integer.valueOf(e.getMessage().getContentRaw());} catch (NumberFormatException ex) {return false;}
				  	  return e.getAuthor().getIdLong() == user.getIdLong();},
				e -> {this.addpenalties3(plannedpunish, e.getMessage().getContentRaw());},
				1, TimeUnit.MINUTES,
				() -> {channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user, "general:timeout").convert()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void addpenalties3(String plannedpunish, String warnings) {
		JSONObject penalties = ConfigLoader.run.getGuildConfig(guild).getJSONObject("penalties");
		try {
			penalties.getJSONArray(warnings);
			channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user, "/commands/moderation/penalty:error").convert()).queue();
			return;
		} catch (JSONException e) {}
 		EventWaiter waiter = Bot.run.getWaiter();
		switch (plannedpunish) {
		case "rr":
			channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user, "/commands/moderation/penalty:add3role").convert()).queue();
			waiter.waitForEvent(MessageReceivedEvent.class,
					e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
						  if(e.getMessage().getMentions().getRoles().isEmpty()) {return false;}
					  	  return e.getAuthor().getIdLong() == user.getIdLong();},
					e -> {penalties.put(warnings, new JSONArray().put(plannedpunish).put(e.getMessage().getMentions().getRoles().get(0).getId()));
						  channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user, "/commands/moderation/penalty:successrole").convert()).queue();},
					1, TimeUnit.MINUTES,
					() -> {channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user, "general:timeout").convert()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
			break;
		case "tm":
			channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user, "/commands/moderation/penalty:add3time").convert()).queue();
			waiter.waitForEvent(MessageReceivedEvent.class,
					e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;}
					 	  try {Integer.valueOf(e.getMessage().getContentRaw());} catch (NumberFormatException ex) {return false;}
					  	  return e.getAuthor().getIdLong() == user.getIdLong();},
					e -> {penalties.put(warnings, new JSONArray().put(plannedpunish).put(e.getMessage().getContentRaw()));
						  channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user,"/commands/moderation/penalty:successtempmute").convert()).queue();},
					1, TimeUnit.MINUTES,
					() -> {channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user,"general:timeout").convert()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
			break;
		case "pm":
			penalties.put(warnings, new JSONArray().put(plannedpunish).put("0"));
			channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user,"/commands/moderation/penalty:successmute").convert()).queue();
			break;
		case "ki":
			penalties.put(warnings, new JSONArray().put(plannedpunish).put("0"));
			channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user,"/commands/moderation/penalty:successkick").convert()).queue();
			break;
		case "tb":
			channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user,"/commands/moderation/penalty:add3time").convert()).queue();
			waiter.waitForEvent(MessageReceivedEvent.class,
					e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;}
						  try {Integer.valueOf(e.getMessage().getContentRaw());} catch (NumberFormatException ex) {return false;}
					  	  return e.getAuthor().getIdLong() == user.getIdLong();},
					e -> {penalties.put(warnings, new JSONArray().put(plannedpunish).put(e.getMessage().getContentRaw()));
						  channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user,"/commands/moderation/penalty:successtempban").convert()).queue();},
					1, TimeUnit.MINUTES,
					() -> {channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user,"general:timeout").convert()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
			break;
		case "pb":
			penalties.put(warnings, new JSONArray().put(plannedpunish).put("0"));
			channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user,"/commands/moderation/penalty:successban").convert()).queue();
			break;
		default:
			channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user, "general:fatal").convert()).queue();
		}
	}
	
	private String listpenalties(SlashCommandInteractionEvent event) {
		StringBuilder sB = new StringBuilder();
		JSONObject current = ConfigLoader.run.getGuildConfig(guild).getJSONObject("penalties");
		if (current.isEmpty()) {
			event.replyEmbeds(AnswerEngine.build.fetchMessage(guild, user, "/commands/moderation/penalty:nopenalties").convert()).queue();;
			return null;
		}
		current.keySet().forEach(e -> {
			sB.append("• " + e + ": ");
			JSONArray penalty = current.getJSONArray(e);
			switch (penalty.getString(0)) {
				case "rr":
					sB.append("Remove role " + guild.getRoleById(penalty.getString(1)).getAsMention());
					break;
				case "tm":
					sB.append("Mute for " + penalty.getString(1) + " days");
					break;
				case "pm":
					sB.append("Permanent mute");
					break;
				case "ki":
					sB.append("Kick from server");
					break;
				case "tb":
					sB.append("Ban from server for " + penalty.getString(1) + " days");
					break;
				case "pb":
					sB.append("Permanent ban from server");
					break;
				default:
					channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user, "general:fatal").convert()).queue();
			}
			sB.append("\n");
		});
		return sB.toString();
	}
}