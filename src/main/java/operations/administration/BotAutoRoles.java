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

public class BotAutoRoles implements OperationEventHandler {

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
			ConfigLoader.getGuildConfig(guild).getJSONArray("botautoroles").clear();
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "remsuccess")).queue();
			return;
		}
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defroles")).queue();
		ResponseDetector.waitForMessage(guild, user, event.getChannel(),
				e -> {return !e.getMessage().getMentions().getRoles().isEmpty();},
				e -> {
					JSONArray botautoroles = ConfigLoader.getGuildConfig(guild).getJSONArray("botautoroles");
					List<Long> roleIDs = new ArrayList<Long>();
					e.getMessage().getMentions().getRoles().forEach(r -> roleIDs.add(r.getIdLong()));
					if (event.getSubOperation().equals("add")) {
						for (int i = 0; i < roleIDs.size(); i++) {
							if (!botautoroles.toList().contains(roleIDs.get(i))) {
								botautoroles.put(roleIDs.get(i));
							}
						}
						event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "addsuccess")).queue();
					}
					if (event.getSubOperation().equals("delete")) {
						for (int i = 0; i < roleIDs.size(); i++) {
							Toolbox.removeValueFromArray(botautoroles, roleIDs.get(i));
						}
						event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "delsuccess")).queue();
					}
				});
	}

	@Override
	public OperationData initialize() {
		OperationData operationData = new OperationData(this).setName("BotAutoRoles")
				  									.setInfo("Configure roles that should be given to every new bot joining")
				  									.setMinimumPermission(Permission.MANAGE_ROLES)
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
		JSONArray botautoroles = ConfigLoader.getGuildConfig(guild).getJSONArray("botautoroles");
		if (botautoroles.isEmpty()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nobotautoroles")).queue();
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