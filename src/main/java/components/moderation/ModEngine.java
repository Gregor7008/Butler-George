package components.moderation;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import base.Bot;
import components.base.ConfigLoader;
import components.base.assets.ConfigManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class ModEngine {
	
	public static ModEngine run;

	public ModEngine() {
		run = this;
		List<Guild> guilds = Bot.run.jda.getGuilds();
		for (int i = 0; i < guilds.size(); i++) {
			this.guildCheck(guilds.get(i));
		}
	}
	
	public void guildCheck(Guild guild) {
		new Thread(() -> {
			ConcurrentHashMap<Long, JSONObject> usersCached = ConfigManager.getUserCache();
			usersCached.forEach((id, obj) -> {
				User user = Bot.run.jda.retrieveUserById(id).complete();
				this.userCheck(guild, user, obj);				
			});
		}).start();
	}
	
	public void userCheck(Guild guild, User user, JSONObject obj) {
		Member member = guild.retrieveMember(user).complete();
		try {
			JSONObject memberConfig = obj.getJSONObject(guild.getId());
			if (!user.isBot() && member != null) {
				//Check for tempmute properties
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
		} catch (JSONException e) {}
	}
}