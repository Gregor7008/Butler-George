
package actions.administration;

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

public class ReportChannel implements ActionRequest {

	@Override
	public void execute(Action event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (event.getSubAction().getName().equals("set")) {
			ConfigLoader.getGuildConfig(guild).put("reportchannel", event.getSubAction().getOptionAsChannel(0).getIdLong());
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "/commands/moderation/reportchannel:setsuccess"));
			return;
		}
		if (event.getSubAction().getName().equals("clear")) {
			ConfigLoader.getGuildConfig(guild).put("reportchannel", 0L);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "/commands/moderation/reportchannel:clearsuccess"));
		}
	}

	@Override
	public ActionData initialize() {
		ActionData actionData = new ActionData(this).setName("ReportChannel")
													.setInfo("Configure a channel for incoming reports for your server")
													.setMinimumPermission(Permission.MANAGE_CHANNEL)
													.setCategory(ActionData.ADMINISTRATION)
													.setSubActions(new SubActionData[] {
															new SubActionData("set", OptionType.CHANNEL),
															new SubActionData("clear")
													});
		return actionData;
	}
}