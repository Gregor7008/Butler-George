package context;

import org.json.JSONObject;

import components.ResponseDetector;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.commands.moderation.ModController;
import components.operations.OperationData;
import components.operations.OperationEvent;
import components.operations.OperationEventHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class Unmute implements OperationEventHandler {

	@Override
	public void execute(OperationEvent event) {
		final Guild guild = event.getGuild();
		final User ruser = event.getUser();
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, ruser, this, "defuser")).queue();
		ResponseDetector.waitForMessage(guild, ruser, event.getChannel(),
				e -> {return !e.getMessage().getMentions().getUsers().isEmpty();},
				e -> {User user = e.getMessage().getMentions().getUsers().get(0);
					  JSONObject userconfig = ConfigLoader.getMemberConfig(guild, user);
					  if (!userconfig.getBoolean("muted") && !userconfig.getBoolean("tempmuted")) {
						  event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nomute").convert()).queue();
						  return;
					  }
					  userconfig.put("muted", false);
					  userconfig.put("tempmuted", false);
					  event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "success").convert()).queue();
					  ModController.run.guildModCheck(guild);
				});
		
	}

	@Override
	public OperationData initialize() {
		OperationData operationData = new OperationData(this).setName("Unmute")
															 .setInfo("Unmute a member")
															 .setMinimumPermission(Permission.MESSAGE_MANAGE)
															 .setCategory(OperationData.MODERATION);
		return operationData;
	}
}