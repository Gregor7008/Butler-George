package components.moderation;

import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class AutoModerator {

	private static AutoModerator INSTANCE;
	
	public static AutoModerator getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new AutoModerator();
		}
		return INSTANCE;
	}
	
	public void messagereceived(GuildMessageReceivedEvent event) {
		String forbiddenwords = Configloader.INSTANCE.getGuildConfig(event.getGuild(), "forbidden");
		String[] singleword = forbiddenwords.split(";");
		for (int i = 0; i < singleword.length; i++) {
			if (event.getMessage().getContentRaw().toLowerCase().contains(singleword[i].toLowerCase())) {
				Configloader.INSTANCE.addUserConfig(event.getMember(), "warnings", "Rude behavior");
				try {
					event.getMember().getUser().openPrivateChannel().queue(channel -> {
						 channel.sendMessageEmbeds(AnswerEngine.getInstance().buildMessage(":warning: You have been warned :warning:", "Server:\n=> " + event.getGuild().getName() + "\nReason:\n=> Rude behavior")).queue();});
				} catch (Exception e) {e.printStackTrace();}
				AutoPunishEngine.getInstance().processWarnings(event.getGuild());
				event.getMessage().delete().queue();
			}
		}
	}
}
