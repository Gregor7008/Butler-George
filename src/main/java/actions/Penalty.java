package actions;

import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import components.actions.Action;
import components.actions.ActionData;
import components.actions.ActionRequest;
import components.actions.SubActionData;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

public class Penalty implements ActionRequest {
	
	private TextChannel channel;
	private User user;
	private Guild guild;

	@Override
	public void execute(Action event) {
		channel = event.getTextChannel();
		user = event.getUser();
		guild = event.getGuild();
		if (event.getSubAction().getName().equals("add")) {
			this.addpenalties1(event);
		}
		if (event.getSubAction().getName().equals("remove")) {
			this.removepenalties(event);
		}
		if (event.getSubAction().getName().equals("list")) {
			String response = this.listpenalties(event);
			if (response != null) {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/penalty:list").replaceDescription("{list}", response));
			}
		}
		
	}

	@Override
	public ActionData initialize() {
		ActionData actionData = new ActionData(this).setName("Penalty")
													.setInfo("Configure penalties for reaching a certain warning limit")
													.setMinimumPermission(Permission.BAN_MEMBERS)
													.setCategory(ActionData.ADMINISTRATION)
													.setSubActions(new SubActionData[] {
															new SubActionData("add"),
															new SubActionData("remove"),
															new SubActionData("list")
													});
		return actionData;
	}
	
	private void removepenalties(Action event) {
		EventWaiter waiter = Bot.run.getWaiter();
		String response = this.listpenalties(event);
		if (response != null) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/penalty:remlist").replaceDescription("{list}", response));
			JSONObject penalties = ConfigLoader.getGuildConfig(guild).getJSONObject("penalties");
			waiter.waitForEvent(MessageReceivedEvent.class,
					e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
					  	  return e.getAuthor().getIdLong() == user.getIdLong();},
					e -> {try {
						penalties.getJSONArray(e.getMessage().getContentRaw());
						penalties.remove(e.getMessage().getContentRaw());
					} catch (JSONException ex) {
						channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/penalty:norem").convert()).queue();
					}
					},
					1, TimeUnit.MINUTES,
					() -> {channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user,"general:timeout").convert()).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});
		}
	}
	
	private void addpenalties1(Action event) {
		SelectMenu menu = SelectMenu.create("menu:class")
				.addOption("Removal of role", "rr")
				.addOption("Temporary mute", "tm")
				.addOption("Permanent mute", "pm")
				.addOption("Kick", "ki")
				.addOption("Temporary ban", "tb")
				.addOption("Permanent ban", "pm")
				.build();
		EventWaiter waiter = Bot.run.getWaiter();
		Message reply = event.replyEmbedsRA(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/penalty:add1").convert()).complete()
				.editMessageComponents(ActionRow.of(menu)).complete();
		waiter.waitForEvent(SelectMenuInteractionEvent.class,
				e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
				  	  return e.getUser().getIdLong() == user.getIdLong();},
				e -> {String plannedpunish = e.getSelectedOptions().get(0).getValue();
					  this.addpenalties2(plannedpunish);},
				1, TimeUnit.MINUTES,
				() -> {reply.delete().queue();
					   channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, "general:timeout").convert()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void addpenalties2(String plannedpunish) {
		EventWaiter waiter = Bot.run.getWaiter();
		channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/penalty:add2").convert()).queue();
		waiter.waitForEvent(MessageReceivedEvent.class,
				e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;}
					  try {Integer.valueOf(e.getMessage().getContentRaw());} catch (NumberFormatException ex) {return false;}
				  	  return e.getAuthor().getIdLong() == user.getIdLong();},
				e -> {this.addpenalties3(plannedpunish, e.getMessage().getContentRaw());},
				1, TimeUnit.MINUTES,
				() -> {channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, "general:timeout").convert()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void addpenalties3(String plannedpunish, String warnings) {
		JSONObject penalties = ConfigLoader.getGuildConfig(guild).getJSONObject("penalties");
		try {
			penalties.getJSONArray(warnings);
			channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/penalty:error").convert()).queue();
			return;
		} catch (JSONException e) {}
 		EventWaiter waiter = Bot.run.getWaiter();
		switch (plannedpunish) {
		case "rr":
			channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/penalty:add3role").convert()).queue();
			waiter.waitForEvent(MessageReceivedEvent.class,
					e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
						  if(e.getMessage().getMentions().getRoles().isEmpty()) {return false;}
					  	  return e.getAuthor().getIdLong() == user.getIdLong();},
					e -> {penalties.put(warnings, new JSONArray().put(plannedpunish).put(e.getMessage().getMentions().getRoles().get(0).getId()));
						  channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/penalty:successrole").convert()).queue();},
					1, TimeUnit.MINUTES,
					() -> {channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, "general:timeout").convert()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
			break;
		case "tm":
			channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/penalty:add3time").convert()).queue();
			waiter.waitForEvent(MessageReceivedEvent.class,
					e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;}
					 	  try {Integer.valueOf(e.getMessage().getContentRaw());} catch (NumberFormatException ex) {return false;}
					  	  return e.getAuthor().getIdLong() == user.getIdLong();},
					e -> {penalties.put(warnings, new JSONArray().put(plannedpunish).put(e.getMessage().getContentRaw()));
						  channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/moderation/penalty:successtempmute").convert()).queue();},
					1, TimeUnit.MINUTES,
					() -> {channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user,"general:timeout").convert()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
			break;
		case "pm":
			penalties.put(warnings, new JSONArray().put(plannedpunish).put("0"));
			channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/moderation/penalty:successmute").convert()).queue();
			break;
		case "ki":
			penalties.put(warnings, new JSONArray().put(plannedpunish).put("0"));
			channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/moderation/penalty:successkick").convert()).queue();
			break;
		case "tb":
			channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/moderation/penalty:add3time").convert()).queue();
			waiter.waitForEvent(MessageReceivedEvent.class,
					e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;}
						  try {Integer.valueOf(e.getMessage().getContentRaw());} catch (NumberFormatException ex) {return false;}
					  	  return e.getAuthor().getIdLong() == user.getIdLong();},
					e -> {penalties.put(warnings, new JSONArray().put(plannedpunish).put(e.getMessage().getContentRaw()));
						  channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/moderation/penalty:successtempban").convert()).queue();},
					1, TimeUnit.MINUTES,
					() -> {channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user,"general:timeout").convert()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
			break;
		case "pb":
			penalties.put(warnings, new JSONArray().put(plannedpunish).put("0"));
			channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user,"/commands/moderation/penalty:successban").convert()).queue();
			break;
		default:
			channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, "general:fatal").convert()).queue();
		}
	}
	
	private String listpenalties(Action event) {
		StringBuilder sB = new StringBuilder();
		JSONObject current = ConfigLoader.getGuildConfig(guild).getJSONObject("penalties");
		if (current.isEmpty()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/penalty:nopenalties"));
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
					channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, "general:fatal").convert()).queue();
			}
			sB.append("\n");
		});
		return sB.toString();
	}
}