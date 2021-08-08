package textcommands.moderation;

import components.AnswerEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class Rolecheck{

	public void perform(Guild guild, Member member, TextChannel channel, Role mentionedRole, String argument) {
		if (mentionedRole == null) {
			AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolecheck:incomplete", guild, member, channel).queue();
			this.wait(3200);
		} else {
		if (hasRole(member, mentionedRole)==true) {
			AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolecheck:found", guild, member, channel).queue();
		} else {
			AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolecheck:notfound", guild, member, channel).queue();
		}
		}
	}
		
	private boolean hasRole(Member member, Role role) {
		return member.getRoles().contains(role);
	}
	
	private void wait(int time) {
		try { Thread.sleep(time);
        } catch (InterruptedException e){}
	}
}