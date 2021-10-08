package components.moderation;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import base.Bot;
import components.base.Configloader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public class ModMail {

	public ModMail(PrivateMessageReceivedEvent event) {
		if (this.checkPresence(event.getAuthor())) {
			OffsetDateTime lastmail = OffsetDateTime.parse(Configloader.INSTANCE.getUserConfig(Bot.INSTANCE.jda.getGuildById(Bot.INSTANCE.getBotConfig("NoLiID")), event.getAuthor(), "lastmail"));
			if (Duration.between(lastmail, OffsetDateTime.now()).toSeconds() > 300) {
				event.getChannel().sendMessage("Your message was directly sent to one of the moderators!").queue();
				this.sendMessage(event);
			} else {
				int timeleft = (int) (300 - Duration.between(lastmail, OffsetDateTime.now()).toSeconds());
				event.getChannel().sendMessage("You need to wait another " + timeleft + " seconds, to send another message to the moderators!\nIf you start to spam, we will automatically warn you for your behavior!").queue();
			}
		} else {
			event.getChannel().sendMessage("ModMail currently only works for members of the NoLimits server, I'm sorry!").queue();
		}
	}
	
	private boolean checkPresence(User user) {
		List<Member> members = Bot.INSTANCE.jda.getGuildById("708381749826289666").loadMembers().get();
		List<User> users = new ArrayList<User>();
		for (int i = 0; i < members.size(); i++) {
			users.add(members.get(i).getUser());
		}
		if (users.contains(user)) {
			return true;
		} else {
			return false;
		}
	}
	
	private void sendMessage(PrivateMessageReceivedEvent event) {
		Guild guild = Bot.INSTANCE.jda.getGuildById(Bot.INSTANCE.getBotConfig("NoLiID"));
		TextChannel channel = guild.getTextChannelById("895947575050530836");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd.MM.yyy");
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(guild.getRoleById("709477954824044555").getColorRaw());
		eb.setAuthor(event.getAuthor().getName(), null, event.getAuthor().getAvatarUrl());
		eb.setFooter(event.getMessage().getTimeCreated().format(formatter));
		eb.addField(new Field(null, event.getMessage().getContentDisplay(), false));
		channel.sendMessageEmbeds(eb.build()).queue();
	}
}
