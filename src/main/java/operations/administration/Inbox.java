package operations.administration;

import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.operations.OperationData;
import components.operations.OperationEvent;
import components.operations.OperationEventHandler;
import components.operations.SubOperationData;
import components.utilities.ResponseDetector;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class Inbox implements OperationEventHandler {

	@Override
	public void execute(OperationEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "seltype"))
						  .setActionRow(Button.primary("community", Emoji.fromMarkdown(":tada:")),
								  	    Button.primary("suggestion", Emoji.fromMarkdown(":bulb:")),
								  	    Button.primary("moderation", Emoji.fromMarkdown(":cop:"))).queue();
		ResponseDetector.waitForButtonClick(guild, user, event.getMessage(), null,
				b -> {String selection = b.getId();
					  if (event.getSubOperation().equals("set")) {
						  event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defchannel").replaceDescription("{selection}", selection)).queue();
						  ResponseDetector.waitForMessage(guild, user, event.getChannel(),
								  e -> {return !e.getMessage().getMentions().getChannels().isEmpty();},
								  e -> {
									  ConfigLoader.getGuildConfig(guild).put(selection + "inbox", e.getMessage().getMentions().getChannels().get(0).getIdLong());
									  event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "setsuccess").replaceDescription("{selection}", selection)).queue();
									  return;
								  });
					  }
					  if (event.getSubOperation().equals("clear")) {
						  ConfigLoader.getGuildConfig(guild).put("communityinbox", 0L);
						  event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "clearsuccess").replaceDescription("{selection}", selection)).queue();
					  }
				});
	}

	@Override
	public OperationData initialize() {
		OperationData operationData = new OperationData(this).setName("Inbox")
													.setInfo("Configure inbox channels for different receivers/purposes")
													.setMinimumPermission(Permission.MANAGE_SERVER)
													.setCategory(OperationData.ADMINISTRATION)
													.setSubOperations(new SubOperationData[] {
															new SubOperationData("set", "Set an inbox channel for a selected use case"),
															new SubOperationData("clear", "Undefine an inbox channel")
													});
		return operationData;
	}
}