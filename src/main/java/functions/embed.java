package functions;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Answer;
import base.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class embed {
	
	String Title, Description, Author, AvatarURL, TNailURL, ImageURL, FooterText;
	CountDownLatch sync = new CountDownLatch(15);
	
	public embed(GuildMessageReceivedEvent event, String object, Member member) {
		Author = member.getEffectiveName();
		AvatarURL = member.getUser().getAvatarUrl();
		TextChannel channel = event.getChannel();
		User user = event.getAuthor();
		this.definetitle(user, channel, true);
	}
	
	private void definetitle(User user, TextChannel channel, boolean handoff) {
		EventWaiter waiter = Bot.getWaiter();
		new Answer("Define \"Title\"!", "Please respond with your wanted title!", channel, false);
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {Title = e.getMessage().getContentRaw();
								  if(handoff) {this.definedescr(user, channel, true);}},
							1, TimeUnit.MINUTES,
							() -> {new Answer("Timeout!", ":exclamation: | You took to long to answer! \\n ---> Error code: 005", channel, true);});
	}

	private void definedescr(User user, TextChannel channel, boolean handoff) {
		EventWaiter waiter = Bot.getWaiter();
		new Answer("Define \"Description\"!", "Please respond with your wanted description!", channel, false);
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {Description = e.getMessage().getContentRaw();
								  if(handoff) {this.definefooter(user, channel, true);}},
							1, TimeUnit.MINUTES,
							() -> {new Answer("Timeout!", ":exclamation: | You took to long to answer! \\n ---> Error code: 005", channel, true);});
	}
	
	private void definefooter(User user, TextChannel channel, boolean handoff) {
		EventWaiter waiter = Bot.getWaiter();
		new Answer("Define \"Footer-Text\"!", "Please respond with your wanted footer!", channel, false);
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {FooterText = e.getMessage().getContentRaw();
								  if(handoff) {this.defineTNail(user, channel, true);}},
							1, TimeUnit.MINUTES,
							() -> {new Answer("Timeout!", ":exclamation: | You took to long to answer! \\n ---> Error code: 005", channel, true);});
	}

	private void defineTNail(User user, TextChannel channel, boolean handoff) {
		EventWaiter waiter = Bot.getWaiter();
		new Answer("Define \"ThumbnailURL\"!", "Please respond with an URL, leading to your Thumbnail!", channel, false);
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {TNailURL = e.getMessage().getContentRaw();
								  if(handoff) {this.defineImag(user, channel, true);}},
							1, TimeUnit.MINUTES,
							() -> {new Answer("Timeout!", ":exclamation: | You took to long to answer! \\n ---> Error code: 005", channel, true);});
	}
	
	private void defineImag(User user, TextChannel channel, boolean handoff) {
		EventWaiter waiter = Bot.getWaiter();
		new Answer("Define \"ImageURL\"!", "Please respond with an URL, leading to your Image!", channel, false);
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {ImageURL = e.getMessage().getContentRaw();
								  if(handoff) {this.buildMessage(channel);}},
							1, TimeUnit.MINUTES,
							() -> {new Answer("Timeout!", ":exclamation: | You took to long to answer! \\n ---> Error code: 005", channel, true);});
	}

	private void buildMessage(TextChannel channel) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(Title);
		eb.setColor(56575);
		eb.setDescription(Description);
		eb.setAuthor(Author, null, AvatarURL);
		eb.setFooter(FooterText);
		eb.setImage(ImageURL);
		eb.setThumbnail(TNailURL);
		eb.addField(null);
		MessageEmbed embed = eb.build();
		channel.sendMessageEmbeds(embed).queue();
	}
}
