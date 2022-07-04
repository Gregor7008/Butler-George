package operations;

import components.ResponseDetector;
import components.Toolbox;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.operations.OperationData;
import components.operations.OperationEvent;
import components.operations.OperationEventHandler;
import components.operations.SubOperationData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class Inbox implements OperationEventHandler {

	@Override
	public void execute(OperationEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "seltype"))
						  .setActionRow(Button.primary("community", Emoji.fromUnicode("\uD83C\uDF89")),
								  	    Button.primary("suggestion", Emoji.fromUnicode("\uD83D\uDCA1")),
								  	    Button.primary("moderation", Emoji.fromUnicode("\uD83D\uDC6E"))).queue();
		ResponseDetector.waitForButtonClick(guild, user, event.getMessage(), null,
				b -> {final String selection = b.getComponentId();
					  Toolbox.deleteActionRows(b.getMessage(),
							 ()  -> {
								 if (event.getSubOperation().equals("set")) {
									  b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defchannel").replaceDescription("{selection}", selection).convert()).queue();
									  ResponseDetector.waitForMessage(guild, user, event.getChannel(),
											  e -> {if (!e.getMessage().getMentions().getChannels().isEmpty()) {
												  		return guild.getTextChannelById(e.getMessage().getMentions().getChannels().get(0).getIdLong()) != null;
												  	} else {return false;}
											  },
											  e -> {
												  long id = e.getMessage().getMentions().getChannels().get(0).getIdLong();
												  ConfigLoader.getGuildConfig(guild).put(selection + "inbox", id);
												  b.getMessage().editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "setsuccess")
														  .replaceDescription("{selection}", selection)
														  .replaceDescription("{channel}", guild.getTextChannelById(id).getAsMention()).convert()).queue();
												  e.getMessage().delete().queue();
												  return;
											  });
								  }
								  if (event.getSubOperation().equals("clear")) {
									  ConfigLoader.getGuildConfig(guild).put("communityinbox", 0L);
									  b.editMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "clearsuccess").replaceDescription("{selection}", selection).convert()).queue();
								  }
							 });
				});
	}

	@Override
	public OperationData initialize() {
		OperationData operationData = new OperationData(this).setName("Inbox")
													.setInfo("Configure inbox channels for different receivers/purposes")
													.setSubOperations(new SubOperationData[] {
															new SubOperationData("set", "Set an inbox channel for a selected use case"),
															new SubOperationData("clear", "Undefine an inbox channel")
													});
		return operationData;
	}
}