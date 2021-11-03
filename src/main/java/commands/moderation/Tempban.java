package commands.moderation;

import java.time.OffsetDateTime;

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

public class Tempban implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		if (!event.getMember().hasPermission(Permission.BAN_MEMBERS)) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/tempban:nopermission")).queue();
			return;
		}
		User user = event.getOption("member").getAsUser();
		this.tempban(Integer.parseInt(event.getOption("days").getAsString()), event.getGuild().getMember(user));
		event.replyEmbeds(AnswerEngine.getInstance().buildMessage("Success!", ":white_check_mark: | The member\s" + user.getName() + "\swas successfully banned for\s" + event.getOption("days").getAsString() + "\sdays!"));
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("tempban", "Ban a user temporary")
												.addOptions(new OptionData(OptionType.USER, "member", "The member you want to ban").setRequired(true))
												.addOptions(new OptionData(OptionType.INTEGER, "days", "The number of days you want the member to be banned").setRequired(true));
		return command;
	}

	@Override
	public String getHelp() {
		return "Use this command to temporary ban a user from this guild";
	}
	
	public void tempban(int days, Member member) {
		OffsetDateTime until = OffsetDateTime.now().plusDays(Long.parseLong(String.valueOf(days)));
		Configloader.INSTANCE.setUserConfig(member, "tbuntil", until.toString());
		Configloader.INSTANCE.setUserConfig(member, "tempbanned", "true");
		member.ban(0).queue();
	}	

}
