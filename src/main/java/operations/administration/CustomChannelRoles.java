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

public class CustomChannelRoles implements OperationRequest {

	@Override
	public void execute(OperationEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		JSONArray ccroles = ConfigLoader.getGuildConfig(guild).getJSONArray("customchannelroles");
		long roleID = event.getSubAction().getOptionAsRole(0).getIdLong();
		if (event.getSubAction().getName().equals("set")) {
			ccroles.clear();
			ccroles.put(roleID);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "setsuccess")).queue();
			return;
		}
		if (event.getSubAction().getName().equals("add")) {
			ccroles.put(roleID);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "addsuccess")).queue();
			return;
		}
		if (event.getSubAction().getName().equals("remove")) {
			ConfigLoader.removeValueFromArray(ccroles, roleID);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "remsuccess")).queue();
			return;
		}
		if (event.getSubAction().getName().equals("clear")) {
			ccroles.clear();
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "clearsuccess")).queue();
			return;
		}
	}

	@Override
	public OperationData initialize() {
		OperationData operationData = new OperationData(this).setName("CustomChannelRoles")
													.setInfo("Configure the roles that should be able to create custom channels")
													.setMinimumPermission(Permission.MANAGE_SERVER)
													.setCategory(OperationData.ADMINISTRATION)
													.setSubActions(new SubActionData[] {
															new SubActionData("set", OptionType.ROLE),
															new SubActionData("add", OptionType.ROLE),
															new SubActionData("remove", OptionType.ROLE),
															new SubActionData("clear", OptionType.ROLE)
													});
		return operationData;
	}
}