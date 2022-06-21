package commands.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import components.base.LanguageEngine;
import components.commands.Command;
import components.utilities.ResponseDetector;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Embed implements Command {

	private SlashCommandInteractionEvent oevent;
	private EmbedBuilder eb;
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
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "/commands/utilities/embed:nopermission").convert()).queue();
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
	public boolean canBeAccessedBy(Member member) {
		return true;
	}
	
	private void definetitle() {
		oevent.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "/commands/utilities/embed:definetitle").convert()).queue();
		ResponseDetector.waitForMessage(guild, user, channel,
							e -> {eb.setTitle(e.getMessage().getContentRaw());
								  messages.add(e.getMessage());
								  this.definedescr();},
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "general:timeout").convert()).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	private void definedescr() {
		messages.add(channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "/commands/utilities/embed:definedescr").convert()).complete());
		ResponseDetector.waitForMessage(guild, user, channel,
							e -> {eb.setDescription(e.getMessage().getContentRaw());
								  messages.add(e.getMessage());
								  this.definefooter();},
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "general:timeout").convert()).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void definefooter() {
		messages.add(channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "/commands/utilities/embed:definefooter").convert()).complete());
		ResponseDetector.waitForMessage(guild, user, channel,
							e -> {if (e.getMessage().getContentRaw()=="none"){} else {eb.setFooter(e.getMessage().getContentRaw());}
							  	  messages.add(e.getMessage());
								  this.defineTNail();},
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "general:timeout").convert()).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	private void defineTNail() {
		messages.add(channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "/commands/utilities/embed:defineTNail").convert()).complete());
		ResponseDetector.waitForMessage(guild, user, channel,
							e -> {if (e.getMessage().getContentRaw().contains("none")){} else {eb.setThumbnail(e.getMessage().getContentRaw());}
								  messages.add(e.getMessage());
								  this.defineImag();},
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "general:timeout").convert()).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void defineImag() {
		messages.add(channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "/commands/utilities/embed:defineImag").convert()).complete());
		ResponseDetector.waitForMessage(guild, user, channel,
							e -> {if (e.getMessage().getContentRaw().contains("none")){} else {eb.setImage(e.getMessage().getContentRaw());}
								  messages.add(e.getMessage());
								  this.wantnewfield();},
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "general:timeout").convert()).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void wantnewfield() {
		messages.add(channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "/commands/utilities/embed:wantnewfield").convert()).complete());
		ResponseDetector.waitForMessage(guild, user, channel,
							e -> {messages.add(e.getMessage());
								  if(e.getMessage().getContentRaw().contains("no")) {this.sendMessage(eb.build());} else {this.addnewfield();}},
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "general:timeout").convert()).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void addnewfield() {
		messages.add(channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "/commands/utilities/embed:addnewfield").convert()).complete());
		ResponseDetector.waitForMessage(guild, user, channel,
							e -> {messages.add(e.getMessage());
								  String[] temp1 = e.getMessage().getContentRaw().split("\\+");
								  eb.addField(temp1[0], temp1[1], true);
								  this.wantnewfield();},
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "general:timeout").convert()).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});
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