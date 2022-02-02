package commands.moderation;

import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Mute implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getOption("member").getAsUser();
		if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, event.getUser(),"/commands/moderation/mute:nopermission")).queue();
			return;
		}
		this.mute(guild, user);
		event.replyEmbeds(AnswerEngine.getInstance().buildMessage(
				AnswerEngine.getInstance().getTitle(guild, user, "/commands/moderation/mute:success"),
				AnswerEngine.getInstance().getDescription(guild, user, "/commands/moderation/mute:success").replace("{user}", user.getName()))).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("mute", "Mute a user permanently")
				.addOptions(new OptionData(OptionType.USER, "member", "The member you want to mute").setRequired(true));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/moderation/mute:help");
	}
	
	public void mute(Guild guild, User user) {
		Configloader.INSTANCE.setUserConfig(guild, user, "tempmuted", "false");
		Configloader.INSTANCE.setUserConfig(guild, user, "muted", "true");
	}
}