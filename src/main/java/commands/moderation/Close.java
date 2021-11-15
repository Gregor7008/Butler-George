package commands.moderation;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class Close implements Command {

	@Override
	public void perform(SlashCommandEvent event) {
		if (Configloader.INSTANCE.getMailConfig1(event.getChannel().getName()).equals(null)) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/close:nochannel")).queue();
			return;
		}
		String cname = event.getChannel().getName();
		event.getTextChannel().delete().queue();
		Bot.INSTANCE.jda.getUserById(Configloader.INSTANCE.getMailConfig1(cname)).openPrivateChannel().complete().sendMessageEmbeds(
				AnswerEngine.getInstance().fetchMessage(event.getGuild(), Bot.INSTANCE.jda.getUserById(Configloader.INSTANCE.getMailConfig1(cname)), "/commands/moderation/close:closed")).queue();
		Configloader.INSTANCE.removeMailConfig(cname);
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("close", "Closes the anonymous conversation, opened via ModMail");
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/moderation/close:help");
	}

}
