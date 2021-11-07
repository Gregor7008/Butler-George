package components.moderation;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import base.Bot;
import components.base.Configloader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public class ModMail {

	public ModMail(PrivateMessageReceivedEvent event) {
		Guild guild = Bot.INSTANCE.jda.getGuildById(Bot.INSTANCE.getBotConfig("NoLiID"));
		if (event.getAuthor().isBot()) {
			return;
		}
		if (Configloader.INSTANCE.getMailConfig2(event.getAuthor().getId()) != null) {
			TextChannel channel = guild.getTextChannelsByName(Configloader.INSTANCE.getMailConfig2(event.getAuthor().getId()), true).get(0);
			channel.sendMessage(event.getMessage().getContentRaw()).queue();
			return;
		}
		if (!event.getMessage().getContentRaw().contains("anonym")) {
			OffsetDateTime lastmail = OffsetDateTime.parse(Configloader.INSTANCE.getUserConfig(guild, event.getAuthor(), "lastmail"));
			if (Duration.between(lastmail, OffsetDateTime.now()).toSeconds() > 300) {
				event.getChannel().sendMessage("Your message was directly sent to the server team.\nThey will contact you as soon as the responsible person is online!").queue();
				Configloader.INSTANCE.setUserConfig(guild.getMember(event.getAuthor()), "lastmail", OffsetDateTime.now().toString());
				this.processMessage(event);
			} else {
				int timeleft = (int) (300 - Duration.between(lastmail, OffsetDateTime.now()).toSeconds());
				event.getChannel().sendMessage("You need to wait another " + timeleft + " seconds, to send another message to the moderators!\nIf you start to spam, we will automatically warn you for your behavior!").queue();
			}
		} else {
			OffsetDateTime lastmail = OffsetDateTime.parse(Configloader.INSTANCE.getUserConfig(guild, event.getAuthor(), "lastmail"));
			if (Duration.between(lastmail, OffsetDateTime.now()).toSeconds() > 1) {
				event.getChannel().sendMessage("Your message was processed and the team will reply soon. Thanks for trusting us!").queue();
				Configloader.INSTANCE.setUserConfig(guild.getMember(event.getAuthor()), "lastmail", OffsetDateTime.now().toString());
				this.processAnonymousMessage(event);
			} else {
				int timeleft = (int) (300 - Duration.between(lastmail, OffsetDateTime.now()).toSeconds());
				event.getChannel().sendMessage("You need to wait another " + timeleft + " seconds, to send another message to the moderators!\nIf you start to spam, we will automatically warn you for your behavior!").queue();
			}
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
		String message = event.getMessage().getContentDisplay().replaceAll("#anonymous", "");
		nc.sendMessage(message).queue();
		Configloader.INSTANCE.setMailConfig(String.valueOf(rn), event.getAuthor().getId());
	}
}
