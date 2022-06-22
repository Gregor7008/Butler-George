package operations.administration;

import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.operation.OperationEvent;
import components.operation.OperationRequest;
import components.operation.OperationData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class SupportTalk implements OperationRequest {

	@Override
	public void execute(OperationEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		if (event.getSubAction().getName().equals("set")) {
			ConfigLoader.getGuildConfig(guild).put("supporttalk", event.getOptionAsChannel(0).getIdLong());
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "setsuccess").convert()).queue();
			return;
		}
		if (event.getSubAction().getName().equals("clear")) {
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