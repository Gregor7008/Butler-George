package components.moderation;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import components.base.Configloader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class ModEngine {
	
	private static ModEngine INSTANCE;
	
	public static ModEngine getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ModEngine();
		}
		return INSTANCE;
	}
	
	public void processWarnings(Guild guild) {
		ConcurrentHashMap<Integer, String> punishements = new ConcurrentHashMap<>();
		String pms = Configloader.INSTANCE.getGuildConfig(guild, "autopunish");
		String[] pm = pms.split(";");
		for (int a = 0; a < pm.length; a++) {
			String[] temp1 = pm[a].split("_");
			punishements.put(Integer.valueOf(temp1[0]), temp1[1]);
		}
		List<Member> members = guild.loadMembers().get();
		//go through members
		for (int e = 0; e < members.size(); e++) {
			Member member = members.get(e);
			int warningcount = Configloader.INSTANCE.getUserConfig(member, "warnings").split(";").length;
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
							String[] temp1 = punishement.split(":");
							guild.removeRoleFromMember(member, guild.getRoleById(temp1[1]));
							Configloader.INSTANCE.setUserConfig(member, "expe", "0");
							Configloader.INSTANCE.setUserConfig(member, "level", "0");
							return;
						}
						if (punishement.contains("tempmute")) {
							String[] temp1 = punishement.split(":");
							//tempmute with duration of days:
							Integer.valueOf(temp1[1]);
							return;
						}
						if (punishement.contains("tempban")) {
							String[] temp1 = punishement.split(":");
							//tempban with duration of days:
							Integer.valueOf(temp1[1]);
							return;
						}
				}
			}
		}
	}
}
