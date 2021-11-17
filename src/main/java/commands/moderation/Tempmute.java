package commands.moderation;

import java.time.OffsetDateTime;

import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Tempmute implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getOption("member").getAsUser();
		if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, event.getUser(),"/commands/moderation/tempmute:nopermission")).queue();
			return;
		}
		this.tempmute(Integer.parseInt(event.getOption("days").getAsString()), guild.retrieveMember(user).complete());
		event.replyEmbeds(AnswerEngine.getInstance().buildMessage(
					AnswerEngine.getInstance().getTitle(guild, user, "/commands/moderation/tempmute:success"),
					AnswerEngine.getInstance().getDescription(guild, user, "/commands/moderation/tempmute:success").replace("{user}", user.getName()).replace("{time}", event.getOption("days").getAsString()))).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("tempmute", "Mute a user temporary")
				.addOptions(new OptionData(OptionType.USER, "member", "The member you want to mute").setRequired(true))
				.addOptions(new OptionData(OptionType.INTEGER, "days", "The number of days you want the member to be muted").setRequired(true));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/moderation/tempmute:help");
	}
	
	public void tempmute(int days, Member member) {
		OffsetDateTime until = OffsetDateTime.now().plusDays(Long.parseLong(String.valueOf(days)));
		Configloader.INSTANCE.setUserConfig(member, "tmuntil", until.toString());
		Configloader.INSTANCE.setUserConfig(member, "tempmuted", "true");
		Configloader.INSTANCE.setUserConfig(member, "muted", "true");
	}
}