package components.moderation;

import java.util.List;

import base.Bot;
import commands.moderation.Rolesorting;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class NoLimitsOnly {
	
	private final Guild guild;
	private final List<Member> members;
	
	public NoLimitsOnly() {
		guild = Bot.INSTANCE.jda.getGuildById(Bot.INSTANCE.getBotConfig("NoLiID"));
		members = guild.loadMembers().get();
	}
	
	public void noliRolecheck() {
		Role gr1 = guild.getRoleById("837742608604332052");
		int gr1p = gr1.getPosition();
		Role gr2 = guild.getRoleById("837744376712265728");
		int gr2p = gr2.getPosition();
		Role gr3 = guild.getRoleById("837743983291400232");
		int gr3p = gr3.getPosition();
		Role gr4 = guild.getRoleById("870231144300441671");
		int gr4p = gr4.getPosition();
		List<Role> sr1 = guild.getRoles().stream().filter(e -> e.getPosition() < gr1p).toList().stream().filter(e -> e.getPosition() > gr2p).toList();
		List<Role> sr2 = guild.getRoles().stream().filter(e -> e.getPosition() < gr2p).toList().stream().filter(e -> e.getPosition() > gr3p).toList();
		List<Role> sr3 = guild.getRoles().stream().filter(e -> e.getPosition() < gr3p).toList().stream().filter(e -> e.getPosition() > gr4p).toList();
		List<Role> sr4 = guild.getRoles().stream().filter(e -> e.getPosition() < gr4p).toList().stream().filter(e -> e.getPosition() >= guild.getRoleById("864136501653798932").getPosition()).toList();
		Rolesorting rs = new Rolesorting();
		for (int i = 0; i < members.size(); i++) {
			Member member = members.get(i);
			if (member.getUser().isBot()) {
				if (member.getRoles().contains(guild.getRoleById("709478250253910103"))) {
					guild.removeRoleFromMember(member, guild.getRoleById("709478250253910103")).queue();
				} //if bot has member role, remove it
				if (!member.getRoles().contains(guild.getRoleById("709742200149377094")) && !member.getRoles().contains(guild.getRoleById("896654419284860928"))) {
					guild.addRoleToMember(member, guild.getRoleById("709742200149377094")).queue();
				} //if bot doesnt have bot role, add it
				return;
			} else {
				if (member.getRoles().contains(guild.getRoleById("807981738012835860"))) {
					guild.addRoleToMember(member, guild.getRoleById("837752568339365928"));
				} else {
					guild.removeRoleFromMember(member, guild.getRoleById("837752568339365928"));
				}
				rs.sorter(guild, member, sr1, gr1);
				rs.sorter(guild, member, sr2, gr2);
				rs.sorter(guild, member, sr3, gr3);
				rs.sorter(guild, member, sr4, gr4);
			}
		}
	}
}
