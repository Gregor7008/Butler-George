package commands.moderation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Goodbye implements Command {

	@Override
	public void perform(SlashCommandEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/goodbye:nopermission")).queue();
			return;
		}
		if (event.getSubcommandName().equals("set")) {
			String message = event.getOption("message").toString();
			String channelid = event.getOption("channel").getAsGuildChannel().getId();
			Configloader.INSTANCE.setGuildConfig(guild, "goodbyemsg", message + ";" + channelid);
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/goodbye:setsuccess")).queue();
			return;
		}
		if (event.getSubcommandName().equals("off")) {
			Configloader.INSTANCE.setGuildConfig(guild, "goodbyemsg", "");
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/goodbye:offsuccess")).queue();
			return;
		}
		if (event.getSubcommandName().equals("test")) {
			String goodbyemsgraw = Configloader.INSTANCE.getGuildConfig(guild, "goodbyemsg");
			LocalDateTime date = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyy - HH:mm");
			String currentdate = date.format(formatter);
			if (goodbyemsgraw != "") {
				String[] goodbyemsg = goodbyemsgraw.split(";");
				goodbyemsg[0].replace("{servername}", guild.getName());
				goodbyemsg[0].replace("{member}", event.getMember().getEffectiveName());
				goodbyemsg[0].replace("{membercount}", Integer.toString(guild.getMemberCount()));
				goodbyemsg[0].replace("{date}", currentdate);
				goodbyemsg[0].replace("{timejoined}", event.getMember().getTimeJoined().format(formatter));
				guild.getTextChannelById(goodbyemsg[1]).sendMessage(goodbyemsg[0]).queue();
			} else {
				event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/goodbye:nonedefined")).queue();
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
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/moderation/goodbye:help");
	}
}