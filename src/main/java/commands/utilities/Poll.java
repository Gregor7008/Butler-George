package commands.utilities;

import java.io.File;
import java.io.FilenameFilter;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
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
	
	private SlashCommandEvent oevent;
	private int messagecount = 0;
	private TextChannel channel;
	private User user;
	private Guild guild;
	private String title, url, description, answers;
	private boolean anym;
	
	@Override
	public void perform(SlashCommandEvent event) {
		oevent = event;
		switch (event.getSubcommandName()) {
		case "create":
			this.createPoll();
			break;
		case "remove":
			this.removePoll();
			break;
		case "list":
			this.listPoll();
			break;
		case "info":
			this.infoPoll();
			break;
		default:
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/utilities/poll:error")).queue();
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("poll", "Manage polls")
											 .addSubcommands(new SubcommandData("create", "Create a poll"))
											 .addSubcommands(new SubcommandData("remove", "Delete a poll")
													 		 .addOption(OptionType.STRING, "title", "Define the title of the poll you want to delete", true))
											 .addSubcommands(new SubcommandData("list", "Lists all active polls"))
											 .addSubcommands(new SubcommandData("info", "Show details about a specific poll")
													 	     .addOption(OptionType.STRING, "title", "Define the title of the poll you want to delete", true));
		return command;
	}

	@Override
	public String getHelp() {
		return "This command is used to create a poll in the chanenl, the command is executed in";
	}
	
	private void createPoll() {
		if (!oevent.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
			oevent.replyEmbeds(AnswerEngine.getInstance().fetchMessage(oevent.getGuild(), oevent.getUser(),"/commands/utilities/poll:nopermission")).queue();
			return;
		}
		channel = oevent.getTextChannel();
		user = oevent.getUser();
		guild = oevent.getGuild();
		
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		oevent.replyEmbeds(AnswerEngine.getInstance().fetchMessage(oevent.getGuild(), oevent.getUser(),"/commands/utilities/poll:definetitle")).queue();
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {title = e.getMessage().getContentRaw();
								  Configloader.INSTANCE.createPollConfig(guild, title);
								  Configloader.INSTANCE.setPollConfig(guild, title, "guild", guild.getId());
								  Configloader.INSTANCE.setPollConfig(guild, title, "user", user.getId());
								  Configloader.INSTANCE.setPollConfig(guild, title, "channel", channel.getId());
								  messagecount++;
								  this.definedescr();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(oevent.getGuild(), oevent.getUser(),"/commands/utilities/poll:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void definedescr() {
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(oevent.getGuild(), oevent.getUser(),"/commands/utilities/poll:definedescr")).queue();
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {description = e.getMessage().getContentRaw();
								  Configloader.INSTANCE.setPollConfig(guild, title, "description", description);
								  messagecount++;
								  this.defineAnswers();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(oevent.getGuild(), oevent.getUser(),"/commands/utilities/poll:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}
	
	private void defineAnswers() {
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(oevent.getGuild(), oevent.getUser(),"/commands/utilities/poll:defineAnswers")).queue();
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {answers = e.getMessage().getContentRaw();
								  Configloader.INSTANCE.setPollConfig(guild, title, "answers", answers);
								  messagecount++;
								  this.defineThumbnail();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(oevent.getGuild(), oevent.getUser(),"/commands/utilities/poll:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	private void defineThumbnail() {
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(oevent.getGuild(), oevent.getUser(),"/commands/utilities/poll:defineTNURL")).queue();
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {if(e.getMessage().getContentRaw().equals("none")) {
									  url = "";
								  } else {
									  url = e.getMessage().getContentRaw();
								  }
								  Configloader.INSTANCE.setPollConfig(guild, title, "thumbnail", url);
								  messagecount++;
								  this.defineDays();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(oevent.getGuild(), oevent.getUser(),"/commands/utilities/poll:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	private void defineDays() {
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(oevent.getGuild(), oevent.getUser(),"/commands/utilities/poll:definedays")).queue();
		messagecount++;
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == user.getIdLong();},
							e -> {Configloader.INSTANCE.setPollConfig(guild, title, "days", e.getMessage().getContentRaw());
								  messagecount++;
								  this.defineAnonymous();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(oevent.getGuild(), oevent.getUser(),"/commands/utilities/poll:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	private void defineAnonymous() {
		EventWaiter waiter = Bot.INSTANCE.getWaiter();
		Message msg = channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(oevent.getGuild(), oevent.getUser(),"/commands/utilities/poll:defineanonymous")).complete();
		msg.addReaction("U+2705").queue();
		msg.addReaction("U+274C").queue();
		messagecount++;
		waiter.waitForEvent(MessageReactionAddEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getUser().getIdLong() == user.getIdLong();},
							e -> {if (e.getReactionEmote().getAsCodepoints().equals("U+2705")) {anym = true;} else {anym = false;}
								  Configloader.INSTANCE.setPollConfig(guild, title, "anonymous", String.valueOf(anym));
								  messagecount++;
								  this.sendPoll();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup();
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(oevent.getGuild(), oevent.getUser(),"/commands/utilities/poll:timeout")).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));});
	}

	private void sendPoll() {
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
		
		eb.setAuthor(user.getName(), null, user.getAvatarUrl());
		if (!url.equals("")) {
			eb.setThumbnail(url);
		}
		eb.setTitle(title);
		eb.setDescription(description);
		eb.setFooter(OffsetDateTime.now().format(formatter) + "\s--\sAnonymous: " + String.valueOf(anym));
		eb.addField("Total answers so far: 0\nPossible answers:", sb.toString(), false);
		eb.setColor(56575);
		
		Message msg = channel.sendMessageEmbeds(eb.build()).complete();
		if (ansplit.length < 10) {
			for (int i = 1; i <= ansplit.length; i++) {
				msg.addReaction("U+003" + String.valueOf(i)).queue();
			}
		}
		Configloader.INSTANCE.setPollConfig(guild, title, "msgid", msg.getId());
	}

	private void removePoll() {
		TextChannel channel = oevent.getGuild().getTextChannelById(Configloader.INSTANCE.getPollConfig(oevent.getGuild(), oevent.getOption("title").getAsString(), "channel"));
		channel.retrieveMessageById(Configloader.INSTANCE.getPollConfig(oevent.getGuild(), oevent.getOption("title").getAsString(), "msgid")).complete().delete().queue();
		if (Configloader.INSTANCE.findPollConfig(oevent.getGuild(), oevent.getOption("title").getAsString()).delete()) {
			oevent.replyEmbeds(AnswerEngine.getInstance().fetchMessage(oevent.getGuild(), oevent.getUser(),"/commands/utilities/poll:removesuccess")).queue();
		} else {
			oevent.replyEmbeds(AnswerEngine.getInstance().fetchMessage(oevent.getGuild(), oevent.getUser(),"/commands/utilities/poll:removefailed")).queue();
		}
	}
	
	private void listPoll() {
		if (!oevent.getMember().hasPermission(Permission.MANAGE_SERVER)) {
			oevent.replyEmbeds(AnswerEngine.getInstance().fetchMessage(oevent.getGuild(), oevent.getUser(),"/commands/utilities/poll:nopermission")).queue();
			return;
		}
		File fl = new File(Bot.INSTANCE.getBotConfig("resourcepath") + "/configs/polls/" + oevent.getGuild().getId());
		StringBuilder sb = new StringBuilder();
		EmbedBuilder eb = new EmbedBuilder();
		List<String> pollfiles = Arrays.asList(fl.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return new File(dir, name).isFile();
			}}));
		for (int i = 0; i < pollfiles.size(); i++) {
			String[] temp = pollfiles.get(i).split(".properties");
			sb.append("#" + String.valueOf(i+1) + "\s" + temp[0] + "\sby\s");
			if (i+1 == pollfiles.size()) {
				sb.append(Bot.INSTANCE.jda.getUserById(Configloader.INSTANCE.getPollConfig(oevent.getGuild(), temp[0], "user")).getName());
			} else {
				sb.append(Bot.INSTANCE.jda.getUserById(Configloader.INSTANCE.getPollConfig(oevent.getGuild(), temp[0], "user")).getName() + "\n");
			}
		}
		eb.setTitle("All the polls on this server:");
		eb.setAuthor(oevent.getUser().getName(), null, oevent.getUser().getAvatarUrl());
		eb.setFooter("Official-NoLimits Bot! - discord.gg/qHA2vUs");
		eb.setColor(56575);
		eb.setDescription(sb.toString());
		oevent.replyEmbeds(eb.build()).queue();
	}
	
	private void infoPoll() {
		if (Configloader.INSTANCE.findPollConfig(oevent.getGuild(), oevent.getOption("title").getAsString()) == null) {
			oevent.replyEmbeds(AnswerEngine.getInstance().fetchMessage(oevent.getGuild(), oevent.getUser(),"/commands/utilities/poll:pollnf")).queue();
			return;
		}
		EmbedBuilder eb = new EmbedBuilder();
		StringBuilder sb = new StringBuilder();
		String title = oevent.getOption("title").getAsString();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm | dd.MM.yyy");
		Guild guild = oevent.getGuild();
		String useranswers = Configloader.INSTANCE.getPollConfig(guild, title, "answercount");
		String[] answers = Configloader.INSTANCE.getPollConfig(guild, title, "answers").split(";");
		sb.append("Author:\s" + Bot.INSTANCE.jda.getUserById(Configloader.INSTANCE.getPollConfig(guild, title, "user")).getName() + "\n");
		sb.append("Time created:\s" + OffsetDateTime.parse(Configloader.INSTANCE.getPollConfig(guild, title, "creation")).format(formatter) + "\n");
		sb.append("Answers:\n");
		for (int i = 0; i < answers.length; i++) {
			sb.append("->\s" + answers[i] + ":\s" + String.valueOf(useranswers.split(String.valueOf(i+1)).length - 1) + "\n");
			//If the command was executed by the author of the poll and the poll isn't anonymous, list users as well 
		}
		sb.append("Time left:\s" + Duration.between(OffsetDateTime.now(),
				  OffsetDateTime.parse(Configloader.INSTANCE.getPollConfig(guild, title, "creation")).plusDays(Long.parseLong(Configloader.INSTANCE.getPollConfig(guild, title, "days")))).toHours() + "\shours");
		eb.setTitle("Information about the poll titled\s\"" + title + "\":");
		eb.setDescription(sb.toString());
		eb.setAuthor(oevent.getUser().getName(), null, oevent.getUser().getAvatarUrl());
		eb.setFooter("Official-NoLimits Bot! - discord.gg/qHA2vUs");
		eb.setColor(56575);
		oevent.replyEmbeds(eb.build()).queue();
	}
	
	private void cleanup() {
		List<Message> messages = channel.getHistory().retrievePast(messagecount).complete();
		channel.deleteMessages(messages).queue();
	}
	
	public void addAnswer(MessageReactionAddEvent event) {
		//implement method
	}
}
