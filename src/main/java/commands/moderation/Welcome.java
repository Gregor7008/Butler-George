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

public class Welcome implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		if (event.getMember().hasPermission(Permission.MANAGE_SERVER) && !Developerlist.getInstance().developers.contains(event.getMember().getId())) {
			event.replyEmbeds(AnswerEngine.getInstance().buildMessage("No Permission!", ":warning: | You have no permission to use this command!\n You need to have the permission to manage the server to get access to this command!"));
			return;
		}
		if (event.getSubcommandName().equals("set")) {
			final String message = event.getOption("message").getAsString();
			final String channelid = event.getOption("channel").getAsGuildChannel().getId();
			Configloader.INSTANCE.setGuildConfig(event.getGuild(), "welcomemsg", message + ";" + channelid);
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/welcome:setsuccess"));
			return;
		}
		if (event.getSubcommandName().equals("off")) {
			Configloader.INSTANCE.setGuildConfig(event.getGuild(), "welcomemsg", "");
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/welcome:offsuccess"));
			return;
		}
		if (event.getSubcommandName().equals("test")) {
			String welcomemsgraw = Configloader.INSTANCE.getGuildConfig(event.getGuild(), "welcomemsg");
			LocalDateTime date = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyy - HH:mm");
			String currentdate = date.format(formatter);
			if (welcomemsgraw != "") {
				String[] welcomemsg = welcomemsgraw.split(";");
				welcomemsg[0].replace("{servername}", event.getGuild().getName());
				welcomemsg[0].replace("{membername}", event.getMember().getAsMention());
				welcomemsg[0].replace("{membercount}", Integer.toString(event.getGuild().getMemberCount()));
				welcomemsg[0].replace("{date}", currentdate);
				event.getGuild().getTextChannelById(welcomemsg[1]).sendMessage(welcomemsg[0]).queue();
			} else {
				event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/welcome:nonedefined"));
			}
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("welcome", "Configure the welcome message, that will be send whenever a new member joins your server!")
									.addSubcommands(new SubcommandData("set", "Set the welcome message")
											  .addOptions(new OptionData(OptionType.STRING, "message", "Variables:{member} {membercount} {servername} {date}!").setRequired(true))
											  .addOptions(new OptionData(OptionType.CHANNEL, "channel", "Provide the channel where the message should be send in").setRequired(true)))
									.addSubcommands(new SubcommandData("off", "Turn welcome messages off again!"))
									.addSubcommands(new SubcommandData("test", "Test the welcome message"));
		return command;
	}

	@Override
	public String getHelp() {
		return "With this command you can define the welcome message, that will be send when a new member joines the server. The channel will be defined in the same process!";
	}

}
