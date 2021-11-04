package commands.moderation;

import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Mute implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/command/moderation/mute:nopermission")).queue();
			return;
		}
		User user = event.getOption("member").getAsUser();
		this.mute(event.getGuild().getMember(user));
		event.replyEmbeds(AnswerEngine.getInstance().buildMessage("Success!", ":white_check_mark: | The member\s" + user.getName() + "\swas successfully muted permanently!"));
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("mute", "Mute a user permanently")
				.addOptions(new OptionData(OptionType.USER, "member", "The member you want to mute").setRequired(true));
		return command;
	}

	@Override
	public String getHelp() {
		return "Use this command to permanently mute a user on this guild";
	}
	
	public void mute(Member member) {
		Configloader.INSTANCE.setUserConfig(member, "tempmuted", "false");
		Configloader.INSTANCE.setUserConfig(member, "muted", "true");
	}
}
