package commands.moderation;

import java.time.OffsetDateTime;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class TempBan implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getOption("member").getAsUser();
		if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, event.getUser(),"/commands/moderation/tempban:nopermission")).queue();
			return;
		}
		this.tempban(Integer.parseInt(event.getOption("days").getAsString()), guild, user);
		event.replyEmbeds(AnswerEngine.ae.buildMessage(
				AnswerEngine.ae.getTitle(guild, user, "/commands/moderation/tempban:success"),
				AnswerEngine.ae.getDescription(guild, user, "/commands/moderation/tempban:success").replace("{user}", user.getName()).replace("{time}", event.getOption("days").getAsString()))).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("tempban", "Bans a user temporarily")
												.addOptions(new OptionData(OptionType.USER, "member", "The member you want to ban", true))
												.addOptions(new OptionData(OptionType.INTEGER, "days", "The number of days you want the member to be banned", true));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/moderation/tempban:help");
	}
	
	public void tempban(int days, Guild guild, User user) {
		OffsetDateTime until = OffsetDateTime.now().plusDays(Long.parseLong(String.valueOf(days)));
		Configloader.INSTANCE.setUserConfig(guild, user, "tbuntil", until.toString());
		Configloader.INSTANCE.setUserConfig(guild, user, "tempbanned", "true");
		guild.getMember(user).ban(0).queue();
		Bot.INSTANCE.modCheck(guild);
	}	
}