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
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class AutoRoles implements ConfigurationEventHandler {

	private Guild guild;
	private User user;
	
	@Override
	public void execute(ConfigurationEvent event) {
		guild = event.getGuild();
		user = event.getUser();
		event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "selacc"))
				.setActionRow(Button.secondary("user", Emoji.fromUnicode("\uD83D\uDEB9")),
							  Button.secondary("bot", Emoji.fromUnicode("\uD83E\uDD16"))).queue();
		AwaitTask.forButtonInteraction(guild, user, event.getMessage(),
				b -> {
					String selection = b.getComponentId();
					if (event.getSubOperation().equals("list")) {
						this.listroles(b, selection);
						return;
					}
					if (event.getSubOperation().equals("remove")) {
						ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONArray(selection + "autoroles").clear();
						b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "remsuccess")).setActionRows().queue();
						return;
					}
					b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defroles")).setActionRows().queue();
					AwaitTask.forMessageReceival(guild, user, event.getChannel(),
							e -> {return !e.getMessage().getMentions().getRoles().isEmpty();},
							e -> {
								JSONArray autoroles = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONArray(selection + "autoroles");
								List<Long> roleIDs = new ArrayList<Long>();
								e.getMessage().getMentions().getRoles().forEach(r -> roleIDs.add(r.getIdLong()));
								if (event.getSubOperation().equals("add")) {
									for (int i = 0; i < roleIDs.size(); i++) {
										if (!autoroles.toList().contains(roleIDs.get(i))) {
											autoroles.put(roleIDs.get(i));
										}
									}
									event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, selection + "addsuccess")).queue();
								}
								if (event.getSubOperation().equals("delete")) {
									for (int i = 0; i < roleIDs.size(); i++) {
										Toolbox.removeValueFromArray(autoroles, roleIDs.get(i));
									}
									event.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, selection + "delsuccess")).queue();
								}
							}, null).append();
				}).append();
	}

	@Override
	public ConfigurationOptionData initialize() {
		ConfigurationOptionData configurationOptionData = new ConfigurationOptionData(this).setName("AutoRoles")
												    .setInfo("Configure roles that should be given to every new account joining")
				  									.setSubOperations(new ConfigurationSubOptionData[] {
				  											new ConfigurationSubOptionData("add", "Add one or more roles"),
				  											new ConfigurationSubOptionData("delete", "Delete one role from the active ones"),
				  											new ConfigurationSubOptionData("remove", "Remove all roles"),
				  											new ConfigurationSubOptionData("list", "List all active roles")
				  									});
		return configurationOptionData;
	}
	
	private void listroles(ButtonInteractionEvent event, String selection) {
		StringBuilder sB = new StringBuilder();
		JSONArray autoroles = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONArray(selection + "autoroles");
		if (autoroles.isEmpty()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, selection + "noautoroles")).queue();
			return;
		}
		for (int i = 0; i < autoroles.length(); i++) {
			sB.append('#')
			  .append(String.valueOf(i) + "\s\s");
			if (i+1 == autoroles.length()) {
				sB.append(guild.getRoleById(autoroles.getLong(i)).getAsMention());
			} else {
				sB.append(guild.getRoleById(autoroles.getLong(i)).getAsMention() + "\n");
			}
		}
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user,  this, selection + "list").replaceDescription("{list}", sB.toString())).queue();
	}
}