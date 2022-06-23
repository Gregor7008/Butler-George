
package operations.administration;

import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.operations.OperationData;
import components.operations.OperationEvent;
import components.operations.OperationEventHandler;
import components.operations.SubActionData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class ReportChannel implements OperationEventHandler {

	@Override
	public void execute(OperationEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (event.getSubOperation().getName().equals("set")) {
			ConfigLoader.getGuildConfig(guild).put("reportchannel", event.getSubOperation().getOptionAsChannel(0).getIdLong());
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "setsuccess"));
			return;
		}
		if (event.getSubOperation().getName().equals("clear")) {
			ConfigLoader.getGuildConfig(guild).put("reportchannel", 0L);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "clearsuccess"));
		}
	}

	@Override
	public OperationData initialize() {
		OperationData operationData = new OperationData(this).setName("ReportChannel")
													.setInfo("Configure a channel for incoming reports for your server")
													.setMinimumPermission(Permission.MANAGE_CHANNEL)
													.setCategory(OperationData.ADMINISTRATION)
													.setSubActions(new SubActionData[] {
															new SubActionData("set", OptionType.CHANNEL),
															new SubActionData("clear")
													});
		return operationData;
	}
}