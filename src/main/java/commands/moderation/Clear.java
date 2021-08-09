package commands.moderation;

import java.util.List;
import java.util.concurrent.TimeUnit;

import components.AnswerEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class Clear {

	public Clear(Guild guild, Member member, TextChannel channel, String argument) {
		List<Message> messages = channel.getHistory().retrievePast(Integer.parseInt(argument)+1).complete();
		channel.deleteMessages(messages).queue();
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/clear:done", guild, member)).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));
	}

}
