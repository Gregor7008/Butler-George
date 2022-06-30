package operations.moderation;

import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.commands.moderation.ModEngine;
import components.operations.OperationData;
import components.operations.OperationEvent;
import components.operations.OperationEventHandler;
import components.utilities.ResponseDetector;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class Mute implements OperationEventHandler {

	@Override
	public void execute(OperationEvent event) {
		final Guild guild = event.getGuild();
		final User ruser = event.getUser();
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, ruser, this, "defuser")).queue();
		ResponseDetector.waitForMessage(guild, ruser, event.getChannel(),
				e -> {return !e.getMessage().getMentions().getUsers().isEmpty();},
				e -> {User user = e.getMessage().getMentions().getUsers().get(0);
					  ConfigLoader.getMemberConfig(guild, user).put("muted", true);
					  event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "success").replaceDescription("{user}", user.getName())).queue();
					  ModEngine.run.guildModCheck(guild);
				});
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