package components;

import java.util.List;

import commands.moderation.Rolesorting;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Automatic {
	
	public Automatic(GuildMessageReceivedEvent event) {
		if (event.getGuild().getId() != "708381749826289666") {
			return;
		}
		this.autocheck(event);
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
		Rolesorting rs = new Rolesorting(event.getGuild(), event.getMember(), event.getChannel());
		rs.sort(event.getGuild(), member, sr1, gr1);
		rs.sort(event.getGuild(), member, sr2, gr2);
		rs.sort(event.getGuild(), member, sr3, gr3);
		rs.sort(event.getGuild(), member, sr4, gr4);
		rs.sort(event.getGuild(), member, sr5, gr5);
	}	
}
