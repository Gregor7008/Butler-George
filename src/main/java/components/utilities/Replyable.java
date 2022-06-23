package components.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.RestAction;

public abstract class Replyable {

	protected Message message = null;
	
	public RestAction<Message> replyEmbeds(MessageEmbed embed) {
		return this.message.editMessageEmbeds(embed);
	}
	
	public RestAction<Message> replyEmbeds(CustomMessageEmbed embed) {
		return this.message.editMessageEmbeds(embed.convert());
	}
	
	public RestAction<Message> replyEmbeds(Collection<CustomMessageEmbed> embeds) {
		List<MessageEmbed> embedsConverted = new ArrayList<>();
		embeds.forEach(e -> embedsConverted.add(e.convert()));
		return this.message.editMessageEmbeds(embedsConverted);
	}
}