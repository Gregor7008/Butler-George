package components.moderation;

import java.io.File;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

import base.Bot;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class ModController {

	public void modcheck() {	
		List<Guild> guilds = Bot.INSTANCE.jda.getGuilds();
		for (int e = 0; e < guilds.size(); e++) {
			Guild guild = guilds.get(e);
			File guilddir = new File(Bot.INSTANCE.getBotConfig("resourcepath") + "/configs/user/" + guild.getId());
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
						channel.createPermissionOverride(newrole)
							   .setDeny(Permission.MESSAGE_WRITE)
							   .setDeny(Permission.USE_SLASH_COMMANDS)
							   .queue();
					}
				}
				String muteroleID = Configloader.INSTANCE.getGuildConfig(guild, "muterole");
				Role muterole = guild.getRoleByBot(muteroleID);
				//Check the user
				if (guilddir.exists()) {
					File pFile = new File(Bot.INSTANCE.getBotConfig("resourcepath") + "/configs/user/" + guild.getId() + "/" + user.getId() + ".properties");
					if (pFile.exists() && !user.isBot()) {
						if (Boolean.parseBoolean(Configloader.INSTANCE.getUserConfig(guild, user, "tempmuted"))) {
							OffsetDateTime tmuntil = OffsetDateTime.parse(Configloader.INSTANCE.getUserConfig(guild, user, "tmuntil"));
							OffsetDateTime now = OffsetDateTime.now();
							int difference = Duration.between(now, tmuntil).toSecondsPart();
							if (difference <= 0) {
								guild.removeRoleFromMember(member, muterole).queue();
								Configloader.INSTANCE.setUserConfig(guild.retrieveMember(user).complete(), "muted", "false");
								Configloader.INSTANCE.setUserConfig(guild.getMember(user), "tempmuted", "false");
								Configloader.INSTANCE.setUserConfig(guild.getMember(user), "tmuntil", "");
							}
						}
						if (Boolean.parseBoolean(Configloader.INSTANCE.getUserConfig(guild, user, "muted"))) {
							if (!member.getRoles().contains(muterole)) {
								guild.addRoleToMember(member, muterole).queue();
								if (guild.getId().equals(Bot.INSTANCE.getBotConfig("NoLiID"))) {
									guild.removeRoleFromMember(member, guild.getRoleById("709478250253910103")).queue();
								}
							}
						} else {
							if (member.getRoles().contains(muterole)) {
								guild.removeRoleFromMember(member, muterole).queue();
								Configloader.INSTANCE.setUserConfig(guild.getMember(user), "muted", "false");
								if (guild.getId().equals(Bot.INSTANCE.getBotConfig("NoLiID"))) {
									guild.addRoleToMember(member, guild.getRoleById("709478250253910103")).queue();
								}
							}
						}
					}
				}
			}
			File[] filelist = guilddir.listFiles();
			for (int i = 0; i < filelist.length; i++) {
				String[] temp1 = filelist[i].getName().split(".properties");
				User user = Bot.INSTANCE.jda.retrieveUserById(temp1[0]).complete();
				if (Boolean.parseBoolean(Configloader.INSTANCE.getUserConfig(guild, user, "tempbanned"))) {
					OffsetDateTime tbuntil = OffsetDateTime.parse(Configloader.INSTANCE.getUserConfig(guild, user, "tbuntil"));
					OffsetDateTime now = OffsetDateTime.now();
					long difference = Duration.between(now, tbuntil).toSeconds();
					if (difference <= 0) {
						guild.unban(user).queue();
						Configloader.INSTANCE.setUserConfig(guild.getMember(user), "tempbanned", "false");
						Configloader.INSTANCE.setUserConfig(guild.getMember(user), "tbuntil", "");
					}
				}
			}
		}
	}
}
