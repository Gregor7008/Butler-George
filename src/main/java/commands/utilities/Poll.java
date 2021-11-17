package commands.utilities;

import java.io.File;
import java.io.FilenameFilter;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Poll implements Command{
	
	private int messagecount = 0;
	private TextChannel channel;
	private User user;
	private Guild guild;
	private String url, description, answers, title, tempname;
	private boolean anym;
	
	@Override
	public void perform(SlashCommandEvent event) {
		channel = event.getTextChannel();
		user = event.getUser();
		guild = event.getGuild();
		if (!event.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/poll:nopermission")).queue();
			return;
		}
		switch (event.getSubcommandName()) {
		case "create":
			this.createPoll(event);
			break;
		case "remove":
			this.removePoll(event);
			break;
		case "list":
			this.listPoll(event);
			break;
		case "info":
			this.infoPoll(event);
			break;
		default:
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/poll:error")).queue();
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("poll", "Manage polls")
											 .addSubcommands(new SubcommandData("create", "Create a poll"))
											 .addSubcommands(new SubcommandData("remove", "Delete a poll")
													 		 .addOption(OptionType.STRING, "msgid", "The message ID of the poll", true))
											 .addSubcommands(new SubcommandData("list", "Lists all active polls"))
											 .addSubcommands(new SubcommandData("info", "Show details about a specific poll")
													 	     .addOption(OptionType.STRING, "msgid", "The message ID of the poll", true));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/utilities/poll:help");
	}
	
	private void createPoll(SlashCommandEvent event) {
		tempname = String.valueOf(new Random().nextInt(100));
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/poll:definetitle")).queue();
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {title = e.getMessage().getContentRaw();
								  Configloader.INSTANCE.createPollConfig(guild, tempname);
								  Configloader.INSTANCE.setPollConfig(guild, tempname, "guild", guild.getId());
								  Configloader.INSTANCE.setPollConfig(guild, tempname, "owner", user.getId());
								  Configloader.INSTANCE.setPollConfig(guild, tempname, "channel", channel.getId());
								  Configloader.INSTANCE.setPollConfig(guild, tempname, "title", title);
								  messagecount++;
								  this.definedescr();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/poll:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void definedescr() {
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/poll:definedescr")).queue();
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {description = e.getMessage().getContentRaw();
								  Configloader.INSTANCE.setPollConfig(guild, tempname, "description", description);
								  messagecount++;
								  this.defineAnswers();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/poll:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void defineAnswers() {
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/poll:defineAnswers")).queue();
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {answers = e.getMessage().getContentRaw();
								  Configloader.INSTANCE.setPollConfig(guild, tempname, "answers", answers);
								  messagecount++;
								  this.defineThumbnail();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/poll:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	private void defineThumbnail() {
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/poll:defineTNURL")).queue();
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {if(e.getMessage().getContentRaw().equals("none")) {
									  url = guild.getIconUrl();
								  } else {
									  url = e.getMessage().getContentRaw();
								  }
								  Configloader.INSTANCE.setPollConfig(guild, tempname, "thumbnail", url);
								  messagecount++;
								  this.defineDays();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/poll:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	private void defineDays() {
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/poll:definedays")).queue();
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {Configloader.INSTANCE.setPollConfig(guild, tempname, "days", e.getMessage().getContentRaw());
								  messagecount++;
								  this.defineAnonymous();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/poll:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	private void defineAnonymous() {
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		Message msg = channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/poll:defineanonymous")).complete();
		msg.addReaction("U+2705").queue();
		msg.addReaction("U+274C").queue();
		messagecount++;
		waiter.waitForEvent(MessageReactionAddEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getUser().getIdLong() == user.getIdLong();},
							e -> {if (e.getReactionEmote().getAsCodepoints().equals("U+2705")) {anym = true;} else {anym = false;}
								  Configloader.INSTANCE.setPollConfig(guild, tempname, "anonymous", String.valueOf(anym));
								  this.sendPoll();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/poll:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	private void sendPoll() {
		this.cleanup();
		EmbedBuilder eb = new EmbedBuilder();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm | dd.MM.yyy");
		StringBuilder sb = new StringBuilder();
		String[] ansplit = answers.split(";");
		for (int i = 0; i < ansplit.length; i++) {
			sb.append("#" + String.valueOf(i+1) + " ");
			if (i+1 == ansplit.length) {
				sb.append(ansplit[i]);
			} else {
				sb.append(ansplit[i] + "\n");
			}
		}
		
		String footer = OffsetDateTime.now().format(formatter) + "\s--\s"+ AnswerEngine.getInstance().getDescription(guild, user, "/commands/utilities/poll:field") + "\s" + String.valueOf(anym);
		eb.setAuthor(user.getName(), null, user.getAvatarUrl());
		if (!url.equals("")) {
			eb.setThumbnail(url);
		}
		eb.setTitle(title);
		eb.setDescription(description);
		eb.setFooter(footer);
		Configloader.INSTANCE.setPollConfig(guild, tempname, "footer", footer);
		eb.addField(AnswerEngine.getInstance().getTitle(guild, user, "/commands/utilities/poll:field"), sb.toString(), false);
		eb.setColor(56575);
		
		Message msg = channel.sendMessageEmbeds(eb.build()).complete();
		if (ansplit.length < 10) {
			for (int i = 1; i <= ansplit.length; i++) {
				msg.addReaction("U+003" + String.valueOf(i) + " U+20E3").queue();
				if (Configloader.INSTANCE.getPollConfig(guild, tempname, "answercount").equals("")) {
					Configloader.INSTANCE.setPollConfig(guild, tempname, "answercount", "0");
				} else {
					String current = Configloader.INSTANCE.getPollConfig(guild, tempname, "answercount");
					Configloader.INSTANCE.setPollConfig(guild, tempname, "answercount", current + ";0");
				}
			}
		}
		String path = Bot.INSTANCE.getBotConfig("resourcepath") + "/configs/polls/" + guild.getId() + "/";
		File renamed = new File(path + msg.getId() + ".properties");
		File pollfile = Configloader.INSTANCE.findPollConfig(guild, tempname);
		while (!pollfile.exists()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		pollfile.renameTo(renamed);
	}

	private void removePoll(SlashCommandEvent event) {
		String msgID = event.getOption("msgid").getAsString();
		TextChannel channel = guild.getTextChannelById(Configloader.INSTANCE.getPollConfig(guild, msgID, "channel"));
		channel.retrieveMessageById(msgID).complete().delete().queue();
		if (Configloader.INSTANCE.findPollConfig(guild, msgID).delete()) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/poll:removesuccess")).queue();
		} else {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/poll:removefailed")).queue();
		}
	}
	
	private void listPoll(SlashCommandEvent event) {
		File fl = new File(Bot.INSTANCE.getBotConfig("resourcepath") + "/configs/polls/" + guild.getId());
		StringBuilder sb = new StringBuilder();
		EmbedBuilder eb = new EmbedBuilder();
		List<String> pollfiles = Arrays.asList(fl.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return new File(dir, name).isFile();
			}}));
		for (int i = 0; i < pollfiles.size(); i++) {
			String[] temp = pollfiles.get(i).split(".properties");
			sb.append("#" + String.valueOf(i+1) + "\s" + Configloader.INSTANCE.getPollConfig(guild, temp[0], "title") + "\sby\s");
			if (i+1 == pollfiles.size()) {
				sb.append(Bot.INSTANCE.jda.getUserById(Configloader.INSTANCE.getPollConfig(guild, temp[0], "user")).getName());
			} else {
				sb.append(Bot.INSTANCE.jda.getUserById(Configloader.INSTANCE.getPollConfig(guild, temp[0], "user")).getName() + "\n");
			}
		}
		eb.setTitle(AnswerEngine.getInstance().getTitle(guild, user, "/commands/utilities/poll:list"));
		eb.setAuthor(user.getName(), null, user.getAvatarUrl());
		eb.setFooter("Official-NoLimits Bot! - discord.gg/qHA2vUs");
		eb.setColor(56575);
		if (sb.toString().equals("")) {
			eb.setDescription(AnswerEngine.getInstance().getDescription(guild, user, "/commands/utilities/poll:list"));
		} else {
			eb.setDescription(sb.toString());
		}
		event.replyEmbeds(eb.build()).queue();
	}
	
	private void infoPoll(SlashCommandEvent event) {
		if (Configloader.INSTANCE.findPollConfig(guild, event.getOption("msgid").getAsString()) == null) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/utilities/poll:pollnf")).queue();
			return;
		}
		if (!Configloader.INSTANCE.getPollConfig(guild, event.getOption("msgid").getAsString(), "owner").equals(user.getId())) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/utilities/poll:noinfoperm")).queue();
			return;
		}
		EmbedBuilder eb = new EmbedBuilder();
		StringBuilder sb = new StringBuilder();
		String name = event.getOption("msgid").getAsString();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm | dd.MM.yyy");
		String[] useranswers = Configloader.INSTANCE.getPollConfig(guild, name, "answercount").split(";");
		String[] users = Configloader.INSTANCE.getPollConfig(guild, name, "users").split(";");
		String[] answers = Configloader.INSTANCE.getPollConfig(guild, name, "answers").split(";");
		String creation = OffsetDateTime.parse(Configloader.INSTANCE.getPollConfig(guild, event.getOption("msgid").getAsString(), "creation")).format(formatter);
		sb.append("Author:\s" + Bot.INSTANCE.jda.getUserById(Configloader.INSTANCE.getPollConfig(guild, name, "owner")).getName() + "\n");
		sb.append("Time created:\s" + creation + "\n");
		sb.append("Anonymous:\s" + Configloader.INSTANCE.getPollConfig(guild, name, "anonymous") + "\n\n");
		sb.append("Answers:\n");
		for (int i = 0; i < answers.length; i++) {
			sb.append("=>\s" + answers[i] + ":\s" + useranswers[i] + "\n");
			if (!Boolean.parseBoolean(Configloader.INSTANCE.getPollConfig(guild, name, "anonymous"))) {
				int temp3 = 0;
				for (int e = 0; e < users.length; e++) {
					String[] temp2 = users[e].split("_");
					if (temp2[1].contains(String.valueOf(i))) {
						if (temp3 == 0) {
							sb.append("\s\s>\s ");
							temp3++;
						}
						sb.append(guild.getMemberById(temp2[0]).getEffectiveName() + ",\s");
					}
				}
				sb.append("\n");
			}
		}
		sb.append("Time left:\s" + Duration.between(OffsetDateTime.now(),
				  OffsetDateTime.parse(Configloader.INSTANCE.getPollConfig(guild, name, "creation")).plusDays(Long.parseLong(Configloader.INSTANCE.getPollConfig(guild, name, "days")))).toHours() + "\shours");
		eb.setTitle("Information about the poll titled\s\"" + Configloader.INSTANCE.getPollConfig(guild, name, "title") + "\":");
		eb.setDescription(sb.toString());
		eb.setAuthor(user.getName(), null, user.getAvatarUrl());
		eb.setFooter("Official-NoLimits Bot! - discord.gg/qHA2vUs");
		eb.setColor(56575);
		event.replyEmbeds(eb.build()).queue(response -> response.deleteOriginal().queueAfter(30, TimeUnit.SECONDS));
	}
	
	private void cleanup() {
		List<Message> messages = channel.getHistory().retrievePast(messagecount).complete();
		channel.deleteMessages(messages).queue();
	}
}