package components.moderation;

import java.util.List;

import base.Bot;
import commands.moderation.Rolesorting;
import components.base.Configloader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class ServerUtilities {
	
	public void rolecheck() {
		final Guild guild = Bot.INSTANCE.jda.getGuildById(Bot.noliID);
		final List<Member> members = guild.getMembers();
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
			if (!member.getUser().isBot()) {
				rs.sorter(guild, member, sr1, gr1);
				rs.sorter(guild, member, sr2, gr2);
				rs.sorter(guild, member, sr3, gr3);
				rs.sorter(guild, member, sr4, gr4);
				if (member.getRoles().contains(guild.getRoleById(Configloader.INSTANCE.getGuildConfig(guild, "muterole")))) {
					guild.removeRoleFromMember(member, guild.getRoleById("709478250253910103")).queue();
				} else {
					if (!member.getRoles().contains(guild.getRoleById("709478250253910103"))) {
						guild.addRoleToMember(member, guild.getRoleById("709478250253910103")).queue();
					}
				}
			}
		}
	}
}