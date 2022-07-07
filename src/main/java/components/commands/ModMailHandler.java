package components.commands;

import java.time.Duration;
import java.time.OffsetDateTime;

import base.Bot;
import components.Toolbox;
import components.base.ConfigLoader;
import components.base.ConfigManager;
import components.base.LanguageEngine;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

public class ModMailHandler {
	
	public static final Guild guild = Bot.INSTANCE.jda.getGuildById(Bot.homeID);

	public ModMailHandler(MessageReceivedEvent event, boolean direction) {
		new Thread(() -> {
			this.processEvent(event, direction);
		}).start();
	}
	
	private void processEvent(MessageReceivedEvent event, boolean direction) {
		User user = event.getAuthor();
		if (user.isBot()) {
			return;
		}
		if (guild == null) {
			return;
		}
		if (direction) {
			if (ConfigLoader.getModMailOfChannel(event.getChannel().getId()) != null) {
				PrivateChannel pc = ConfigLoader.getModMailOfChannel(event.getChannel().getId()).openPrivateChannel().complete();
				Toolbox.resendMessage(pc, event.getMessage());
			}
			return;
		}
		boolean member = false;
		try {
			if (guild.retrieveMember(user).complete() != null) {
				member = true;
			}
		} catch (ErrorResponseException e) {}
		if (guild.retrieveBan(user).complete() == null && !member) {
			event.getChannel().sendMessageEmbeds(LanguageEngine.fetchMessage(null, null, this, "nosupport").convert()).queue();
			return;
		}
		if (ConfigLoader.getModMailOfUser(user.getId()) != null) {
			TextChannel channel = ConfigLoader.getModMailOfUser(user.getId());
			Toolbox.resendMessage(channel, event.getMessage());
			return;
		}
		OffsetDateTime lastmail = OffsetDateTime.parse(ConfigLoader.getMemberConfig(guild, user).getString("lastmail"), ConfigManager.dateTimeFormatter);
		if (Duration.between(lastmail, OffsetDateTime.now()).toSeconds() > 300) {
			event.getChannel().sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "success").convert()).queue();
			ConfigLoader.getMemberConfig(guild, user).put("lastmail", OffsetDateTime.now().format(ConfigManager.dateTimeFormatter));
			this.processMessage(event);
		} else {
			int timeleft = (int) (300 - Duration.between(lastmail, OffsetDateTime.now()).toSeconds());
			event.getChannel().sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "timelimit")
					.replaceDescription("{timeleft}", String.valueOf(timeleft)).convert()).queue();
		}
	}
	
	private void processMessage(MessageReceivedEvent event) {
		if (ConfigLoader.getGuildConfig(guild).getLong("modmailcategory") == 0) {
			Category ctg = guild.createCategory("----------📝 ModMail ------------").complete();
			ConfigLoader.getGuildConfig(guild).put("modmailcategory", ctg.getIdLong());
		}
		TextChannel nc = guild.createTextChannel(event.getAuthor().getName(), guild.getCategoryById(ConfigLoader.getGuildConfig(guild).getLong("supportcategory"))).complete();
		Toolbox.resendMessage(nc, event.getMessage());
		nc.sendMessage(guild.getPublicRole().getAsMention());
		nc.upsertPermissionOverride(guild.getPublicRole()).deny(Permission.VIEW_CHANNEL).queue();
		ConfigLoader.setModMailConfig(nc.getId(), event.getAuthor().getId());
	}
}