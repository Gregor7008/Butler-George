package actions.administration;

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

public class CustomChannelRoles implements ActionRequest {

	@Override
	public void execute(Action event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		JSONArray ccroles = ConfigLoader.getGuildConfig(guild).getJSONArray("customchannelroles");
		long roleID = event.getSubAction().getOptionAsRole(0).getIdLong();
		if (event.getSubAction().getName().equals("set")) {
			ccroles.clear();
			ccroles.put(roleID);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/customchannelroles:setsuccess")).queue();
			return;
		}
		if (event.getSubAction().getName().equals("add")) {
			ccroles.put(roleID);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/customchannelroles:addsuccess")).queue();
			return;
		}
		if (event.getSubAction().getName().equals("remove")) {
			ConfigLoader.removeValueFromArray(ccroles, roleID);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/customchannelroles:remsuccess")).queue();
			return;
		}
		if (event.getSubAction().getName().equals("clear")) {
			ccroles.clear();
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, "/commands/moderation/customchannelroles:clearsuccess")).queue();
			return;
		}
	}

	@Override
	public ActionData initialize() {
		ActionData actionData = new ActionData(this).setName("CustomChannelRoles")
													.setInfo("Configure the roles that should be able to create custom channels")
													.setMinimumPermission(Permission.MANAGE_SERVER)
													.setCategory(ActionData.ADMINISTRATION)
													.setSubActions(new SubActionData[] {
															new SubActionData("set", OptionType.ROLE),
															new SubActionData("add", OptionType.ROLE),
															new SubActionData("remove", OptionType.ROLE),
															new SubActionData("clear", OptionType.ROLE)
													});
		return actionData;
	}
}