package commands.moderation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import commands.Command;
import components.Developerlist;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Goodbye implements Command {

	@Override
	public void perform(SlashCommandEvent event) {
		if (event.getMember().hasPermission(Permission.MANAGE_SERVER) && !Developerlist.getInstance().developers.contains(event.getMember().getId())) {
			event.replyEmbeds(AnswerEngine.getInstance().buildMessage("No Permission!", ":warning: | You have no permission to use this command!\n You need to have the permission to manage the server to get access to this command!"));
			return;
		}
		if (event.getSubcommandName().equals("set")) {
			final String message = event.getOption("message").toString();
			final String channelid = event.getOption("channel").getAsGuildChannel().getId();
			Configloader.INSTANCE.setGuildConfig(event.getGuild(), "goodbyemsg", message + ";" + channelid);
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/goodbye:setsuccess"));
			return;
		}
		if (event.getSubcommandName().equals("off")) {
			Configloader.INSTANCE.setGuildConfig(event.getGuild(), "goodbyemsg", "");
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/goodbye:offsuccess"));
			return;
		}
		if (event.getSubcommandName().equals("test")) {
			String goodbyemsgraw = Configloader.INSTANCE.getGuildConfig(event.getGuild(), "goodbyemsg");
			LocalDateTime date = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyy - HH:mm");
			String currentdate = date.format(formatter);
			if (goodbyemsgraw != "") {
				String[] goodbyemsg = goodbyemsgraw.split(";");
				goodbyemsg[0].replace("{servername}", event.getGuild().getName());
				goodbyemsg[0].replace("{member}", event.getMember().getEffectiveName());
				goodbyemsg[0].replace("{membercount}", Integer.toString(event.getGuild().getMemberCount()));
				goodbyemsg[0].replace("{date}", currentdate);
				goodbyemsg[0].replace("{timejoined}", event.getMember().getTimeJoined().format(formatter));
				event.getGuild().getTextChannelById(goodbyemsg[1]).sendMessage(goodbyemsg[0]).queue();
			} else {
				event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/goodbye:nonedefined"));
			}
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("goodbye", "Configure the goodbye message, that will be send whenever a new member joins your server!")
				.addSubcommands(new SubcommandData("set", "Set the welcome message")
						  .addOptions(new OptionData(OptionType.STRING, "message", "Variables:{member} {membercount} {servername} {date} {timejoined}!").setRequired(true))
						  .addOptions(new OptionData(OptionType.CHANNEL, "channel", "Provide the channel where the message should be send in").setRequired(true)))
				.addSubcommands(new SubcommandData("off", "Turn goodbye messages off again!"))
				.addSubcommands(new SubcommandData("test", "Test the goodbye message"));
		return command;
	}

	@Override
	public String getHelp() {
		return "With this command you can define the goodbye message, that will be send when a member leaves the server. The channel will be defined in the same process!";
	}

}
