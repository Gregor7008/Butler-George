package operations;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import components.ResponseDetector;
import components.Toolbox;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.operations.OperationData;
import components.operations.OperationEvent;
import components.operations.OperationEventHandler;
import components.operations.SubOperationData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

public class Penalty implements OperationEventHandler {

	private OperationEvent event;
	private User user;
	private Guild guild;

	@Override
	public void execute(OperationEvent event) {
		this.user = event.getUser();
		this.guild = event.getGuild();
		this.event = event;
		if (event.getSubOperation().equals("add")) {
			this.addpenalties1(event);
			return;
		}
		if (event.getSubOperation().equals("remove")) {
			ConfigLoader.getGuildConfig(guild).getJSONObject("penalties").clear();
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "remsuccess")).queue();
			return;
		}
		if (event.getSubOperation().equals("delete")) {
			this.deletepenalties(event);
			return;
		}
		if (event.getSubOperation().equals("list")) {
			String response = this.listpenalties(event);
			if (response != null) {
				event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "list").replaceDescription("{list}", response)).queue();
			}
		}
		
	}

	@Override
	public OperationData initialize() {
		OperationData operationData = new OperationData(this).setName("Penalty")
													.setInfo("Configure penalties for reaching a certain warning limit")
													.setSubOperations(new SubOperationData[] {
															new SubOperationData("add", "Add a new penalty for a specific amount of warnings"),
															new SubOperationData("delete", "Deactivate and delete one penalty"),
															new SubOperationData("remove", "Remove all active penalties"),
															new SubOperationData("list", "List all active penalties")
													});
		return operationData;
	}
	
	private void deletepenalties(OperationEvent event) {
		String response = this.listpenalties(event);
		if (response != null) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "remlist").replaceDescription("{list}", response)).queue();
			JSONObject penalties = ConfigLoader.getGuildConfig(guild).getJSONObject("penalties");
			ResponseDetector.waitForMessage(guild, user, event.getChannel(),
					e -> {try {
							  penalties.getJSONArray(e.getMessage().getContentRaw());
							  penalties.remove(e.getMessage().getContentRaw());
							  event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "delsuccess").convert()).queue();
					      } catch (JSONException ex) {
					    	  event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nodelval").convert()).queue();
					      }});
		}
	}
	
	private void addpenalties1(OperationEvent event) {
		SelectMenu menu = SelectMenu.create("selpen")
				.addOption("Removal of role", "rr")
				.addOption("Temporary mute", "tm")
				.addOption("Permanent mute", "pm")
				.addOption("Kick", "ki")
				.addOption("Temporary ban", "tb")
				.addOption("Permanent ban", "pb")
				.build();
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "add1")).setActionRows(ActionRow.of(menu)).queue();
		ResponseDetector.waitForMenuSelection(guild, user, event.getMessage(), menu.getId(),
				e -> {String plannedpunish = e.getSelectedOptions().get(0).getValue();
					  this.addpenalties2(plannedpunish, e, event);});
	}
	
	private void addpenalties2(String plannedpunish, SelectMenuInteractionEvent event, OperationEvent op) {
		Toolbox.deleteActionRows(event.getMessage(), () -> {
			event.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "add2").convert()).queue();
			ResponseDetector.waitForMessage(guild, user, op.getMessage(),
					e -> {try {
						      Integer.valueOf(e.getMessage().getContentRaw());
						      return true;
						  } catch (NumberFormatException ex) {return false;}},
					e -> {this.addpenalties3(plannedpunish, e.getMessage().getContentRaw(), op);});
		});
	}
	
	private void addpenalties3(String plannedpunish, String warnings, OperationEvent op) {
		JSONObject penalties = ConfigLoader.getGuildConfig(guild).getJSONObject("penalties");
		try {
			penalties.getJSONArray(warnings);
			event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "error").convert()).queue();
			return;
		} catch (JSONException e) {}
		switch (plannedpunish) {
		case "rr":
			event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "add3role").convert()).queue();
			ResponseDetector.waitForMessage(guild, user, op.getMessage(),
					e -> {return !e.getMessage().getMentions().getRoles().isEmpty();},
					e -> {penalties.put(warnings, new JSONArray().put(plannedpunish).put(e.getMessage().getMentions().getRoles().get(0).getId()));
						  event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "successrole").convert()).queue();});
			break;
		case "tm":
			event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "add3time").convert()).queue();
			ResponseDetector.waitForMessage(guild, user, op.getMessage(),
					e -> {try {
							  Integer.valueOf(e.getMessage().getContentRaw());
							  return true;
						  } catch (NumberFormatException ex) {return false;}},
					e -> {penalties.put(warnings, new JSONArray().put(plannedpunish).put(e.getMessage().getContentRaw()));
						  event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "successtempmute").convert()).queue();});
			break;
		case "pm":
			penalties.put(warnings, new JSONArray().put(plannedpunish).put("0"));
			event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "successmute").convert()).queue();
			break;
		case "ki":
			penalties.put(warnings, new JSONArray().put(plannedpunish).put("0"));
			event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "successkick").convert()).queue();
			break;
		case "tb":
			event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "add3time").convert()).queue();
			ResponseDetector.waitForMessage(guild, user, op.getMessage(),
					e -> {try {
							  Integer.valueOf(e.getMessage().getContentRaw());
							  return true;
						  } catch (NumberFormatException ex) {return false;}},
					e -> {penalties.put(warnings, new JSONArray().put(plannedpunish).put(e.getMessage().getContentRaw()));
						  event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "successtempban").convert()).queue();});
			break;
		case "pb":
			penalties.put(warnings, new JSONArray().put(plannedpunish).put("0"));
			event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "successban").convert()).queue();
			break;
		default:
			event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "fatal").convert()).queue();
		}
	}
	
	private String listpenalties(OperationEvent event) {
		StringBuilder sB = new StringBuilder();
		JSONObject current = ConfigLoader.getGuildConfig(guild).getJSONObject("penalties");
		if (current.isEmpty()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nopenalties")).queue();
			return null;
		}
		current.keySet().forEach(e -> {
			sB.append("#" + e + ": ");
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
					event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "fatal")).queue();
			}
			sB.append("\n");
		});
		return sB.toString();
	}
}