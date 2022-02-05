package commands.utilities;

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
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class Embed implements Command{

	private SlashCommandEvent oevent;
	private EmbedBuilder eb;
	private EventWaiter waiter = Bot.INSTANCE.getWaiter();
	private Member member;
	private User user;
	private Guild guild;
	private TextChannel channel;
	private List<Message> messages;
	
	@Override
	public void perform(SlashCommandEvent event) {
		oevent = event;
		member = event.getMember();
		user = event.getUser();
		guild = event.getGuild();
		channel = event.getTextChannel();
		if (!member.hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/embed:nopermission")).queue();
			return;
		}
		eb = new EmbedBuilder();
		eb.setAuthor(member.getEffectiveName(), null, user.getAvatarUrl());	
		eb.setColor(56575);
		this.definetitle();
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("embed", "Creates a custom embedded message!");
		return command;
	}
	
	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/utilities/embed:help");
	}
	
	private void definetitle() {
		oevent.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/embed:definetitle")).queue();
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {eb.setTitle(e.getMessage().getContentRaw());
								  messages.add(e.getMessage());
								  this.definedescr();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"general:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	private void definedescr() {
		messages.add(channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/embed:definedescr")).complete());
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {eb.setDescription(e.getMessage().getContentRaw());
								  messages.add(e.getMessage());
								  this.definefooter();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"general:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void definefooter() {
		messages.add(channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/embed:definefooter")).complete());
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {if (e.getMessage().getContentRaw()=="none"){} else {eb.setFooter(e.getMessage().getContentRaw());}
							  	  messages.add(e.getMessage());
								  this.defineTNail();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"general:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	private void defineTNail() {
		messages.add(channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/embed:defineTNail")).complete());
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {if (e.getMessage().getContentRaw().contains("none")){} else {eb.setThumbnail(e.getMessage().getContentRaw());}
								  messages.add(e.getMessage());
								  this.defineImag();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"general:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void defineImag() {
		messages.add(channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/embed:defineImag")).complete());
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {if (e.getMessage().getContentRaw().contains("none")){} else {eb.setImage(e.getMessage().getContentRaw());}
								  messages.add(e.getMessage());
								  this.wantnewfield();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"general:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void wantnewfield() {
		messages.add(channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/embed:wantnewfield")).complete());
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {messages.add(e.getMessage());
								  if(e.getMessage().getContentRaw().contains("no")) {this.sendMessage(eb.build());} else {this.addnewfield();}},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"general:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void addnewfield() {
		messages.add(channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/embed:addnewfield")).complete());
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {messages.add(e.getMessage());
								  String[] temp1 = e.getMessage().getContentRaw().split("\\+");
								  eb.addField(temp1[0], temp1[1], true);
								  this.wantnewfield();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"general:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
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