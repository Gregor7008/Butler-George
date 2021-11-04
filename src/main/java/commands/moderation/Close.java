package commands.moderation;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
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
		Bot.INSTANCE.jda.getUserById(Configloader.INSTANCE.getMailConfig1(cname)).openPrivateChannel().complete().sendMessage("Your anonymous conversation has been closed. Everything send from now on, will be processed as a new ModMail message!").queue();
		Configloader.INSTANCE.deleteMailConfig(cname);
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("close", "Closes the anonymous conversation, opened via ModMail");
		return command;
	}

	@Override
	public String getHelp() {
		return "This command is used to close a conversation with an anonymous user, which was opened by him via the ModMail function.\nUse this command in the channel the conversation took place in!";
	}

}
