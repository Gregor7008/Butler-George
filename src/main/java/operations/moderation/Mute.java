package operations.moderation;

import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.commands.moderation.ModEngine;
import components.operations.OperationData;
import components.operations.OperationEvent;
import components.operations.OperationEventHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class Mute implements OperationEventHandler {

	@Override
	public void execute(OperationEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getOptionAsUser(0);
		ConfigLoader.getMemberConfig(guild, user).put("muted", true);
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "success").replaceDescription("{user}", user.getName())).queue();
		ModEngine.run.guildModCheck(guild);
	}

	@Override
	public OperationData initialize() {
		OperationData operationData = new OperationData(this).setName("Mute")
													.setInfo("Mute a member permanently")
													.setMinimumPermission(Permission.MESSAGE_MANAGE)
													.setCategory(OperationData.MODERATION);
		return operationData;
	}
}