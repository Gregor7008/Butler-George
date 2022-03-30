package commands.moderation;

import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class IgnoreChannel implements Command{
	
	private Guild guild;
	private User user;

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		guild = event.getGuild();
		user = event.getUser();
		if(!event.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user,"/commands/moderation/ignorechannel:nopermission").convert()).queue();
			return;
		}
		switch (event.getSubcommandName()) {
		case "add":
			Configloader.INSTANCE.addGuildConfig(guild, "ignored", event.getOption("channel").getAsGuildChannel().getId());
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/ignorechannel:successadd").convert()).queue();
			break;
		case "list":
			this.listignoredchannels(event);
			break;
		case "remove":
			Configloader.INSTANCE.deleteGuildConfig(guild, "ignored", event.getOption("channel").getAsGuildChannel().getId());
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/ignorechannel:successremove").convert()).queue();
			break;
		default:
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "general:fatal").convert()).queue();
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("ignorechannel", "0")
										.addSubcommands(new SubcommandData("add", "Adds a channel to the \"ignored\"-list")
												.addOption(OptionType.CHANNEL, "channel", "Mention the channel", true))
										.addSubcommands(new SubcommandData("list", "Lists all currently ignored channels"))
										.addSubcommands(new SubcommandData("remove", "Removes a channel from the \"ignored\"-list")
												.addOption(OptionType.CHANNEL, "channel", "Mention the channel", true));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/moderation/ignorechannel:help");
	}
	
	private void listignoredchannels(SlashCommandInteractionEvent event) {
		String channelids = Configloader.INSTANCE.getGuildConfig(guild, "ignored");
		if (channelids.equals("")) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/ignorechannel:nochannels").convert()).queue();
			return;
		}
		StringBuilder sb = new StringBuilder();
		String[] channelid = channelids.split(";");
		for (int i = 0; i < channelid.length; i++) {
			sb.append("#" + String.valueOf(i + 1) + " ");
			sb.append(guild.getTextChannelById(channelid[i]).getAsMention());
			if (i+1 != channelid.length) {
				sb.append("\n");
			}
		}
		event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/ignorechannel:list").replaceDescription("{list}", sb.toString()).convert()).queue();
	}
}