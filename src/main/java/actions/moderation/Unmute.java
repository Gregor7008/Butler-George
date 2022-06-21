package actions.moderation;

import org.json.JSONObject;

import components.actions.Action;
import components.actions.ActionData;
import components.actions.ActionRequest;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.commands.moderation.ModEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class Unmute implements ActionRequest {

	@Override
	public void execute(Action event) {
		final Guild guild = event.getGuild();
		final User user =  event.getUser();
		final User cuser = event.getOptionAsUser(0);
		JSONObject userconfig = ConfigLoader.getMemberConfig(guild, cuser);
		if (!userconfig.getBoolean("muted") && !userconfig.getBoolean("tempmuted")) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "/commands/moderation/unmute:nomute").convert()).queue();
			return;
		}
		userconfig.put("muted", false);
		userconfig.put("tempmuted", false);
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "/commands/moderation/unmute:success").convert()).queue();
		ModEngine.run.guildModCheck(guild);
	}

	@Override
	public ActionData initialize() {
		//TODO Initialize Unmute
		return null;
	}
}