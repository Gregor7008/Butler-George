package components.moderation;

import java.io.File;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import base.Bot;
import components.base.Configloader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class ModEngine {

	public ModEngine() {
		new Thread(() -> {
			List<Guild> guilds = Bot.INSTANCE.jda.getGuilds();
			for (int i = 0; i < guilds.size(); i++) {
				this.run(guilds.get(i));
			}
		}).start();
	}
	
	public void run(Guild guild) {
		File guilddir = new File(Bot.environment + "/configs/user/" + guild.getId());
		List<Member> members = guild.loadMembers().get();
		for (int i = 0; i < members.size(); i++) {
			Member member = members.get(i);
			User user = member.getUser();
			//Check the user
			if (guilddir.exists() && !user.isBot()){
				File pFile = new File(Bot.environment + "/configs/user/" + guild.getId() + "/" + user.getId() + ".properties");
				if (pFile.exists()) {
					//Check for tempmute properties
					if (!Boolean.parseBoolean(Configloader.INSTANCE.getUserConfig(guild, user, "tempmuted"))) {
						if (Boolean.parseBoolean(Configloader.INSTANCE.getUserConfig(guild, user, "muted"))) {
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
							Configloader.INSTANCE.setUserConfig(guild, user, "tempmuted", "false");
						}
					}
				}
			}
		}
		File[] filelist = guilddir.listFiles();
		if (filelist != null) {
			for (int i = 0; i < filelist.length; i++) {
				String[] temp1 = filelist[i].getName().split(".properties");
				User user = Bot.INSTANCE.jda.retrieveUserById(temp1[0]).complete();
				if (Boolean.parseBoolean(Configloader.INSTANCE.getUserConfig(guild, user, "tempbanned"))) {
					OffsetDateTime tbuntil = OffsetDateTime.parse(Configloader.INSTANCE.getUserConfig(guild, user, "tbuntil"));
					OffsetDateTime now = OffsetDateTime.now();
					long difference = Duration.between(now, tbuntil).toSeconds();
					if (difference <= 0) {
						guild.unban(user).queue();
						Configloader.INSTANCE.setUserConfig(guild, user, "tempbanned", "false");
						Configloader.INSTANCE.setUserConfig(guild, user, "tbuntil", "");
					}
				}
			}
		}
	}
}