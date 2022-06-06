package commands.moderation;

import java.util.List;
import java.util.concurrent.TimeUnit;

import commands.Command;
import components.base.AnswerEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Clear implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		TextChannel channel = event.getTextChannel();
		int count = Integer.parseInt(event.getOption("count").getAsString());
		List<Message> messages = channel.getHistory().retrievePast(count).complete();
		try {channel.deleteMessages(messages).queue();} catch (Exception e) {
			event.replyEmbeds(AnswerEngine.build.fetchMessage(guild, user,"/commands/moderation/clear:error").convert()).queue(response -> response.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
			return;
		}
		event.replyEmbeds(AnswerEngine.build.fetchMessage(guild, user,"/commands/moderation/clear:done").convert()).queue(response -> response.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("clear", "Deletes a specific number of messages from this channel!")
				.addOptions(new OptionData(OptionType.INTEGER, "count", "Hand over the number of messages you want to delete!", true));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.build.getRaw(guild, user, "/commands/moderation/clear:help");
	}
}