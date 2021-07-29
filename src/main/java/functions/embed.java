package functions;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import tools.answer;

public class embed {
	
	String Title, Description, Author, AvatarURL, TNailURL, ImageURL, FooterText;
	CountDownLatch sync = new CountDownLatch(15);
	GuildMessageReceivedEvent oevent;
	
	public embed(GuildMessageReceivedEvent event, String object, Member member) {
		oevent = event;
		Author = member.getEffectiveName();
		AvatarURL = member.getUser().getAvatarUrl();
		TextChannel channel = event.getChannel();
		User user = event.getAuthor();
		this.definetitle(user, channel, true);
	}
	
	private void definetitle(User user, TextChannel channel, boolean handoff) {
		EventWaiter waiter = Bot.getWaiter();
		new answer("/commands/embed:definetitle", oevent);
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {Title = e.getMessage().getContentRaw();
								  if(handoff) {this.definedescr(user, channel, true);}},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup(2, oevent);
								   new answer("/commands/embed:timeout", oevent);});
	}

	private void definedescr(User user, TextChannel channel, boolean handoff) {
		EventWaiter waiter = Bot.getWaiter();
		new answer("/commands/embed:definedescr", oevent);
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {Description = e.getMessage().getContentRaw();
								  if(handoff) {this.definefooter(user, channel, true);}},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup(4, oevent);
								   new answer("/commands/embed:timeout", oevent);});
	}
	
	private void definefooter(User user, TextChannel channel, boolean handoff) {
		EventWaiter waiter = Bot.getWaiter();
		new answer("/commands/embed:definefooter", oevent);
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {if (e.getMessage().getContentRaw()=="none"){} else {FooterText = e.getMessage().getContentRaw();}
								  if(handoff) {this.defineTNail(user, channel, true);}},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup(6, oevent);
								   new answer("/commands/embed:timeout", oevent);});
	}

	private void defineTNail(User user, TextChannel channel, boolean handoff) {
		EventWaiter waiter = Bot.getWaiter();
		new answer("/commands/embed:defineTNail", oevent);
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {if (e.getMessage().getContentRaw()=="none"){} else {TNailURL = e.getMessage().getContentRaw();}
								  if(handoff) {this.defineImag(user, channel, true);}},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup(8, oevent);
								   new answer("/commands/embed:timeout", oevent);});
	}
	
	private void defineImag(User user, TextChannel channel, boolean handoff) {
		EventWaiter waiter = Bot.getWaiter();
		new answer("/commands/embed:defineImag", oevent);
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {if (e.getMessage().getContentRaw()=="none"){} else {ImageURL = e.getMessage().getContentRaw();}
								  if(handoff) {this.buildMessage(channel);}},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup(10, oevent);
								   new answer("/commands/embed:timeout", oevent);});
	}

	private void buildMessage(TextChannel channel) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(Title);
		eb.setColor(56575);
		eb.setDescription(Description);
		eb.setAuthor(Author, null, AvatarURL);
		if (FooterText == null){} else {eb.setFooter(FooterText);}
		try {eb.setImage(ImageURL);} catch (Exception e) {};
		try {eb.setThumbnail(TNailURL);} catch (Exception e) {};
		eb.addField(null);
		MessageEmbed embed = eb.build();
		this.sendMessage(embed, channel);
	}
	
	private void sendMessage(MessageEmbed embed, TextChannel channel) {
		List<Message> messages = channel.getHistory().retrievePast(11).complete();
		channel.deleteMessages(messages).queue();
		channel.sendMessageEmbeds(embed).queue();
	}
	
	private void cleanup(int i, GuildMessageReceivedEvent event) {
		List<Message> messages = event.getChannel().getHistory().retrievePast(i).complete();
		event.getChannel().deleteMessages(messages).queue();
	}
}
