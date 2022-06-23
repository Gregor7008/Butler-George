package operations.administration;

import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.operations.OperationData;
import components.operations.OperationEvent;
import components.operations.OperationEventHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class SupportTalk implements OperationEventHandler {

	@Override
	public void execute(OperationEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		if (event.getSubOperation().getName().equals("set")) {
			ConfigLoader.getGuildConfig(guild).put("supporttalk", event.getOptionAsChannel(0).getIdLong());
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "setsuccess").convert()).queue();
			return;
		}
		if (event.getSubOperation().getName().equals("clear")) {
			ConfigLoader.getGuildConfig(guild).put("supporttalk", 0L);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "clearsuccess").convert()).queue();
		}
	}

	@Override
	public OperationData initialize() {
		//TODO Initialize SupportTalk
		return null;
	}
}