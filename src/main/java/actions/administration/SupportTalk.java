package actions.administration;

import components.actions.Action;
import components.actions.ActionData;
import components.actions.ActionRequest;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class SupportTalk implements ActionRequest {

	@Override
	public void execute(Action event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		if (event.getSubAction().getName().equals("set")) {
			ConfigLoader.getGuildConfig(guild).put("supporttalk", event.getOptionAsChannel(0).getIdLong());
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "/commands/moderation/supporttalk:setsuccess").convert()).queue();
			return;
		}
		if (event.getSubAction().getName().equals("clear")) {
			ConfigLoader.getGuildConfig(guild).put("supporttalk", 0L);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "/commands/moderation/supporttalk:clearsuccess").convert()).queue();
		}
	}

	@Override
	public ActionData initialize() {
		//TODO Initialize SupportTalk
		return null;
	}
}