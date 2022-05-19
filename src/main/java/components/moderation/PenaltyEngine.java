package components.moderation;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import base.Bot;
import components.base.ConfigLoader;
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
		String pis = ConfigLoader.run.getGuildConfig(guild, "penalties");
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
			int warningcount = ConfigLoader.run.getUserConfig(guild, user, "warnings").split(";").length;
			for (int a = warningcount; a > 0; a--) {
				if (penalties.get(a) != null) {
					warningcount = a;
					a = 0;
				}
			}
			if (penalties.get(warningcount) != null && !ConfigLoader.run.getUserConfig(guild, user, "penaltycount").equals(String.valueOf(warningcount))) {
				String penalty = penalties.get(warningcount);
				ConfigLoader.run.setUserConfig(guild, user, "penaltycount", String.valueOf(warningcount));
				//go through penaltys
				switch(penalty) {
					case ("kick"):
						member.kick().queue();
						break;
					case ("ban"):
						member.ban(0, "Too many warnings!").queue();
						break;
					default:
						if (penalty.contains("removerole")) {
							String[] temp1 = penalty.split("_");
							guild.removeRoleFromMember(member, guild.getRoleById(temp1[1]));
							ConfigLoader.run.setUserConfig(guild, user, "expe", "0");
							ConfigLoader.run.setUserConfig(guild, user, "level", "0");
							return;
						}
						if (penalty.contains("tempmute")) {
							String[] temp1 = penalty.split("_");
							ConfigLoader.run.setUserConfig(guild, user, "tempmuted", "true");
							guild.getMember(user).timeoutFor(Integer.valueOf(temp1[1]), TimeUnit.DAYS).queue();
							return;
						}
						if (penalty.contains("tempban")) {
							String[] temp1 = penalty.split("_");
							this.tempban(Integer.valueOf(temp1[1]), guild, user);
							return;
						}
				}
			}
		}
	}
	
	private void tempban(int days, Guild guild, User user) {
		OffsetDateTime until = OffsetDateTime.now().plusDays(Long.parseLong(String.valueOf(days)));
		ConfigLoader.run.setUserConfig(guild, user, "tbuntil", until.toString());
		ConfigLoader.run.setUserConfig(guild, user, "tempbanned", "true");
		guild.getMember(user).ban(0).queue();
		Bot.INSTANCE.modCheck(guild);
	}	
}