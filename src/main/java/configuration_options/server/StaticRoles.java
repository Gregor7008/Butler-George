package configuration_options.server;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import base.assets.AwaitTask;
import base.engines.LanguageEngine;
import base.engines.Toolbox;
import base.engines.configs.ConfigLoader;
import configuration_options.assets.ConfigurationEvent;
import configuration_options.assets.ConfigurationEventHandler;
import configuration_options.assets.ConfigurationOptionData;
import configuration_options.assets.ConfigurationSubOptionData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class StaticRoles implements ConfigurationEventHandler {

	private Guild guild;
	private User user;
	
	@Override
	public void execute(ConfigurationEvent event) {
		guild = event.getGuild();
		user = event.getUser();
		event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "seltype")).setActionRow(
				Button.secondary("adminroles", Emoji.fromUnicode("\u2696")),
				Button.secondary("moderationroles", Emoji.fromUnicode("\uD83D\uDC6E")),
				Button.secondary("supportroles", Emoji.fromUnicode("\uD83D\uDEA8")),
				Button.secondary("customchannelaccessroles", Emoji.fromUnicode("\uD83D\uDD12"))).queue();
		AwaitTask.forButtonInteraction(guild, user, event.getMessage(),
				b -> {
					String type = b.getComponentId();
					if (event.getSubOperation().equals("list")) {
						this.listroles(b, type);
						return;
					}
					if (event.getSubOperation().equals("remove")) {
						ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONArray(type).clear();
						b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "remsuccess").replaceDescription("{type}", type)).setActionRows().queue();
						return;
					}
					b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defroles")).setActionRows().queue();
					AwaitTask.forMessageReceival(guild, user, event.getChannel(),
							e -> {return !e.getMessage().getMentions().getRoles().isEmpty();},
							e -> {
								JSONArray ccdefaccessroles = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONArray(type);
								List<Long> roleIDs = new ArrayList<Long>();
								e.getMessage().getMentions().getRoles().forEach(r -> roleIDs.add(r.getIdLong()));
								if (event.getSubOperation().equals("add")) {
									for (int i = 0; i < roleIDs.size(); i++) {
										if (!ccdefaccessroles.toList().contains(roleIDs.get(i))) {
											ccdefaccessroles.put(roleIDs.get(i));
										}
									}
									event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "addsuccess").replaceDescription("{type}", type)).queue();
								}
								if (event.getSubOperation().equals("delete")) {
									for (int i = 0; i < roleIDs.size(); i++) {
										Toolbox.removeValueFromArray(ccdefaccessroles, roleIDs.get(i));
									}
									event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "delsuccess").replaceDescription("{type}", type)).queue();
								}
							}, null).append();
				}).append();
	}

	@Override
	public ConfigurationOptionData initialize() {
		ConfigurationOptionData configurationOptionData = new ConfigurationOptionData(this).setName("StaticRoles")
													.setInfo("Configure the roles for different areas of responsibility")
													.setSubOperations(new ConfigurationSubOptionData[] {
				  											new ConfigurationSubOptionData("add", "Add one or more roles"),
				  											new ConfigurationSubOptionData("delete", "Delete one role from the active ones"),
				  											new ConfigurationSubOptionData("remove", "Remove all roles"),
				  											new ConfigurationSubOptionData("list", "List all active roles")
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
	
	private void listroles(ButtonInteractionEvent event, String type) {
		StringBuilder sB = new StringBuilder();
		JSONArray botautoroles = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONArray(type);
		if (botautoroles.isEmpty()) {
			event.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "none").replaceDescription("{type}", type)).queue();
			return;
		}
		for (int i = 0; i < botautoroles.length(); i++) {
			sB.append('#')
			  .append(String.valueOf(i) + "\s\s");
			if (i+1 == botautoroles.length()) {
				sB.append(guild.getRoleById(botautoroles.getLong(i)).getAsMention());
			} else {
				sB.append(guild.getRoleById(botautoroles.getLong(i)).getAsMention() + "\n");
			}
		}
		event.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "list")
				.replaceDescription("{list}", sB.toString())
				.replaceDescription("{type}", type)).queue();
	}
}
