package functions;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class chat {
	
	String message;
	TextChannel channel;
	
	public chat(GuildMessageReceivedEvent event) {
		message = event.getMessage().getContentRaw();
		channel = event.getChannel();
		respond(event);
	}
	
	public void respond(GuildMessageReceivedEvent event) {
		if (message.toLowerCase().contains("minecraft")) {
			channel.sendMessage("Warum schreibst du \"Minecraft\" aus? :exploding_head:").queue();
		}
		
		//if (event.getMessage().getMentionedMembers().get(0) == event.getGuild().getMembersByEffectiveName("Gregor7008", false).get(0)) {
		//	channel.sendMessage("Du wirst gebraucht, " + event.getGuild().getMembersByEffectiveName("Gregor7008", false).get(0).getAsMention()).queue();
		//}
	}
	
}
