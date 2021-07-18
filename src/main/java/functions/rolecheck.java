package functions;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import tools.Answer;

public class rolecheck {

	public rolecheck(GuildMessageReceivedEvent event, Member member) {
		Role mentionedRole = event.getMessage().getMentionedRoles().get(0);
		if (mentionedRole == null) {
			new Answer("Incomplete command", ":exclamation: | Please mention a role I should check the user for! \n ----> Error code: 004", event.getChannel(), false);
			this.wait(3200);
		} else {
		if (hasRole(member, mentionedRole)==true) {
			new Answer("Role found!", ":white_check_mark: | The user has the role " + mentionedRole.getAsMention(), event.getChannel(), false);
		} else {
			new Answer("Role not found!", ":x: | The user doesn't have the role " + mentionedRole.getAsMention(), event.getChannel(), false);
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