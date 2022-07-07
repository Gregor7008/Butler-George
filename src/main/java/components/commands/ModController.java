package components.commands;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import base.Bot;
import components.base.ConfigLoader;
import components.base.ConfigManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class ModController {
	
	public static ModController run;

	public ModController() {
		run = this;
		List<Guild> guilds = Bot.INSTANCE.jda.getGuilds();
		for (int i = 0; i < guilds.size(); i++) {
			this.guildModCheck(guilds.get(i));
		}
	}
	
	public void guildModCheck(Guild guild) {
		new Thread(() -> {
			ConcurrentHashMap<Long, JSONObject> usersCached = ConfigManager.getUserCache();
			usersCached.forEach((id, obj) -> {
				User user = Bot.INSTANCE.jda.retrieveUserById(id).complete();
				try {
					this.userModCheck(guild, user);
				} catch (JSONException e) {}
			});
		}).start();
	}
	
	public void userModCheck(Guild guild, User user) {
		Member member = guild.retrieveMember(user).complete();
		JSONObject memberConfig = ConfigLoader.getMemberConfig(guild, user);
		if (!user.isBot() && member != null) {
			if (!memberConfig.getBoolean("tempmuted")) {
				if (memberConfig.getBoolean("muted")) {
					if (!member.isTimedOut()) {
						member.timeoutFor(27, TimeUnit.DAYS).queue();
					}
				} else {
					if (member.isTimedOut()) {
						member.removeTimeout().queue();
					}
				}
			} else {
				if (!guild.getMember(user).isTimedOut()) {
					memberConfig.put("tempmuted", false);
				}
			}
			if (ConfigLoader.getMemberConfig(guild, user).getBoolean("tempbanned")) {
				OffsetDateTime tbuntil = OffsetDateTime.parse(memberConfig.getString("tempbanneduntil"), ConfigManager.dateTimeFormatter);
				OffsetDateTime now = OffsetDateTime.now();
				long difference = Duration.between(now, tbuntil).toSeconds();
				if (difference <= 0) {
					guild.unban(user).queue();
					memberConfig.put("tempbanned", false);
					memberConfig.put("tempbanneduntil", "");
				}
			}
		}
	}
	
	public void guildPenaltyCheck(Guild guild) {
		new Thread(() -> {
			List<Member> members = guild.loadMembers().get();
			for (int e = 0; e < members.size(); e++) {
				Member member = members.get(e);
				if (!member.getUser().isBot()) {
					this.userPenaltyCheck(guild, member);
				}
			}
		}).start();
	}
	
	public void userPenaltyCheck(Guild guild, Member member) {
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
					ModController.run.guildModCheck(guild);
					break;
				case ("kk"):
					member.kick().queue();
					break;
				case ("tb"):
					OffsetDateTime until = OffsetDateTime.now().plusDays(Integer.valueOf(penalty.getString(1)));
					ConfigLoader.getMemberConfig(guild, user).put("tempbanneduntil", until.format(ConfigManager.dateTimeFormatter));
					ConfigLoader.getMemberConfig(guild, user).put("tempbanned", true);
					guild.getMember(user).ban(0).queue();
					this.userModCheck(guild, user);
					break;
				case ("pb"):
					member.ban(0, "Too many warnings").queue();
					break;
				default:
					return;
			}
		}
	}	
}