package commands.moderation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import commands.Command;
import components.base.AnswerEngine;
import components.base.ConfigLoader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Welcome implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (event.getSubcommandName().equals("set")) {
			String message = event.getOption("message").getAsString();
			String channelid = event.getOption("channel").getAsGuildChannel().getId();
			ConfigLoader.run.getGuildConfig(guild).put("welcomemsg", message + ";" + channelid);
			event.replyEmbeds(AnswerEngine.build.fetchMessage(guild, user,"/commands/moderation/welcome:setsuccess").convert()).queue();
			return;
		}
		if (event.getSubcommandName().equals("off")) {
			ConfigLoader.run.getGuildConfig(guild).put("welcomemsg", "");
			event.replyEmbeds(AnswerEngine.build.fetchMessage(guild, user,"/commands/moderation/welcome:offsuccess").convert()).queue();
			return;
		}
		if (event.getSubcommandName().equals("test")) {
			String welcomemsgraw = ConfigLoader.run.getGuildConfig(guild).getString("welcomemsg");
			LocalDateTime date = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyy - HH:mm");
			String currentdate = date.format(formatter);
			if (welcomemsgraw != "") {
				String[] welcomemsg = welcomemsgraw.split(";");
				String msg = welcomemsg[0].replace("{server}", guild.getName()).replace("{member}", event.getMember().getAsMention())
							.replace("{membercount}", Integer.toString(guild.getMemberCount())).replace("{date}", currentdate);
				guild.getTextChannelById(welcomemsg[1]).sendMessage(msg).queue();
				event.replyEmbeds(AnswerEngine.build.fetchMessage(guild, user, "/commands/moderation/welcome:testsuccess").convert()).queue();
			} else {
				event.replyEmbeds(AnswerEngine.build.fetchMessage(guild, user,"/commands/moderation/welcome:nonedefined").convert()).queue();
			}
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("welcome", "0")
									.addSubcommands(new SubcommandData("set", "Set the welcome message, that'll be send whenever a new member joins")
											  .addOptions(new OptionData(OptionType.STRING, "message", "Variables:{member} {membercount} {server} {date}!").setRequired(true))
											  .addOptions(new OptionData(OptionType.CHANNEL, "channel", "Provide the channel where the message should be send in").setRequired(true)))
									.addSubcommands(new SubcommandData("off", "Turns welcome messages off"))
									.addSubcommands(new SubcommandData("test", "Test the welcome message"));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.build.getRaw(guild, user, "/commands/moderation/welcome:help");
	}
}