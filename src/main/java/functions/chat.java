package functions;

import java.util.List;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class chat {
	
	String message;
	TextChannel channel;
	
	public chat(GuildMessageReceivedEvent event) {
		message = event.getMessage().getContentRaw();
		channel = event.getChannel();
		this.autocheck(event);
		respond(event);
	}
	
	private void autocheck(GuildMessageReceivedEvent event) {
		Role gr1 = event.getGuild().getRoleById("837742608604332052");
		int gr1p = gr1.getPosition();
		Role gr2 = event.getGuild().getRoleById("837744376712265728");
		int gr2p = gr2.getPosition();
		Role gr3 = event.getGuild().getRoleById("837743983291400232");
		int gr3p = gr3.getPosition();
		Role gr4 = event.getGuild().getRoleById("870231144300441671");
		int gr4p = gr4.getPosition();
		Role gr5 = event.getGuild().getRoleById("863731004836806666");
		int gr5p = gr5.getPosition();
		Member member = event.getMember();
		List<Role> sr1 = event.getGuild().getRoles().stream().filter(e -> e.getPosition() < gr1p).toList().stream().filter(e -> e.getPosition() > gr2p).toList();
		List<Role> sr2 = event.getGuild().getRoles().stream().filter(e -> e.getPosition() < gr2p).toList().stream().filter(e -> e.getPosition() > gr3p).toList();
		List<Role> sr3 = event.getGuild().getRoles().stream().filter(e -> e.getPosition() < gr3p).toList().stream().filter(e -> e.getPosition() > gr4p).toList();
		List<Role> sr4 = event.getGuild().getRoles().stream().filter(e -> e.getPosition() < gr4p).toList().stream().filter(e -> e.getPosition() > gr5p).toList();
		List<Role> sr5 = event.getGuild().getRoles().stream().filter(e -> e.getPosition() < gr5p).toList().stream().filter(e -> e.getPosition() >= event.getGuild().getRoleById("863708141317259294").getPosition()).toList();
		new rolesorting(event, member, sr1, gr1);
		new rolesorting(event, member, sr2, gr3);
		new rolesorting(event, member, sr3, gr3);
		new rolesorting(event, member, sr4, gr4);
		new rolesorting(event, member, sr5, gr5);
	}

	public void respond(GuildMessageReceivedEvent event) {
		if (message.toLowerCase().contains("minecraft")) {
			channel.sendMessage("Warum schreibst du \"Minecraft\" aus? :exploding_head:").queue();
		}
	}
	
}
