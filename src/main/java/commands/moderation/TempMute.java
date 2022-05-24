package commands.moderation;

import java.util.concurrent.TimeUnit;

import base.Bot;
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

public class TempMute implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getOption("member").getAsUser();
		this.tempmute(Integer.parseInt(event.getOption("days").getAsString()), guild, user);
		event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/tempmute:success")
				.replaceDescription("{user}", user.getName())
				.replaceDescription("{time}", event.getOption("days").getAsString()).convert()).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("tempmute", "Mutes a user temporarily")
				.addOptions(new OptionData(OptionType.USER, "member", "The member you want to mute").setRequired(true))
				.addOptions(new OptionData(OptionType.INTEGER, "days", "The number of days you want the member to be muted").setRequired(true));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/moderation/tempmute:help");
	}
	
	private void tempmute(int days, Guild guild, User user) {
		ConfigLoader.run.getUserConfig(guild, user).put("tempmuted", true);
		guild.getMember(user).timeoutFor(days, TimeUnit.DAYS).queue();
		Bot.INSTANCE.modCheck(guild);
	}
}