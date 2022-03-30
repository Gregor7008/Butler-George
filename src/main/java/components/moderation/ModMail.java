package components.moderation;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Random;

import base.Bot;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ModMail {

	public ModMail(MessageReceivedEvent event) {
		Guild guild = Bot.INSTANCE.jda.getGuildById(Bot.homeID);
		User user = event.getAuthor();
		if (user.isBot()) {
			return;
		}
		if (guild.retrieveMember(user).complete() == null) {
			event.getChannel().sendMessageEmbeds(AnswerEngine.ae.fetchMessage(null, null, "/components/moderation/modmail:nosupport").convert()).queue();
			return;
		}
		if (Configloader.INSTANCE.getMailConfig2(user.getId()) != null) {
			TextChannel channel = guild.getTextChannelById(Configloader.INSTANCE.getMailConfig2(user.getId()));
			channel.sendMessage(event.getMessage().getContentRaw()).queue();
			return;
		}
		if (!event.getMessage().getContentRaw().contains("anonym")) {
			OffsetDateTime lastmail = OffsetDateTime.parse(Configloader.INSTANCE.getUserConfig(guild, user, "lastmail"));
			if (Duration.between(lastmail, OffsetDateTime.now()).toSeconds() > 300) {
				event.getChannel().sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/components/moderation/modmail:success").convert()).queue();
				Configloader.INSTANCE.setUserConfig(guild, user, "lastmail", OffsetDateTime.now().toString());
				this.processMessage(event);
			} else {
				int timeleft = (int) (300 - Duration.between(lastmail, OffsetDateTime.now()).toSeconds());
				event.getChannel().sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/components/moderation/modmail:timelimit")
						.replaceDescription("{timeleft}", String.valueOf(timeleft)).convert()).queue();
			}
		} else {
			OffsetDateTime lastmail = OffsetDateTime.parse(Configloader.INSTANCE.getUserConfig(guild, user, "lastmail"));
			if (Duration.between(lastmail, OffsetDateTime.now()).toSeconds() > 1) {
				event.getChannel().sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/components/moderation/modmail:successanonym").convert()).queue();
				Configloader.INSTANCE.setUserConfig(guild, user, "lastmail", OffsetDateTime.now().toString());
				this.processAnonymousMessage(event);
			} else {
				int timeleft = (int) (300 - Duration.between(lastmail, OffsetDateTime.now()).toSeconds());
				event.getChannel().sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/components/moderation/modmail:timelimit")
						.replaceDescription("{timeleft}", String.valueOf(timeleft)).convert()).queue();
			}
		}
	}
	
	private void processMessage(MessageReceivedEvent event) {
		Guild guild = Bot.INSTANCE.jda.getGuildById(Bot.homeID);
		Role support = guild.getRoleById(Configloader.INSTANCE.getGuildConfig(guild, "supportrole"));
		if (Configloader.INSTANCE.getGuildConfig(guild, "supportcategory").equals("")) {
			Category ctg = guild.createCategory("----------📝 Tickets ------------").complete();
			Configloader.INSTANCE.setGuildConfig(guild, "supportcategory", ctg.getId());
		}
		TextChannel nc = guild.createTextChannel(event.getAuthor().getName(), guild.getCategoryById(Configloader.INSTANCE.getGuildConfig(guild, "supportcategory"))).complete();
		nc.sendMessage(event.getMessage().getContentRaw() + "\n" + support.getAsMention()).queue();
		nc.putPermissionOverride(guild.getPublicRole()).setDeny(Permission.VIEW_CHANNEL).queue();
		nc.putPermissionOverride(support).setAllow(Permission.VIEW_CHANNEL).queue();
		Configloader.INSTANCE.setMailConfig(nc.getId(), event.getAuthor().getId());
	}
	
	private void processAnonymousMessage(MessageReceivedEvent event) {
		Guild guild = Bot.INSTANCE.jda.getGuildById(Bot.homeID);
		Role support = guild.getRoleById(Configloader.INSTANCE.getGuildConfig(guild, "supportrole"));
		int rn = new Random().nextInt(100);
		if (Configloader.INSTANCE.getGuildConfig(guild, "supportcategory").equals("")) {
			Category ctg = guild.createCategory("----------📝 Tickets ------------").complete();
			Configloader.INSTANCE.setGuildConfig(guild, "supportcategory", ctg.getId());
		}
		TextChannel nc = guild.createTextChannel(String.valueOf(rn), guild.getCategoryById(Configloader.INSTANCE.getGuildConfig(guild, "supportcategory"))).complete();
		String message = event.getMessage().getContentDisplay().replaceAll("#anonymous", "");
		nc.sendMessage(message + "\n" + support.getAsMention()).queue();
		nc.putPermissionOverride(guild.getPublicRole()).setDeny(Permission.VIEW_CHANNEL).queue();
		nc.putPermissionOverride(support).setAllow(Permission.VIEW_CHANNEL).queue();
		Configloader.INSTANCE.setMailConfig(nc.getId(), event.getAuthor().getId());
	}
}