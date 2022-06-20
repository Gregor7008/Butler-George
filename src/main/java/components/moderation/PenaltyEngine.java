package components.moderation;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import base.Bot;
import components.base.ConfigLoader;
import components.base.assets.ConfigManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class PenaltyEngine {
	
	public static PenaltyEngine run;
	
	public PenaltyEngine() {
		run = this;
		List<Guild> guilds = Bot.run.jda.getGuilds();
		for (int i = 0; i < guilds.size(); i++) {
			this.guildCheck(guilds.get(i));
		}
	}
	
	public void guildCheck(Guild guild) {
		new Thread(() -> {
			List<Member> members = guild.loadMembers().get();
			for (int e = 0; e < members.size(); e++) {
				Member member = members.get(e);
				this.userCheck(guild, member);
			}
		}).start();
	}
	
	public void userCheck(Guild guild, Member member) {
		User user = member.getUser();
		JSONObject penalties = ConfigLoader.getGuildConfig(guild).getJSONObject("penalties");
		if (penalties.isEmpty()) {
			return;
		}
		int warningcount = ConfigLoader.getMemberConfig(guild, user).getJSONArray("warnings").length();
		boolean error = false;
		for (int a = warningcount; a > 0; a--) {
			try {
				penalties.getJSONArray(String.valueOf(a));
				warningcount = a;
				a = 0;
			} catch (JSONException ex) {
				error = true;
			}
		}
		if (!error && ConfigLoader.getMemberConfig(guild, user).getInt("penaltycount") < warningcount) {
			JSONArray penalty = penalties.getJSONArray(String.valueOf(warningcount));
			ConfigLoader.getMemberConfig(guild, user).put("penaltycount", warningcount);
			//go through penaltys
			switch(penalty.getString(0)) {
				case ("rr"):
					guild.removeRoleFromMember(member, guild.getRoleById(penalty.getString(1)));
					ConfigLoader.getMemberConfig(guild, user).put("experience", 0);
					ConfigLoader.getMemberConfig(guild, user).put("level", 0);
					break;
				case ("tm"):
					ConfigLoader.getMemberConfig(guild, user).put("tempmuted", true);
					guild.getMember(user).timeoutFor(Integer.valueOf(penalty.getString(1)), TimeUnit.DAYS).queue();
					break;
				case ("pm"):
					ConfigLoader.getMemberConfig(guild, user).put("muted", true);
					ModEngine.run.guildCheck(guild);
					break;
				case ("kk"):
					member.kick().queue();
					break;
				case ("tb"):
					this.tempban(Integer.valueOf(penalty.getString(1)), guild, user);
					break;
				case ("pb"):
					member.ban(0, "Too many warnings").queue();
					break;
				default:
					return;
			}
		}
	}
	
	private void tempban(int days, Guild guild, User user) {
		OffsetDateTime until = OffsetDateTime.now().plusDays(days);
		ConfigLoader.getMemberConfig(guild, user).put("tempbanneduntil", until.format(ConfigManager.dateTimeFormatter));
		ConfigLoader.getMemberConfig(guild, user).put("tempbanned", true);
		guild.getMember(user).ban(0).queue();
		ModEngine.run.guildCheck(guild);
	}	
}