package components.moderation;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import commands.moderation.Tempban;
import commands.moderation.Tempmute;
import components.base.Configloader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class AutoPunishEngine {
	
	private static AutoPunishEngine INSTANCE;
	
	public static AutoPunishEngine getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new AutoPunishEngine();
		}
		return INSTANCE;
	}
	
	public void processWarnings(Guild guild) {
		ConcurrentHashMap<Integer, String> punishements = new ConcurrentHashMap<>();
		String pms = Configloader.INSTANCE.getGuildConfig(guild, "autopunish");
		if (pms.equals("")) {
			return;
		}
		String[] pm = pms.split(";");
		for (int a = 0; a < pm.length; a++) {
			String[] temp1 = pm[a].split("_", 2);
			punishements.put(Integer.valueOf(temp1[0]), temp1[1]);
		}
		List<Member> members = guild.getMembers();
		//go through members
		for (int e = 0; e < members.size(); e++) {
			Member member = members.get(e);
			User user = members.get(e).getUser();
			int warningcount = Configloader.INSTANCE.getUserConfig(guild, user, "warnings").split(";").length;
			if (punishements.get(warningcount) != null) {
				String punishement = punishements.get(warningcount);
				//go through punishements
				switch(punishement) {
					case ("kick"):
						member.kick().queue();
						break;
					case ("ban"):
						member.ban(0, "Too many warnings!").queue();
						break;
					default:
						if (punishement.contains("removerole")) {
							String[] temp1 = punishement.split("_");
							guild.removeRoleFromMember(member, guild.getRoleById(temp1[1]));
							Configloader.INSTANCE.setUserConfig(guild, user, "expe", "0");
							Configloader.INSTANCE.setUserConfig(guild, user, "level", "0");
							return;
						}
						if (punishement.contains("tempmute")) {
							String[] temp1 = punishement.split("_");
							Tempmute tm = new Tempmute();
							tm.tempmute(Integer.valueOf(temp1[1]), guild, user);
							return;
						}
						if (punishement.contains("tempban")) {
							String[] temp1 = punishement.split("_");
							Tempban tb = new Tempban();
							tb.tempban(Integer.valueOf(temp1[1]), guild, user);
							return;
						}
				}
			}
		}
	}
}