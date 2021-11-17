package commands.moderation;

import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class Move implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (Configloader.INSTANCE.getGuildConfig(guild, "supportrole").equals("")) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/command/moderation/move:norole")).queue();
			return;
		}
		if (!event.getMember().getRoles().contains(guild.getRoleById(Configloader.INSTANCE.getGuildConfig(guild, "supportrole")))) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/move:nopermission")).queue();
			return;
		}
		if (Configloader.INSTANCE.getGuildConfig(guild, "supporttalk").equals("")) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/move:nochannel")).queue();
			return;
		}
		if (!guild.getMember(event.getOption("member").getAsUser()).getVoiceState().inVoiceChannel()) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/move:memnotconn")).queue();
			return;
		}
		VoiceChannel st = guild.getVoiceChannelById(Configloader.INSTANCE.getGuildConfig(guild, "supporttalk"));
		if (st.getMembers().contains(event.getMember())) {
			guild.moveVoiceMember(guild.getMember(event.getOption("member").getAsUser()), st).queue();
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/move:success")).queue();
		} else {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/move:notconnected")).queue();
		}
		
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("move", "Move a member into the support talk").addOption(OptionType.USER, "member", "The member you want to move");
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/moderation/move:help");
	}

}