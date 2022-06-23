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

public class LevelChannel implements OperationEventHandler {

	@Override
	public void execute(OperationEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (event.getSubOperation().getName().equals("set")) {
			ConfigLoader.getGuildConfig(guild).put("communityinbox", event.getOptionAsChannel(0).getIdLong());
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "setsuccess")).queue();
			return;
		}
		if (event.getSubOperation().getName().equals("clear")) {
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