package components.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public abstract class Replyable {

	protected Message message = null;
	
	public MessageAction replyEmbeds(MessageEmbed embed) {
		return this.message.editMessageEmbeds(embed);
	}
	
	public MessageAction replyEmbeds(CustomMessageEmbed embed) {
		return this.message.editMessageEmbeds(embed.convert());
	}
	
	public MessageAction replyEmbeds(Collection<CustomMessageEmbed> embeds) {
		List<MessageEmbed> embedsConverted = new ArrayList<>();
		embeds.forEach(e -> embedsConverted.add(e.convert()));
		return this.message.editMessageEmbeds(embedsConverted);
	}
}