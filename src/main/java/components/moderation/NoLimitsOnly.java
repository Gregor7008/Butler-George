package components.moderation;

import java.util.List;

import base.Bot;
import commands.moderation.Rolesorting;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class NoLimitsOnly {
	
	private final Guild guild;
	
	public NoLimitsOnly() {
		guild = Bot.INSTANCE.jda.getGuildById("708381749826289666");
		if (guild != null) {
			while (Bot.INSTANCE.jda.getPresence().getStatus().equals(OnlineStatus.ONLINE)) {
				this.rolecheck();
				try {wait(1000);} catch (InterruptedException e) {}
			}
		}
	}
	
	private void rolecheck() {
		Role gr1 = guild.getRoleById("837742608604332052");
		int gr1p = gr1.getPosition();
		Role gr2 = guild.getRoleById("837744376712265728");
		int gr2p = gr2.getPosition();
		Role gr3 = guild.getRoleById("837743983291400232");
		int gr3p = gr3.getPosition();
		Role gr4 = guild.getRoleById("870231144300441671");
		int gr4p = gr4.getPosition();
		Role gr5 = guild.getRoleById("863731004836806666");
		int gr5p = gr5.getPosition();
		List<Role> sr1 = guild.getRoles().stream().filter(e -> e.getPosition() < gr1p).toList().stream().filter(e -> e.getPosition() > gr2p).toList();
		List<Role> sr2 = guild.getRoles().stream().filter(e -> e.getPosition() < gr2p).toList().stream().filter(e -> e.getPosition() > gr3p).toList();
		List<Role> sr3 = guild.getRoles().stream().filter(e -> e.getPosition() < gr3p).toList().stream().filter(e -> e.getPosition() > gr4p).toList();
		List<Role> sr4 = guild.getRoles().stream().filter(e -> e.getPosition() < gr4p).toList().stream().filter(e -> e.getPosition() > gr5p).toList();
		List<Role> sr5 = guild.getRoles().stream().filter(e -> e.getPosition() < gr5p).toList().stream().filter(e -> e.getPosition() >= guild.getRoleById("863708141317259294").getPosition()).toList();
		Rolesorting rs = new Rolesorting();
		List<Member> members = guild.loadMembers().get();
		for (int i = 0; i < members.size(); i++) {
			Member member = members.get(i);
			rs.sorter(guild, member, sr1, gr1);
			rs.sorter(guild, member, sr2, gr2);
			rs.sorter(guild, member, sr3, gr3);
			rs.sorter(guild, member, sr4, gr4);
			rs.sorter(guild, member, sr5, gr5);
		}
	}
}
