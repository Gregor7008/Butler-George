package commands.moderation;

import java.util.List;
import java.util.concurrent.TimeUnit;

import commands.Commands;
import components.AnswerEngine;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Clear implements Commands{

	@Override
	public void perform(GuildMessageReceivedEvent event, String arguments) {
		event.getMessage().delete().queue();
		List<Message> messages = event.getChannel().getHistory().retrievePast(Integer.parseInt(arguments)).complete();
		event.getChannel().deleteMessages(messages).queue();
		AnswerEngine.getInstance().fetchMessage("/commands/moderation/clear:done", event).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));
	}

}
