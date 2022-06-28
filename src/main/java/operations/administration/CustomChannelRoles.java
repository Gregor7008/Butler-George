package operations.administration;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.operations.OperationData;
import components.operations.OperationEvent;
import components.operations.OperationEventHandler;
import components.operations.SubOperationData;
import components.utilities.ResponseDetector;
import components.utilities.Toolbox;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class CustomChannelRoles implements OperationEventHandler {

	private Guild guild;
	private User user;
	
	@Override
	public void execute(OperationEvent event) {
		guild = event.getGuild();
		user = event.getUser();
		if (event.getSubOperation().equals("list")) {
			this.listroles(event);
			return;
		}
		if (event.getSubOperation().equals("remove")) {
			ConfigLoader.getGuildConfig(guild).getJSONArray("customchannelroles").clear();
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "remsuccess")).queue();
			return;
		}
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defroles")).queue();
		ResponseDetector.waitForMessage(guild, user, event.getChannel(),
				e -> {return !e.getMessage().getMentions().getRoles().isEmpty();},
				e -> {
					JSONArray customchannelroles = ConfigLoader.getGuildConfig(guild).getJSONArray("customchannelroles");
					List<Long> roleIDs = new ArrayList<Long>();
					e.getMessage().getMentions().getRoles().forEach(r -> roleIDs.add(r.getIdLong()));
					if (event.getSubOperation().equals("add")) {
						for (int i = 0; i < roleIDs.size(); i++) {
							if (!customchannelroles.toList().contains(roleIDs.get(i))) {
								customchannelroles.put(roleIDs.get(i));
							}
						}
						event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "addsuccess")).queue();
					}
					if (event.getSubOperation().equals("delete")) {
						for (int i = 0; i < roleIDs.size(); i++) {
							Toolbox.removeValueFromArray(customchannelroles, roleIDs.get(i));
						}
						event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "delsuccess")).queue();
					}
				});
	}

	@Override
	public OperationData initialize() {
		OperationData operationData = new OperationData(this).setName("CustomChannelRoles")
													.setInfo("Configure the roles that should be able to create custom user channels")
													.setMinimumPermission(Permission.MANAGE_SERVER)
													.setCategory(OperationData.ADMINISTRATION)
													.setSubOperations(new SubOperationData[] {
				  											new SubOperationData("add", "Add one or more roles"),
				  											new SubOperationData("delete", "Delete one role from the active ones"),
				  											new SubOperationData("remove", "Remove all roles"),
				  											new SubOperationData("list", "List all active roles")
				  									});
		return operationData;
	}
	
	private void listroles(OperationEvent event) {
		StringBuilder sB = new StringBuilder();
		JSONArray botautoroles = ConfigLoader.getGuildConfig(guild).getJSONArray("customchannelroles");
		if (botautoroles.isEmpty()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "noccroles")).queue();
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
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "list").replaceDescription("{list}", sB.toString())).queue();
	}
}