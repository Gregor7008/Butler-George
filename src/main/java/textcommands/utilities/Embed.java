package textcommands.utilities;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import components.AnswerEngine;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Embed {

	private EmbedBuilder eb;
	private int messagecount;
	private Bot bot;
	private Guild guild;
	private Member member;
	private User user;
	private TextChannel channel;
	
	public Embed(Guild iguild, Member imember, TextChannel ichannel) {
		guild = iguild;
		member = imember;
		user = member.getUser();
		channel = ichannel;
		eb = new EmbedBuilder();
		eb.setAuthor(member.getEffectiveName(), null, member.getUser().getAvatarUrl());	
		eb.setColor(56575);
		messagecount=1;
		this.definetitle();
	}
	
	private void definetitle() {
		EventWaiter waiter = bot.getWaiter();
		AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:definetitle", guild, member, channel).queue();
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {eb.setTitle(e.getMessage().getContentRaw());
								  messagecount++;
								  this.definedescr();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:timeout", guild, member, channel).queue();});
	}

	private void definedescr() {
		EventWaiter waiter = bot.getWaiter();
		AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:definedescr", guild, member, channel).queue();
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {eb.setDescription(e.getMessage().getContentRaw());
								  messagecount++;
								  this.definefooter();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:timeout", guild, member, channel).queue();});
	}
	
	private void definefooter() {
		EventWaiter waiter = bot.getWaiter();
		AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:definefooter", guild, member, channel).queue();
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {if (e.getMessage().getContentRaw()=="none"){} else {eb.setFooter(e.getMessage().getContentRaw());}
								  messagecount++;
								  this.defineTNail();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:timeout", guild, member, channel).queue();});
	}

	private void defineTNail() {
		EventWaiter waiter = bot.getWaiter();
		AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:defineTNail", guild, member, channel).queue();
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {if (e.getMessage().getContentRaw().contains("none")){} else {eb.setThumbnail(e.getMessage().getContentRaw());}
								  messagecount++;
								  this.defineImag();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:timeout", guild, member, channel).queue();});
	}
	
	private void defineImag() {
		EventWaiter waiter = bot.getWaiter();
		AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:defineImag", guild, member, channel).queue();
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {if (e.getMessage().getContentRaw().contains("none")){} else {eb.setImage(e.getMessage().getContentRaw());}
								  messagecount++;
								  this.wantnewfield();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:timeout", guild, member, channel).queue();});
	}
	
	private void wantnewfield() {
		EventWaiter waiter = bot.getWaiter();
		AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:wantnewfield", guild, member, channel).queue();
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {messagecount++;
								  if(e.getMessage().getContentRaw().contains("no")) {this.sendMessage(eb.build());} else {this.addnewfield();}},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:timeout", guild, member, channel).queue();});
	}
	
	private void addnewfield() {
		EventWaiter waiter = bot.getWaiter();
		AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:addnewfield", guild, member, channel).queue();
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {messagecount++;
								  String[] temp1 = e.getMessage().getContentRaw().split("\\+");
								  eb.addField(temp1[0], temp1[1], true);
								  this.wantnewfield();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:timeout", guild, member, channel).queue();});
	}
	
	private void sendMessage(MessageEmbed embed) {
		this.cleanup();
		channel.sendMessageEmbeds(embed).queue();
	}
	
	private void cleanup() {
		List<Message> messages = channel.getHistory().retrievePast(messagecount).complete();
		channel.deleteMessages(messages).queue();
	}
}
