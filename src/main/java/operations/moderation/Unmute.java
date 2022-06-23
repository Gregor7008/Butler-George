package operations.moderation;

import org.json.JSONObject;

import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.commands.moderation.ModEngine;
import components.operations.OperationData;
import components.operations.OperationEvent;
import components.operations.OperationEventHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class Unmute implements OperationEventHandler {

	@Override
	public void execute(OperationEvent event) {
		final Guild guild = event.getGuild();
		final User user =  event.getUser();
		final User cuser = event.getOptionAsUser(0);
		JSONObject userconfig = ConfigLoader.getMemberConfig(guild, cuser);
		if (!userconfig.getBoolean("muted") && !userconfig.getBoolean("tempmuted")) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nomute").convert()).queue();
			return;
		}
		userconfig.put("muted", false);
		userconfig.put("tempmuted", false);
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "success").convert()).queue();
		ModEngine.run.guildModCheck(guild);
	}

	@Override
	public OperationData initialize() {
		//TODO Initialize Unmute
		return null;
	}
}