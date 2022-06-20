package actions;

import org.json.JSONArray;

import components.actions.Action;
import components.actions.ActionData;
import components.actions.ActionRequest;
import components.actions.SubActionData;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class DefaultAccessRoles implements ActionRequest {

	@Override
	public void execute(Action event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		JSONArray ccdefroles = ConfigLoader.getGuildConfig(guild).getJSONArray("customchannelaccessroles");
		long roleID = event.getOptionAsRole(0).getIdLong();
		if (event.getSubAction().getName().equals("set")) {
			ccdefroles.clear();
			ccdefroles.put(roleID);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/defaultaccessroles:setsuccess"));
			return;
		}
		if (event.getSubAction().getName().equals("add")) {
			ccdefroles.put(roleID);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/defaultaccessroles:addsuccess"));
			return;
		}
		if (event.getSubAction().getName().equals("remove")) {
			ConfigLoader.removeValueFromArray(ccdefroles, roleID);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/defaultaccessroles:remsuccess"));
			return;
		}
		if (event.getSubAction().getName().equals("clear")) {
			ccdefroles.clear();
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/defaultaccessroles:clearsuccess"));
			return;
		}
	}

	@Override
	public ActionData initialize() {
		ActionData actionData = new ActionData(this).setName("DefaultAccessRoles")
													.setInfo("Configure the roles that should have access to channels of users by default")
													.setMinimumPermission(Permission.MANAGE_SERVER)
													.setCategory(ActionData.ADMINISTRATION)
													.setSubActions(new SubActionData[] {
															new SubActionData("set", OptionType.ROLE),
															new SubActionData("add", OptionType.ROLE),
															new SubActionData("remove", OptionType.ROLE),
															new SubActionData("clear")
													});
		return actionData;
	}
}
