package commands.moderation;

import components.AnswerEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class Rolecheck{

	public Rolecheck(Guild guild, Member member, TextChannel channel, Role mentionedRole) {
		if (member == null) {
			channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolecheck:nomember", guild, member)).queue();
			return;
		}
		if (mentionedRole == null) {
			channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolecheck:incomplete", guild, member)).queue();
			return;
		}
		if (hasRole(member, mentionedRole)==true) {
			channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolecheck:found", guild, member)).queue();
		} else {
			channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolecheck:notfound", guild, member)).queue();
		}
	}
		
	private boolean hasRole(Member member, Role role) {
		return member.getRoles().contains(role);
	}
}