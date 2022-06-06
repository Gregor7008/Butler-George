package commands.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Embed implements Command{

	private SlashCommandInteractionEvent oevent;
	private EmbedBuilder eb;
	private EventWaiter waiter = Bot.run.getWaiter();
	private Member member;
	private User user;
	private Guild guild;
	private TextChannel channel;
	private List<Message> messages = new ArrayList<>();
	
	@Override
	public void perform(SlashCommandInteractionEvent event) {
		oevent = event;
		member = event.getMember();
		user = event.getUser();
		guild = event.getGuild();
		channel = event.getTextChannel();
		if (!member.hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
			event.replyEmbeds(AnswerEngine.build.fetchMessage(guild, user,"/commands/utilities/embed:nopermission").convert()).queue();
			return;
		}
		eb = new EmbedBuilder();
		eb.setAuthor(member.getEffectiveName(), null, user.getAvatarUrl());	
		eb.setColor(56575);
		this.definetitle();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("embed", "Creates a custom embedded message!");
		return command;
	}
	
	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.build.getRaw(guild, user, "/commands/utilities/embed:help");
	}
	
	private void definetitle() {
		oevent.replyEmbeds(AnswerEngine.build.fetchMessage(guild, user,"/commands/utilities/embed:definetitle").convert()).queue();
		waiter.waitForEvent(MessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {eb.setTitle(e.getMessage().getContentRaw());
								  messages.add(e.getMessage());
								  this.definedescr();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user,"general:timeout").convert()).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	private void definedescr() {
		messages.add(channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user,"/commands/utilities/embed:definedescr").convert()).complete());
		waiter.waitForEvent(MessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {eb.setDescription(e.getMessage().getContentRaw());
								  messages.add(e.getMessage());
								  this.definefooter();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user,"general:timeout").convert()).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void definefooter() {
		messages.add(channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user,"/commands/utilities/embed:definefooter").convert()).complete());
		waiter.waitForEvent(MessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {if (e.getMessage().getContentRaw()=="none"){} else {eb.setFooter(e.getMessage().getContentRaw());}
							  	  messages.add(e.getMessage());
								  this.defineTNail();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user,"general:timeout").convert()).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	private void defineTNail() {
		messages.add(channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user,"/commands/utilities/embed:defineTNail").convert()).complete());
		waiter.waitForEvent(MessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {if (e.getMessage().getContentRaw().contains("none")){} else {eb.setThumbnail(e.getMessage().getContentRaw());}
								  messages.add(e.getMessage());
								  this.defineImag();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user,"general:timeout").convert()).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void defineImag() {
		messages.add(channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user,"/commands/utilities/embed:defineImag").convert()).complete());
		waiter.waitForEvent(MessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {if (e.getMessage().getContentRaw().contains("none")){} else {eb.setImage(e.getMessage().getContentRaw());}
								  messages.add(e.getMessage());
								  this.wantnewfield();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user,"general:timeout").convert()).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void wantnewfield() {
		messages.add(channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user,"/commands/utilities/embed:wantnewfield").convert()).complete());
		waiter.waitForEvent(MessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {messages.add(e.getMessage());
								  if(e.getMessage().getContentRaw().contains("no")) {this.sendMessage(eb.build());} else {this.addnewfield();}},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user,"general:timeout").convert()).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void addnewfield() {
		messages.add(channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user,"/commands/utilities/embed:addnewfield").convert()).complete());
		waiter.waitForEvent(MessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {messages.add(e.getMessage());
								  String[] temp1 = e.getMessage().getContentRaw().split("\\+");
								  eb.addField(temp1[0], temp1[1], true);
								  this.wantnewfield();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user,"general:timeout").convert()).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void sendMessage(MessageEmbed embed) {
		this.cleanup();
		channel.sendMessageEmbeds(embed).queue();
	}
	
	private void cleanup() {
		channel.deleteMessages(messages).queue();
		oevent.getHook().deleteOriginal().queue();
	}
}