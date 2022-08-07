package slash_commands.engines;

import java.util.List;

import base.Bot;
import base.engines.configs.ConfigLoader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import slash_commands.administration.Rolesorting;

public class ServerUtilities {
	
	public static void rolecheck() {
		final Guild guild = Bot.INSTANCE.jda.getGuildById(Bot.HOME);
		if (guild == null) {
			return;
		}
		final List<Member> members = guild.getMembers();
		Role gr1 = guild.getRoleById("837742608604332052");
		int gr1p = gr1.getPosition();
		Role gr2 = guild.getRoleById("939622690577326090");
		int gr2p = gr2.getPosition();
		Role gr3 = guild.getRoleById("837744376712265728");
		int gr3p = gr3.getPosition();
		Role gr4 = guild.getRoleById("837743983291400232");
		int gr4p = gr4.getPosition();
		Role gr5 = guild.getRoleById("870231144300441671");
		int gr5p = gr5.getPosition();
		List<Role> sr1 = guild.getRoles().stream().filter(e -> e.getPosition() < gr1p).toList().stream().filter(e -> e.getPosition() > gr2p).toList();
		List<Role> sr2 = guild.getRoles().stream().filter(e -> e.getPosition() < gr2p).toList().stream().filter(e -> e.getPosition() > gr3p).toList();
		List<Role> sr3 = guild.getRoles().stream().filter(e -> e.getPosition() < gr3p).toList().stream().filter(e -> e.getPosition() > gr4p).toList();
		List<Role> sr4 = guild.getRoles().stream().filter(e -> e.getPosition() < gr4p).toList().stream().filter(e -> e.getPosition() > gr5p).toList();
		List<Role> sr5 = guild.getRoles().stream().filter(e -> e.getPosition() < gr5p).toList().stream().filter(e -> e.getPosition() >= guild.getRoleById("806462168337219585").getPosition()).toList();
		Rolesorting rs = new Rolesorting();
		for (int i = 0; i < members.size(); i++) {
			Member member = members.get(i);
			if (!member.getUser().isBot()) {
				rs.sorter(guild, member, sr1, gr1);
				rs.sorter(guild, member, sr2, gr2);
				rs.sorter(guild, member, sr3, gr3);
				rs.sorter(guild, member, sr4, gr4);
				rs.sorter(guild, member, sr5, gr5);
				if (member.getRoles().contains(guild.getRoleById(ConfigLoader.INSTANCE.getGuildConfig(guild).getLong("muterole")))) {
					guild.removeRoleFromMember(member, guild.getRoleById("709478250253910103")).queue();
				} else {
					if (!member.getRoles().contains(guild.getRoleById("709478250253910103"))) {
						guild.addRoleToMember(member, guild.getRoleById("709478250253910103")).queue();
					}
				}
			}
		}
	}
	
	public static void controlChannels(boolean action) {
		final Guild guild = Bot.INSTANCE.jda.getGuildById(Bot.HOME);
		if (guild == null) {
			return;
		}
		if (action) {
			guild.getTextChannelById("937825700243726387").upsertPermissionOverride(guild.getPublicRole()).grant(Permission.VIEW_CHANNEL).queue();
		} else {
			guild.getTextChannelById("937825700243726387").upsertPermissionOverride(guild.getPublicRole()).deny(Permission.VIEW_CHANNEL).queue();
		}
	}
}