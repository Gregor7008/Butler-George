package components.moderation;

import base.Bot;
import components.base.AnswerEngine;
import components.base.ConfigLoader;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class AutoModerator {

	private static AutoModerator INSTANCE;
	
	public static AutoModerator getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new AutoModerator();
		}
		return INSTANCE;
	}
	
	public void messagereceived(MessageReceivedEvent event) {
		String forbiddenwords = ConfigLoader.cfl.getGuildConfig(event.getGuild(), "forbidden");
		String[] singleword = forbiddenwords.split(";");
		for (int i = 0; i < singleword.length; i++) {
			if (event.getMessage().getContentRaw().toLowerCase().contains(singleword[i].toLowerCase())) {
				ConfigLoader.cfl.addUserConfig(event.getGuild(), event.getAuthor(), "warnings", "Rude behavior");
				try {
					event.getMember().getUser().openPrivateChannel().complete()
						 .sendMessageEmbeds(AnswerEngine.ae.fetchMessage(event.getGuild(), event.getAuthor(), "/components/moderation/automoderator:warning")
								 .replaceDescription("{guild}", event.getGuild().getName()).convert()).queue();
				} catch (Exception e) {e.printStackTrace();}
				event.getMessage().delete().queue();
				Bot.INSTANCE.penaltyCheck(event.getGuild());
			}
		}
	}
}