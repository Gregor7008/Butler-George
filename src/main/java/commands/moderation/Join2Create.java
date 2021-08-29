package commands.moderation;

import commands.Command;
import components.Developerlist;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Join2Create implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		final Member member = event.getMember();
		if(!member.hasPermission(Permission.MANAGE_CHANNEL) && !Developerlist.getInstance().developers.contains(event.getMember().getId())) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/join2create:nopermission")).queue();
			return;
		}
		Configloader.INSTANCE.addGuildConfig(event.getGuild(), "join2create", event.getOption("channel").getAsGuildChannel().getId());
		event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/join2create:success!")).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("join2create", "Set the join2create channel of the server!").addOptions(new OptionData(OptionType.CHANNEL, "channel", "Enter the voicechannel that should be used as the join2create channel!").setRequired(true));
		return command;
	}

	@Override
	public String getHelp() {
		return "This command is used to set the Join2Create channel, with which members can create their own private voicechannel, by simply joining!";
	}

}
