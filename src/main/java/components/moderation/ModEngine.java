package components.moderation;

import java.io.File;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import base.Bot;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
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
			User user = members.get(i).getUser();
			Member member = members.get(i);
			//Create "Muted" role
			if (Configloader.INSTANCE.getGuildConfig(guild, "muterole").equals("")) {
				Role newrole = guild.createRole()
									 .setName("Muted")
									 .complete();
				Configloader.INSTANCE.setGuildConfig(guild, "muterole", newrole.getId());
				List<TextChannel> channels = guild.getTextChannels();
				for (int o = 0; o < channels.size(); o++) {
					TextChannel channel = channels.get(o);
					Collection<Permission> perms = new ArrayList<Permission>();
					perms.add(Permission.CREATE_PRIVATE_THREADS);
					perms.add(Permission.CREATE_PUBLIC_THREADS);
					perms.add(Permission.USE_APPLICATION_COMMANDS);
					perms.add(Permission.MESSAGE_SEND);
					perms.add(Permission.MESSAGE_ADD_REACTION);
					channel.putPermissionOverride(newrole).setDeny(perms).queue();
				}
			}
			String muteroleID = Configloader.INSTANCE.getGuildConfig(guild, "muterole");
			Role muterole = guild.getRoleById(muteroleID);
			//Check the user
			if (guilddir.exists() && !user.isBot()){
				File pFile = new File(Bot.environment + "/configs/user/" + guild.getId() + "/" + user.getId() + ".properties");
				if (pFile.exists()) {
					//Check for tempmute properties
					if (Boolean.parseBoolean(Configloader.INSTANCE.getUserConfig(guild, user, "tempmuted"))) {
						OffsetDateTime tmuntil = OffsetDateTime.parse(Configloader.INSTANCE.getUserConfig(guild, user, "tmuntil"));
						OffsetDateTime now = OffsetDateTime.now();
						int difference = Duration.between(now, tmuntil).toSecondsPart();
						if (difference <= 0) {
							Configloader.INSTANCE.setUserConfig(guild, user, "muted", "false");
							Configloader.INSTANCE.setUserConfig(guild, user, "tempmuted", "false");
							Configloader.INSTANCE.setUserConfig(guild, user, "tmuntil", "");
						}
					}
					//Enforce mute properties
					if (Boolean.parseBoolean(Configloader.INSTANCE.getUserConfig(guild, user, "muted"))) {
						if (!member.getRoles().contains(muterole)) {
							guild.addRoleToMember(member, muterole).queue();
						}
					} else {
						if (member.getRoles().contains(muterole)) {
							guild.removeRoleFromMember(member, muterole).queue();
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