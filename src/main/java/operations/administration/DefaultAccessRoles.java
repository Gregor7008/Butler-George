package operations.administration;

import org.json.JSONArray;

import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.operation.OperationEvent;
import components.operation.OperationRequest;
import components.operation.OperationData;
import components.operation.SubActionData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class DefaultAccessRoles implements OperationRequest {

	@Override
	public void execute(OperationEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		JSONArray ccdefroles = ConfigLoader.getGuildConfig(guild).getJSONArray("customchannelaccessroles");
		long roleID = event.getOptionAsRole(0).getIdLong();
		if (event.getSubAction().getName().equals("set")) {
			ccdefroles.clear();
			ccdefroles.put(roleID);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "setsuccess")).queue();
			return;
		}
		if (event.getSubAction().getName().equals("add")) {
			ccdefroles.put(roleID);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "addsuccess")).queue();
			return;
		}
		if (event.getSubAction().getName().equals("remove")) {
			ConfigLoader.removeValueFromArray(ccdefroles, roleID);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "remsuccess")).queue();
			return;
		}
		if (event.getSubAction().getName().equals("clear")) {
			ccdefroles.clear();
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "clearsuccess")).queue();
			return;
		}
	}

	@Override
	public OperationData initialize() {
		OperationData operationData = new OperationData(this).setName("DefaultAccessRoles")
													.setInfo("Configure the roles that should have access to channels of users by default")
													.setMinimumPermission(Permission.MANAGE_SERVER)
													.setCategory(OperationData.ADMINISTRATION)
													.setSubActions(new SubActionData[] {
															new SubActionData("set", OptionType.ROLE),
															new SubActionData("add", OptionType.ROLE),
															new SubActionData("remove", OptionType.ROLE),
															new SubActionData("clear")
													});
		return operationData;
	}
}
