package components.moderation;

import java.io.File;
import java.io.FilenameFilter;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import base.Bot;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.RoleAction;

public class ModController {
	
	public ModController () {
		while (Bot.INSTANCE.jda.getPresence().getStatus().equals(OnlineStatus.ONLINE)) {
			 this.modcheck();
		}
	}

	private void modcheck() {		
		File file1 = new File(Bot.INSTANCE.getBotConfig("resourcepath") + "/configs/user");
		List<String> guildids = Arrays.asList(file1.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return new File(dir, name).isDirectory();
			}}));
		for (int e = 0; e < guildids.size(); e++) {
			Guild guild = Bot.INSTANCE.jda.getGuildById(guildids.get(e));
			
			File file2 = new File(Bot.INSTANCE.getBotConfig("resourcepath") + "/configs/user/" + guildids.get(e));
			List<String> users = Arrays.asList(file2.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return new File(dir, name).isFile();
				}
			}));
			for (int i = 0; i < users.size(); i++) {
				String[] temp1 = users.get(i).split(".properties");
				User user = guild.retrieveMemberById(temp1[0]).complete().getUser();
				if (Configloader.INSTANCE.getGuildConfig(guild, "muterole").equals("")) {
					RoleAction cr = guild.createRole()
										 .setName("Muted");
					Role newrole = cr.complete();
					Configloader.INSTANCE.setGuildConfig(guild, "muterole", newrole.getId());
					List<TextChannel> channels = guild.getTextChannels();
					for (int o = 0; o < channels.size(); o++) {
						TextChannel channel = channels.get(o);
						channel.createPermissionOverride(newrole)
							   .setDeny(Permission.MESSAGE_WRITE)
							   .queue();
					}
				}
				if (Boolean.parseBoolean(Configloader.INSTANCE.getUserConfig(guild, user, "tempmuted"))) {
					OffsetDateTime tmuntil = OffsetDateTime.parse(Configloader.INSTANCE.getUserConfig(guild, user, "tmuntil"));
					OffsetDateTime now = OffsetDateTime.now();
					int difference = Duration.between(tmuntil, now).toSecondsPart();
					if (difference <= 0) {
						guild.removeRoleFromMember(guild.retrieveMember(user).complete(), guild.getRoleById(Configloader.INSTANCE.getGuildConfig(guild, "muterole"))).queue();
						user.openPrivateChannel().queue(channel -> {
							try {
								channel.sendMessageEmbeds(AnswerEngine.getInstance().buildMessage("Hey\s" + user.getName() + "!", ":tada: | You were just unmuted over at the\s" + guild.getName() + "\sserver!\nYou can chat again now!")).queue();
							} catch (Exception e1) {}
						});
						Configloader.INSTANCE.setUserConfig(guild.retrieveMember(user).complete(), "muted", "false");
					}
				}
				if (Boolean.parseBoolean(Configloader.INSTANCE.getUserConfig(guild, user, "muted"))) {
					if (!guild.retrieveMember(user).complete().getRoles().contains(guild.getRoleById(Configloader.INSTANCE.getGuildConfig(guild, "muterole")))) {
						guild.addRoleToMember(guild.retrieveMember(user).complete(),guild.getRoleById(Configloader.INSTANCE.getGuildConfig(guild, "muterole"))).queue();
					}
				} else {
					if (!guild.retrieveMember(user).complete().getRoles().contains(guild.getRoleById(Configloader.INSTANCE.getGuildConfig(guild, "muterole")))) {
						guild.removeRoleFromMember(guild.retrieveMember(user).complete(),guild.getRoleById(Configloader.INSTANCE.getGuildConfig(guild, "muterole"))).queue();
					}
				}
				if (Boolean.parseBoolean(Configloader.INSTANCE.getUserConfig(guild, user, "tempbanned"))) {
					OffsetDateTime tbuntil = OffsetDateTime.parse(Configloader.INSTANCE.getUserConfig(guild, user, "tbuntil"));
					OffsetDateTime now = OffsetDateTime.now();
					int difference = Duration.between(tbuntil, now).toSecondsPart();
					if (difference <= 0) {
						guild.unban(user).queue();
						user.openPrivateChannel().queue(channel -> {
							try {
								channel.sendMessageEmbeds(AnswerEngine.getInstance().buildMessage("Hey\s" + user.getName() + "!", ":tada: | You were just unbanned over at the\s" + guild.getName() + "\sserver!\nYou can join again now!")).queue();
							} catch (Exception e1) {}
						});
					}
				}
			}
		}
	}
}
