package commands.moderation;

import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Setsuggestionchannel implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		if (event.getOption("channel") != null) {
			if (!event.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
				event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/setsuggestionchannel:nopermission")).queue();
			} else {
				Configloader.INSTANCE.setGuildConfig(event.getGuild(), "suggest", event.getOption("channel").getAsGuildChannel().getId());
				event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/setsuggestionchannel:successset")).queue();
			}
		} else {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/setsuggestionchannel:noargs")).queue();
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("setsuggestionchannel", "Set a suggestion channel for your server!")
										.addOptions(new OptionData(OptionType.CHANNEL, "channel", "Mention the channel that should be used", true));	
		return command;
	}

	@Override
	public String getHelp() {
		return "This command is used to set a suggestion channel for your server";
	}

}
