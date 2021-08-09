package commands.utilities;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import commands.Command;
import components.AnswerEngine;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class Embed implements Command{

	private EmbedBuilder eb;
	private int messagecount;
	private Bot bot = Bot.INSTANCE;
	private Member member;
	private User user;
	private TextChannel channel;
	
	@Override
	public void perform(SlashCommandEvent event) {
		member = event.getMember();
		if (!member.hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/embed:nopermission"));
			return;
		}
		user = member.getUser();
		channel = event.getTextChannel();
		eb = new EmbedBuilder();
		eb.setAuthor(member.getEffectiveName(), null, member.getUser().getAvatarUrl());	
		eb.setColor(56575);
		messagecount=1;
		this.definetitle(event);
	}

	@Override
	public CommandData initialize(Guild guild) {
		CommandData command = new CommandData("embed", "Create an cutom embedded message!");
		return command;
	}
	
	@Override
	public String getHelp() {
		return "Use this command, and it will take you through the setup process!";
	}
	
	private void definetitle(SlashCommandEvent event) {
		EventWaiter waiter = bot.getWaiter();
		event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:definetitle")).queue();
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {eb.setTitle(e.getMessage().getContentRaw());
								  messagecount++;
								  this.definedescr();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	private void definedescr() {
		EventWaiter waiter = bot.getWaiter();
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:definedescr")).queue();
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {eb.setDescription(e.getMessage().getContentRaw());
								  messagecount++;
								  this.definefooter();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void definefooter() {
		EventWaiter waiter = bot.getWaiter();
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:definefooter")).queue();
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {if (e.getMessage().getContentRaw()=="none"){} else {eb.setFooter(e.getMessage().getContentRaw());}
								  messagecount++;
								  this.defineTNail();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	private void defineTNail() {
		EventWaiter waiter = bot.getWaiter();
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:defineTNail")).queue();
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {if (e.getMessage().getContentRaw().contains("none")){} else {eb.setThumbnail(e.getMessage().getContentRaw());}
								  messagecount++;
								  this.defineImag();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void defineImag() {
		EventWaiter waiter = bot.getWaiter();
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:defineImag")).queue();
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {if (e.getMessage().getContentRaw().contains("none")){} else {eb.setImage(e.getMessage().getContentRaw());}
								  messagecount++;
								  this.wantnewfield();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void wantnewfield() {
		EventWaiter waiter = bot.getWaiter();
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:wantnewfield")).queue();
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {messagecount++;
								  if(e.getMessage().getContentRaw().contains("no")) {this.sendMessage(eb.build());} else {this.addnewfield();}},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void addnewfield() {
		EventWaiter waiter = bot.getWaiter();
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:addnewfield")).queue();
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
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/utilities/embed:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
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
