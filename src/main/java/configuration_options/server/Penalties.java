package configuration_options.server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import base.assets.AwaitTask;
import base.engines.LanguageEngine;
import base.engines.configs.ConfigLoader;
import configuration_options.assets.ConfigurationEvent;
import configuration_options.assets.ConfigurationEventHandler;
import configuration_options.assets.ConfigurationOptionData;
import configuration_options.assets.ConfigurationSubOptionData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

public class Penalties implements ConfigurationEventHandler {

	private ConfigurationEvent event;
	private User user;
	private Guild guild;

	@Override
	public void execute(ConfigurationEvent event) {
		this.user = event.getUser();
		this.guild = event.getGuild();
		this.event = event;
		if (event.getSubOperation().equals("add")) {
			this.addpenalties1(event);
			return;
		}
		if (event.getSubOperation().equals("remove")) {
			ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONObject("penalties").clear();
			event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "remsuccess")).queue();
			return;
		}
		if (event.getSubOperation().equals("delete")) {
			this.deletepenalties(event);
			return;
		}
		if (event.getSubOperation().equals("list")) {
			String response = this.listpenalties(event);
			if (response != null) {
				event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "list").replaceDescription("{list}", response)).queue();
			}
		}
		
	}

	@Override
	public ConfigurationOptionData initialize() {
		ConfigurationOptionData configurationOptionData = new ConfigurationOptionData(this).setName("Penalties")
													.setInfo("Configure penalties for reaching a certain warning limit")
													.setSubOperations(new ConfigurationSubOptionData[] {
															new ConfigurationSubOptionData("add", "Add a new penalty for a specific amount of warnings"),
															new ConfigurationSubOptionData("delete", "Deactivate and delete one penalty"),
															new ConfigurationSubOptionData("remove", "Remove all active penalties"),
															new ConfigurationSubOptionData("list", "List all active penalties")
													});
		return configurationOptionData;
	}

	@Override
	public boolean checkBotPermissions(ConfigurationEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAvailableTo(Member member) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void deletepenalties(ConfigurationEvent event) {
		String response = this.listpenalties(event);
		if (response != null) {
			event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "remlist").replaceDescription("{list}", response)).queue();
			JSONObject penalties = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONObject("penalties");
			AwaitTask.forMessageReceival(guild, user, event.getChannel(),
					e -> {try {
							  penalties.getJSONArray(e.getMessage().getContentRaw());
							  penalties.remove(e.getMessage().getContentRaw());
							  event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "delsuccess")).queue();
					      } catch (JSONException ex) {
					    	  event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nodelval")).queue();
					      }}).append();
		}
	}
	
	private void addpenalties1(ConfigurationEvent event) {
		SelectMenu menu = SelectMenu.create("selpen")
				.addOption("Removal of role", "rr")
				.addOption("Temporary mute", "tm")
				.addOption("Permanent mute", "pm")
				.addOption("Kick", "ki")
				.addOption("Temporary ban", "tb")
				.addOption("Permanent ban", "pb")
				.build();
		event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "add1")).setActionRows(ActionRow.of(menu)).queue();
		AwaitTask.forSelectMenuInteraction(guild, user, event.getMessage(),
				e -> {String plannedpunish = e.getSelectedOptions().get(0).getValue();
					  this.addpenalties2(plannedpunish, e, event);})
		.addValidComponents(menu.getId()).append();;
	}
	
	private void addpenalties2(String plannedpunish, SelectMenuInteractionEvent event, ConfigurationEvent op) {
			event.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "add2")).setActionRows().queue();
			AwaitTask.forMessageReceival(guild, user, op.getChannel(),
					e -> {try {
						      Integer.valueOf(e.getMessage().getContentRaw());
						      return true;
						  } catch (NumberFormatException ex) {return false;}},
					e -> {this.addpenalties3(plannedpunish, e.getMessage().getContentRaw(), op);}, null).append();;
	}
	
	private void addpenalties3(String plannedpunish, String warnings, ConfigurationEvent op) {
		JSONObject penalties = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONObject("penalties");
		try {
			penalties.getJSONArray(warnings);
			event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "error")).queue();
			return;
		} catch (JSONException e) {}
		switch (plannedpunish) {
		case "rr":
			event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "add3role")).queue();
			AwaitTask.forMessageReceival(guild, user, op.getChannel(),
					e -> {return !e.getMessage().getMentions().getRoles().isEmpty();},
					e -> {penalties.put(warnings, new JSONArray().put(plannedpunish).put(e.getMessage().getMentions().getRoles().get(0).getId()));
						  event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "successrole")).queue();}, null).append();
			break;
		case "tm":
			event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "add3time")).queue();
			AwaitTask.forMessageReceival(guild, user, op.getChannel(),
					e -> {try {
							  Integer.valueOf(e.getMessage().getContentRaw());
							  return true;
						  } catch (NumberFormatException ex) {return false;}},
					e -> {penalties.put(warnings, new JSONArray().put(plannedpunish).put(e.getMessage().getContentRaw()));
						  event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "successtempmute")).queue();}, null).append();
			break;
		case "pm":
			penalties.put(warnings, new JSONArray().put(plannedpunish).put("0"));
			event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "successmute")).queue();
			break;
		case "ki":
			penalties.put(warnings, new JSONArray().put(plannedpunish).put("0"));
			event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "successkick")).queue();
			break;
		case "tb":
			event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "add3time")).queue();
			AwaitTask.forMessageReceival(guild, user, op.getChannel(),
					e -> {try {
							  Integer.valueOf(e.getMessage().getContentRaw());
							  return true;
						  } catch (NumberFormatException ex) {return false;}},
					e -> {penalties.put(warnings, new JSONArray().put(plannedpunish).put(e.getMessage().getContentRaw()));
						  event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "successtempban")).queue();}, null).append();
			break;
		case "pb":
			penalties.put(warnings, new JSONArray().put(plannedpunish).put("0"));
			event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "successban")).queue();
			break;
		default:
			event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "fatal")).queue();
		}
	}
	
	private String listpenalties(ConfigurationEvent event) {
		StringBuilder sB = new StringBuilder();
		JSONObject current = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONObject("penalties");
		if (current.isEmpty()) {
			event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nopenalties")).queue();
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
					event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "fatal")).queue();
			}
			sB.append("\n");
		});
		return sB.toString();
	}
}