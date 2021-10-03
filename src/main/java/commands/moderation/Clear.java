package commands.moderation;

import java.util.List;
import java.util.concurrent.TimeUnit;

import commands.Command;
import components.Developerlist;
import components.base.AnswerEngine;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Clear implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		final Member member = event.getMember();
		if (!member.hasPermission(Permission.MESSAGE_MANAGE) && !Developerlist.getInstance().developers.contains(event.getMember().getId())) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/clear:nopermission")).queue();
			return;
		}
		final TextChannel channel = event.getTextChannel();
		final int count = Integer.parseInt(event.getOption("count").getAsString());
		List<Message> messages = channel.getHistory().retrievePast(count).complete();
		try {channel.deleteMessages(messages).queue();} catch (Exception e) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/clear:error")).queue(response -> response.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
			e.printStackTrace();
			return;
		}
		event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/clear:done")).queue(response -> response.deleteOriginal().queueAfter(3, TimeUnit.SECONDS));
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("clear", "Clears a specific number of messages from this channel!").addOptions(new OptionData(OptionType.INTEGER, "count", "Hand over the number of messages you want to delete!"));
		return command;
	}

	@Override
	public String getHelp() {
		return "Use this command to clear messages from the channel you use the command in.";
	}

}
