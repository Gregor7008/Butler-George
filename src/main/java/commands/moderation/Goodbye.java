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

public class Goodbye implements Command {

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (event.getSubcommandName().equals("set")) {
			String message = event.getOption("message").toString();
			String channelid = event.getOption("channel").getAsGuildChannel().getId();
			ConfigLoader.run.getGuildConfig(guild).put("goodbyemsg", message + ";" + channelid);
			event.replyEmbeds(AnswerEngine.run.fetchMessage(guild, user,"/commands/moderation/goodbye:setsuccess").convert()).queue();
			return;
		}
		if (event.getSubcommandName().equals("off")) {
			ConfigLoader.run.getGuildConfig(guild).put("goodbyemsg", "");
			event.replyEmbeds(AnswerEngine.run.fetchMessage(guild, user,"/commands/moderation/goodbye:offsuccess").convert()).queue();
			return;
		}
		if (event.getSubcommandName().equals("test")) {
			String goodbyemsgraw = ConfigLoader.run.getGuildConfig(guild).getString("goodbyemsg");
			LocalDateTime date = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyy - HH:mm");
			String currentdate = date.format(formatter);
			if (goodbyemsgraw != "") {
				String[] goodbyemsg = goodbyemsgraw.split(";");
				String msg = goodbyemsg[0].replace("{server}", guild.getName()).replace("{member}", event.getMember().getEffectiveName())
							.replace("{membercount}", Integer.toString(guild.getMemberCount()))
							.replace("{timejoined}", event.getMember().getTimeJoined().format(formatter)).replace("{date}", currentdate);
				guild.getTextChannelById(goodbyemsg[1]).sendMessage(msg).queue();
				event.replyEmbeds(AnswerEngine.run.fetchMessage(guild, user, "/commands/moderation/goodbye:testsuccess").convert()).queue();
			} else {
				event.replyEmbeds(AnswerEngine.run.fetchMessage(guild, user,"/commands/moderation/goodbye:nonedefined").convert()).queue();
			}
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("goodbye", "0")
				.addSubcommands(new SubcommandData("set", "Sets the goodbye message, that'll be send whenever a member leaves the server!")
						  .addOptions(new OptionData(OptionType.STRING, "message", "Variables:{member} {membercount} {server} {date} {timejoined}!", true))
						  .addOptions(new OptionData(OptionType.CHANNEL, "channel", "Provide the channel where the message should be send in", true)))
				.addSubcommands(new SubcommandData("off", "Turns goodbye messages off"))
				.addSubcommands(new SubcommandData("test", "Tests the goodbye message"));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.run.getRaw(guild, user, "/commands/moderation/goodbye:help");
	}
}