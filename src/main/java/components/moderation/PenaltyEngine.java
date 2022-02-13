package components.moderation;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import base.Bot;
import commands.moderation.TempBan;
import commands.moderation.TempMute;
import components.base.Configloader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class PenaltyEngine {
	
	public PenaltyEngine() {
		new Thread(() -> {
			List<Guild> guilds = Bot.INSTANCE.jda.getGuilds();
			for (int i = 0; i < guilds.size(); i++) {
				this.run(guilds.get(i));
			}
		}).start();
	}
	
	public void run(Guild guild) {
		ConcurrentHashMap<Integer, String> penalties = new ConcurrentHashMap<>();
		String pis = Configloader.INSTANCE.getGuildConfig(guild, "penalties");
		if (pis.equals("")) {
			return;
		}
		String[] pn = pis.split(";");
		for (int a = 0; a < pn.length; a++) {
			String[] temp1 = pn[a].split("_", 2);
			penalties.put(Integer.valueOf(temp1[0]), temp1[1]);
		}
		List<Member> members = guild.loadMembers().get();
		//go through members
		for (int e = 0; e < members.size(); e++) {
			Member member = members.get(e);
			User user = members.get(e).getUser();
			int warningcount = Configloader.INSTANCE.getUserConfig(guild, user, "warnings").split(";").length;
			for (int a = warningcount; a > 0; a--) {
				if (penalties.get(a) != null) {
					warningcount = a;
					a = 0;
				}
			}
			if (penalties.get(warningcount) != null && !Configloader.INSTANCE.getUserConfig(guild, user, "penaltycount").equals(String.valueOf(warningcount))) {
				String penalty = penalties.get(warningcount);
				Configloader.INSTANCE.setUserConfig(guild, user, "penaltycount", String.valueOf(warningcount));
				//go through penaltys
				switch(penalty) {
					case ("kick"):
						member.kick().queue();
						break;
					case ("ban"):
						member.ban(0, "Too many warnings!").queue();
						Configloader.INSTANCE.findorCreateUserConfig(guild, user).delete();
						break;
					default:
						if (penalty.contains("removerole")) {
							String[] temp1 = penalty.split("_");
							guild.removeRoleFromMember(member, guild.getRoleById(temp1[1]));
							Configloader.INSTANCE.setUserConfig(guild, user, "expe", "0");
							Configloader.INSTANCE.setUserConfig(guild, user, "level", "0");
							return;
						}
						if (penalty.contains("tempmute")) {
							String[] temp1 = penalty.split("_");
							TempMute tm = new TempMute();
							tm.tempmute(Integer.valueOf(temp1[1]), guild, user);
							return;
						}
						if (penalty.contains("tempban")) {
							String[] temp1 = penalty.split("_");
							TempBan tb = new TempBan();
							tb.tempban(Integer.valueOf(temp1[1]), guild, user);
							return;
						}
				}
			}
		}
	}
}