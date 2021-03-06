package commands.moderation;

import java.util.List;
import java.util.concurrent.TimeUnit;

import commands.Command;
import components.base.AnswerEngine;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Clear implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/clear:nopermission")).queue();
			return;
		}
		TextChannel channel = event.getTextChannel();
		int count = Integer.parseInt(event.getOption("count").getAsString());
		List<Message> messages = channel.getHistory().retrievePast(count).complete();
		try {channel.deleteMessages(messages).queue();} catch (Exception e) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/clear:error")).queue(response -> response.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
			return;
		}
		event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/clear:done")).queue(response -> response.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("clear", "Deletes a specific number of messages from this channel!")
				.addOptions(new OptionData(OptionType.INTEGER, "count", "Hand over the number of messages you want to delete!", true));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/moderation/clear:help");
	}
}