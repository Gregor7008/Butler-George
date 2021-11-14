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
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Ignorechannel implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		if(!event.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(),"/commands/moderation/ignorechannel:nopermission")).queue();
			return;
		}
		switch (event.getSubcommandName()) {
		case "add":
			Configloader.INSTANCE.addGuildConfig(event.getGuild(), "ignored", event.getOption("channel").getAsGuildChannel().getId());
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(), "/commands/moderation/ignorechannel:successadd")).queue();
			break;
		case "list":
			this.listignoredchannels(event);
			break;
		case "remove":
			Configloader.INSTANCE.deleteGuildConfig(event.getGuild(), "ignored", event.getOption("channel").getAsGuildChannel().getId());
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(), "/commands/moderation/ignorechannel:successremove")).queue();
			break;
		default:
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(), "/commands/moderation/ignorechannel:error")).queue();
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("ignorechannel", "Tells the bot to ignore a channel")
										.addSubcommands(new SubcommandData("add", "Adds a new ignored channel")
												.addOption(OptionType.CHANNEL, "channel", "Mention a channel", true))
										.addSubcommands(new SubcommandData("list", "Lists all currently ignored channels"))
										.addSubcommands(new SubcommandData("remove", "Removes a channel from the \"ignored\" list")
												.addOption(OptionType.CHANNEL, "channel", "Mention a channel", true));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/moderation/ignorechannel:help");
	}
	
	private void listignoredchannels(SlashCommandEvent event) {
		String channelids = Configloader.INSTANCE.getGuildConfig(event.getGuild(), "ignored");
		if (channelids.equals("")) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(event.getGuild(), event.getUser(), "/commands/moderation/ignorechannel:nochannels")).queue();
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(AnswerEngine.getInstance().getDescription(event.getGuild(), event.getUser(), "/commands/moderation/ignorechannel:list"));
		String[] channelid = channelids.split(";");
		for (int i = 0; i < channelid.length; i++) {
			sb.append("#" + String.valueOf(i + 1) + " ");
			sb.append(event.getGuild().getTextChannelById(channelid[i]).getAsMention());
			if (i+1 != channelid.length) {
				sb.append("\n");
			}
		}
		String title = AnswerEngine.getInstance().getTitle(event.getGuild(), event.getUser(), "/commands/moderation/ignorechannel:list");
		event.replyEmbeds(AnswerEngine.getInstance().buildMessage(title, sb.toString())).queue();
	}
}
