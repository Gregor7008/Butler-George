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
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Join2Create implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		final String id = event.getOption("channel").getAsGuildChannel().getId();
		if(!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user,"/commands/moderation/join2create:nopermission")).queue();
			return;
		}
		if (guild.getVoiceChannelById(id) == null) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/join2create:invalid")).queue();
			return;
		}
		if (event.getSubcommandName().equals("add")) {
			if (Configloader.INSTANCE.getGuildConfig(guild, "join2create").contains(id)) {
				event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/join2create:adderror")).queue();
			} else {
				Configloader.INSTANCE.addGuildConfig(guild, "join2create", id);
				event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/join2create:addsuccess")).queue();
			}
		}
		if (event.getSubcommandName().equals("remove")) {
			if (Configloader.INSTANCE.getGuildConfig(guild, "join2create").contains(id)) {
				Configloader.INSTANCE.deleteGuildConfig(guild, "join2create", id);
				event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/join2create:remsuccess")).queue();
			} else {
				event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/join2create:remerror")).queue();
			}
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("join2create", "Edit the join2create channels of the server!")
										.addSubcommands(new SubcommandData("add", "Add a new join2create channel")
												.addOptions(new OptionData(OptionType.CHANNEL, "channel", "Enter the voicechannel that should be used as the join2create channel!", true)))
										.addSubcommands(new SubcommandData("remove", "Remove a join2create channel")
												.addOptions(new OptionData(OptionType.CHANNEL, "channel", "Enter the voicechannel that should be used as the join2create channel!", true)));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/moderation/join2create:help");
	}
}