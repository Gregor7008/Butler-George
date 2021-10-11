package components.moderation;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

import base.Bot;
import components.base.Configloader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public class ModMail {

	public ModMail(PrivateMessageReceivedEvent event) {
		if (event.getAuthor().isBot()) {
			return;
		}
		if (this.checkPresence(event.getAuthor())) {
			event.getChannel().sendMessage("ModMail currently only works for members of the NoLimits server.\nJoin here: https://discord.gg/qHA2vUs").queue();
			return;
		}
		if (!event.getMessage().getContentRaw().contains("anonym")) {
			OffsetDateTime lastmail = OffsetDateTime.parse(Configloader.INSTANCE.getUserConfig(Bot.INSTANCE.jda.getGuildById(Bot.INSTANCE.getBotConfig("NoLiID")), event.getAuthor(), "lastmail"));
			if (Duration.between(lastmail, OffsetDateTime.now()).toSeconds() > 300) {
				event.getChannel().sendMessage("Your message was directly sent to one of the moderators!").queue();
				this.processMessage(event);
			} else {
				int timeleft = (int) (300 - Duration.between(lastmail, OffsetDateTime.now()).toSeconds());
				event.getChannel().sendMessage("You need to wait another " + timeleft + " seconds, to send another message to the moderators!\nIf you start to spam, we will automatically warn you for your behavior!").queue();
			}
		} else {
			OffsetDateTime lastmail = OffsetDateTime.parse(Configloader.INSTANCE.getUserConfig(Bot.INSTANCE.jda.getGuildById(Bot.INSTANCE.getBotConfig("NoLiID")), event.getAuthor(), "lastmail"));
			if (Duration.between(lastmail, OffsetDateTime.now()).toSeconds() > 1) {
				event.getChannel().sendMessage("Your message was processed and my team will reply soon. Thanks for trusting us!").queue();
				this.processAnonymousMessage(event);
			} else {
				int timeleft = (int) (300 - Duration.between(lastmail, OffsetDateTime.now()).toSeconds());
				event.getChannel().sendMessage("You need to wait another " + timeleft + " seconds, to send another message to the moderators!\nIf you start to spam, we will automatically warn you for your behavior!").queue();
			}
		}
	}

	private boolean checkPresence(User user) {
		Guild guild = Bot.INSTANCE.jda.getGuildById("708381749826289666");
		List<Guild> guilds = user.getMutualGuilds();
		if (guilds.contains(guild)) {
			return true;
		} else {
			return false;
		}
	}
	
	private void processMessage(PrivateMessageReceivedEvent event) {
		Guild guild = Bot.INSTANCE.jda.getGuildById(Bot.INSTANCE.getBotConfig("NoLiID"));
		TextChannel channel = guild.getTextChannelById("895947575050530836");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm | dd.MM.yyy");
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(guild.getRoleById("709477954824044555").getColorRaw());
		eb.setFooter(event.getMessage().getTimeCreated().format(formatter) + "\s-\s" + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator());
		eb.setDescription(event.getMessage().getContentDisplay());
		eb.setTitle(":warning: | New message by " + event.getAuthor().getName() + "!");
		channel.sendMessageEmbeds(eb.build()).queue();
	}
	
	private void processAnonymousMessage(PrivateMessageReceivedEvent event) {
		Random rng = new Random();
		Guild guild = Bot.INSTANCE.jda.getGuildById(Bot.INSTANCE.getBotConfig("NoLiID"));
		int rn = rng.nextInt(100);
		while (Configloader.INSTANCE.getMailConfig1(String.valueOf(rn)) != null) {
			rn = rng.nextInt(100);
		}
		TextChannel nc = guild.createTextChannel("#" + String.valueOf(rn), guild.getCategoryById("896011407303270402")).complete();
		nc.sendMessage(event.getMessage().getContentDisplay()).queue();
		Configloader.INSTANCE.setMailConfig(String.valueOf(rn), event.getAuthor().getId());
	}
}
