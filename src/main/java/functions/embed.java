package functions;

import java.util.List;
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

	GuildMessageReceivedEvent oevent;
	EmbedBuilder eb;
	int messagecount;
	
	public embed(GuildMessageReceivedEvent event) {
		oevent = event;
		Member member;
		try {member = event.getMessage().getMentionedMembers().get(0);
		} catch (Exception e) {member = event.getMember();}
		eb = new EmbedBuilder();
		eb.setAuthor(member.getEffectiveName(), null, member.getUser().getAvatarUrl());	
		eb.setColor(56575);
		messagecount=1;
		this.definetitle(event.getAuthor(), event.getChannel());
	}
	
	private void definetitle(User user, TextChannel channel) {
		EventWaiter waiter = Bot.getWaiter();
		new answer("/commands/embed:definetitle", oevent);
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {eb.setTitle(e.getMessage().getContentRaw());
								  messagecount++;
								  this.definedescr(user, channel);},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   new answer("/commands/embed:timeout", oevent);});
	}

	private void definedescr(User user, TextChannel channel) {
		EventWaiter waiter = Bot.getWaiter();
		new answer("/commands/embed:definedescr", oevent);
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {eb.setDescription(e.getMessage().getContentRaw());
								  messagecount++;
								  this.definefooter(user, channel);},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   new answer("/commands/embed:timeout", oevent);});
	}
	
	private void definefooter(User user, TextChannel channel) {
		EventWaiter waiter = Bot.getWaiter();
		new answer("/commands/embed:definefooter", oevent);
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {if (e.getMessage().getContentRaw()=="none"){} else {eb.setFooter(e.getMessage().getContentRaw());}
								  messagecount++;
								  this.defineTNail(user, channel);},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   new answer("/commands/embed:timeout", oevent);});
	}

	private void defineTNail(User user, TextChannel channel) {
		EventWaiter waiter = Bot.getWaiter();
		new answer("/commands/embed:defineTNail", oevent);
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {if (e.getMessage().getContentRaw().contains("none")){} else {eb.setThumbnail(e.getMessage().getContentRaw());}
								  messagecount++;
								  this.defineImag(user, channel);},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   new answer("/commands/embed:timeout", oevent);});
	}
	
	private void defineImag(User user, TextChannel channel) {
		EventWaiter waiter = Bot.getWaiter();
		new answer("/commands/embed:defineImag", oevent);
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {if (e.getMessage().getContentRaw().contains("none")){} else {eb.setImage(e.getMessage().getContentRaw());}
								  messagecount++;
								  this.wantnewfield(user, channel);},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   new answer("/commands/embed:timeout", oevent);});
	}
	
	private void wantnewfield(User user, TextChannel channel) {
		EventWaiter waiter = Bot.getWaiter();
		new answer("/commands/embed:wantnewfield", oevent);
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {messagecount++;
								  if(e.getMessage().getContentRaw().contains("no")) {this.sendMessage(eb.build(), channel);} else {this.addnewfield(user, channel);}},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   new answer("/commands/embed:timeout", oevent);});
	}
	
	private void addnewfield(User user, TextChannel channel) {
		EventWaiter waiter = Bot.getWaiter();
		new answer("/commands/embed:addnewfield", oevent);
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {messagecount++;
								  String[] temp1 = e.getMessage().getContentRaw().split("\\+");
								  eb.addField(temp1[0], temp1[1], true);
								  this.wantnewfield(user, channel);},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   new answer("/commands/embed:timeout", oevent);});
	}
	
	private void sendMessage(MessageEmbed embed, TextChannel channel) {
		this.cleanup();
		channel.sendMessageEmbeds(embed).queue();
	}
	
	private void cleanup() {
		List<Message> messages = oevent.getChannel().getHistory().retrievePast(messagecount).complete();
		oevent.getChannel().deleteMessages(messages).queue();
	}
}
