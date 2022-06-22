package operations.administration;

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

public class LevelChannel implements OperationRequest {

	@Override
	public void execute(OperationEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (event.getSubAction().getName().equals("set")) {
			ConfigLoader.getGuildConfig(guild).put("communityinbox", event.getOptionAsChannel(0).getIdLong());
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "setsuccess")).queue();
			return;
		}
		if (event.getSubAction().getName().equals("clear")) {
			ConfigLoader.getGuildConfig(guild).put("communityinbox", 0L);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "clearsuccess")).queue();
		}
	}

	@Override
	public OperationData initialize() {
		OperationData operationData = new OperationData(this).setName("LevelChannel")
													.setInfo("Configure a channel for level-up messsages")
													.setMinimumPermission(Permission.MANAGE_SERVER)
													.setCategory(OperationData.ADMINISTRATION)
													.setSubActions(new SubActionData[] {
															new SubActionData("set", OptionType.CHANNEL),
															new SubActionData("clear", OptionType.CHANNEL)
													});
		return operationData;
	}
}