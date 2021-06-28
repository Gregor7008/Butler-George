package functions;

import main.Answer;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class rolecheck {

	public rolecheck(GuildMessageReceivedEvent event, Role mentionedRole, Member member) {
		if (mentionedRole == null) {
			new Answer("Incomplete command", ":exclamation: | Please mention a role I should check the user for! \n Error code: 004", event, false);
			this.wait(3200);
		} else {
		if (hasRole(member, mentionedRole)==true) {
			new Answer("Role found!", ":white_check_mark: | The user has the role " + mentionedRole.getAsMention(), event, false);
		} else {
			new Answer("Role not found!", ":x: | The user doesn't have the role " + mentionedRole.getAsMention(), event, false);
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