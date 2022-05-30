package components.moderation;

import org.json.JSONArray;

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
		JSONArray forbiddenwords = ConfigLoader.run.getGuildConfig(event.getGuild()).getJSONArray("forbiddenwords");
		for (int i = 0; i < forbiddenwords.length(); i++) {
			if (event.getMessage().getContentRaw().toLowerCase().contains(forbiddenwords.getString(i).toLowerCase())) {
				ConfigLoader.run.getUserConfig(event.getGuild(), event.getAuthor()).getJSONArray("warnings").put("Rude behavior");
				try {
					event.getMember().getUser().openPrivateChannel().complete()
						 .sendMessageEmbeds(AnswerEngine.ae.fetchMessage(event.getGuild(), event.getAuthor(), "/components/moderation/automoderator:warning")
								 .replaceDescription("{guild}", event.getGuild().getName()).convert()).queue();
				} catch (Exception e) {e.printStackTrace();}
				event.getMessage().delete().queue();
				Bot.INSTANCE.penaltyCheck(event.getGuild());
				i = forbiddenwords.length();
			}
		}
	}
}