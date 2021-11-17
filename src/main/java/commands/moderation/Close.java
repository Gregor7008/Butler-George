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
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (!event.getMember().getRoles().contains(guild.getRoleById(Configloader.INSTANCE.getGuildConfig(guild, "supportrole")))) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/close:nopermission"));
			return;
		}
		if (event.getTextChannel().getName().contains("-support")) {
			event.getTextChannel().delete().queue();
			return;
		}
		if (Configloader.INSTANCE.getMailConfig1(event.getChannel().getName()).equals(null)) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/close:nochannel")).queue();
			return;
		}
		String cname = event.getChannel().getName();
		event.getTextChannel().delete().queue();
		Bot.INSTANCE.jda.getUserById(Configloader.INSTANCE.getMailConfig1(cname)).openPrivateChannel().complete().sendMessageEmbeds(
				AnswerEngine.getInstance().fetchMessage(guild, Bot.INSTANCE.jda.getUserById(Configloader.INSTANCE.getMailConfig1(cname)), "/commands/moderation/close:closed")).queue();
		Configloader.INSTANCE.removeMailConfig(cname);
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("close", "Closes the modmail/support channel");
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/moderation/close:help");
	}
}