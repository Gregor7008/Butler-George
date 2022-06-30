package operations;

import components.ResponseDetector;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.operations.OperationData;
import components.operations.OperationEvent;
import components.operations.OperationEventHandler;
import components.operations.SubOperationData;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class SupportTalk implements OperationEventHandler {

	@Override
	public void execute(OperationEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		if (event.getSubOperation().equals("set")) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defchannel")).queue();
			ResponseDetector.waitForMessage(guild, user, event.getChannel(),
					e -> {if (!e.getMessage().getMentions().getChannels().isEmpty()) {
						 	 return e.getMessage().getMentions().getChannels().get(0).getType().isAudio();
					} else {return false;}}, 
					e -> {
						ConfigLoader.getGuildConfig(guild).put("supporttalk", e.getMessage().getMentions().getChannels().get(0).getIdLong());
						event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "setsuccess").convert()).queue();
						return;
					});
		}
		if (event.getSubOperation().equals("clear")) {
			ConfigLoader.getGuildConfig(guild).put("supporttalk", 0L);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "clearsuccess").convert()).queue();
		}
	}

	@Override
	public OperationData initialize() {
		OperationData operationData = new OperationData(this).setName("SupportTalk")
															 .setInfo("Configure a voice channel for voice support")
															 .setMinimumPermission(Permission.MANAGE_SERVER)
															 .setCategory(OperationData.ADMINISTRATION)
															 .setSubOperations(new SubOperationData[] {
																	 new SubOperationData("set", "Set a voice channel as the support talk"),
																	 new SubOperationData("clear", "Undefine the support talk")
															 });
		return operationData;
	}
}