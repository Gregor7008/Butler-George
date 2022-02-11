package commands.utilities;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Giveaway implements Command{

	private SlashCommandEvent oevent;
	private Guild guild;
	private User user;
	private TextChannel channel;
	private List<Message> messages = new ArrayList<>();
	final EventWaiter waiter = Bot.INSTANCE.getWaiter();
	final EmbedBuilder eb = new EmbedBuilder();
	
	@Override
	public void perform(SlashCommandEvent event) {
		oevent = event;
		guild = event.getGuild();
		user = event.getUser();
		channel = event.getTextChannel();
		if (event.getSubcommandName().equals("delete")) {
			String giveaways = Configloader.INSTANCE.getUserConfig(guild, user, "giveaways");
			String channelid = event.getOption("channel").getAsGuildChannel().getId();
			String msgid = event.getOption("msgid").getAsString();
			if (giveaways.contains(channelid + "_" + msgid)) {
				guild.getTextChannelById(channelid).retrieveMessageById(msgid).complete().delete().queue();
				event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/utilities/giveaway:remsuccess")).queue();
				Configloader.INSTANCE.deleteUserConfig(guild, user, "giveaways", channelid + "_" + msgid);
				return;
			} else {
				event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/utilities/giveaway:remfail")).queue();
				return;
			}
		}
		if (event.getSubcommandName().equals("create")) {
			eb.setAuthor(user.getName(), null, user.getAvatarUrl());
			eb.setColor(56575);
			if (event.getOption("days") != null) {
				OffsetDateTime until = OffsetDateTime.now().plusDays(Long.parseLong(event.getOption("days").getAsString()));
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm | dd.MM.yyy");
				eb.setFooter("Ends: " + until.format(formatter));
			}
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/giveaway:title")).queue();
			waiter.waitForEvent(GuildMessageReceivedEvent.class,
								e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
								  	  return e.getAuthor().getIdLong() == user.getIdLong();},
								e -> {eb.setTitle(e.getMessage().getContentRaw());
									  messages.add(e.getMessage());
									  this.defineDescr();},
								1, TimeUnit.MINUTES,
								() -> {this.cleanup();
									   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"general:timeout")).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("giveaway", "0")
				.addSubcommands(new SubcommandData("create", "Creates a new giveaway")
						.addOption(OptionType.CHANNEL, "channel", "The channel the giveaway should be send in", true)
						.addOption(OptionType.INTEGER, "days", "The amount of days the giveaway should be active"))
				.addSubcommands(new SubcommandData("delete", "Deletes one of your giveaways")
						.addOption(OptionType.CHANNEL, "channel", "The channel the giveaway was sent in", true)
						.addOption(OptionType.STRING, "msgid", "The id of the message of your giveaway", true));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/utilities/giveaway:help");
	}
	
	private void defineDescr() {
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/utilities/giveaway:description")).queue(r -> messages.add(r));
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
				e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
				  	  return e.getAuthor().getIdLong() == user.getIdLong();},
				e -> {eb.setDescription(e.getMessage().getContentDisplay());
					  messages.add(e.getMessage());
					  this.defineThumbnail();},
				1, TimeUnit.MINUTES,
				() -> {this.cleanup();
					   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"general:timeout")).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void defineThumbnail() {
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/utilities/giveaway:thumbnail")).queue(r -> messages.add(r));
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
				e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
				  	  return e.getAuthor().getIdLong() == user.getIdLong();},
				e -> {if (!e.getMessage().getContentRaw().contains("none")){
						eb.setThumbnail(e.getMessage().getContentRaw());}
					  messages.add(e.getMessage());
					  this.defineFields();},
				1, TimeUnit.MINUTES,
				() -> {this.cleanup();
					   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"general:timeout")).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void defineFields() {
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/utilities/giveaway:notes")).queue(r -> messages.add(r));
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
				e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
				  	  return e.getAuthor().getIdLong() == user.getIdLong();},
				e -> {if (!e.getMessage().getContentRaw().contains("none")) {
				  	  	eb.addField("Notes:", e.getMessage().getContentDisplay(), false);}
					  messages.add(e.getMessage());
					  this.defineMentiones();},
				1, TimeUnit.MINUTES,
				() -> {this.cleanup();
					   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"general:timeout")).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void defineMentiones() {
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/utilities/giveaway:mentiones")).queue(r -> messages.add(r));
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
				e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
				  	  return e.getAuthor().getIdLong() == user.getIdLong();},
				e -> {messages.add(e.getMessage());
					  this.sendGiveaway(e.getMessage());},
				1, TimeUnit.MINUTES,
				() -> {this.cleanup();
					   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"general:timeout")).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void sendGiveaway(Message msg) {
		this.cleanup();
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/utilities/giveaway:success")).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));
		TextChannel finalch = guild.getTextChannelById(oevent.getOption("channel").getAsGuildChannel().getId());
		if (finalch != null) {
			List<Role> roles = msg.getMentionedRoles();
			if (roles.size() > 0) {
				StringBuilder sb = new StringBuilder();
				roles.forEach(r -> sb.append(r.getAsMention() + " "));
				finalch.sendMessage(sb.toString()).queue(r -> r.delete().queueAfter(3, TimeUnit.SECONDS));
			}
			finalch.sendMessageEmbeds(eb.build()).queue(r -> Configloader.INSTANCE.addUserConfig(guild, user, "giveaways", finalch.getId() + "_" + r.getId()));
		} else {
			channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "general:fatal")).queue();
		}
	}
	
	private void cleanup() {
		channel.deleteMessages(messages).queue();
		oevent.getHook().deleteOriginal().queue();
	}
}