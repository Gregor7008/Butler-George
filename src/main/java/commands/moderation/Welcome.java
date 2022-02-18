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

public class Welcome implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/welcome:nopermission")).queue();
			return;
		}
		if (event.getSubcommandName().equals("set")) {
			String message = event.getOption("message").getAsString();
			String channelid = event.getOption("channel").getAsGuildChannel().getId();
			Configloader.INSTANCE.setGuildConfig(guild, "welcomemsg", message + ";" + channelid);
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/welcome:setsuccess")).queue();
			return;
		}
		if (event.getSubcommandName().equals("off")) {
			Configloader.INSTANCE.setGuildConfig(guild, "welcomemsg", "");
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/welcome:offsuccess")).queue();
			return;
		}
		if (event.getSubcommandName().equals("test")) {
			String welcomemsgraw = Configloader.INSTANCE.getGuildConfig(guild, "welcomemsg");
			LocalDateTime date = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyy - HH:mm");
			String currentdate = date.format(formatter);
			if (welcomemsgraw != "") {
				String[] welcomemsg = welcomemsgraw.split(";");
				String msg = welcomemsg[0].replace("{server}", guild.getName()).replace("{member}", event.getMember().getAsMention())
							.replace("{membercount}", Integer.toString(guild.getMemberCount())).replace("{date}", currentdate);
				guild.getTextChannelById(welcomemsg[1]).sendMessage(msg).queue();
				event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/welcome:testsuccess")).queue();
			} else {
				event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/welcome:nonedefined")).queue();
			}
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("welcome", "0")
									.addSubcommands(new SubcommandData("set", "Set the welcome message, that'll be send whenever a new member joins")
											  .addOptions(new OptionData(OptionType.STRING, "message", "Variables:{member} {membercount} {server} {date}!").setRequired(true))
											  .addOptions(new OptionData(OptionType.CHANNEL, "channel", "Provide the channel where the message should be send in").setRequired(true)))
									.addSubcommands(new SubcommandData("off", "Turns welcome messages off"))
									.addSubcommands(new SubcommandData("test", "Test the welcome message"));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/moderation/welcome:help");
	}
}