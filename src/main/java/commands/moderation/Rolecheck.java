package commands.moderation;

import base.Commands;
import components.Answer;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Rolecheck implements Commands{

	@Override
	public void perform(GuildMessageReceivedEvent event, String arguments) {
		Member member;
		try {member = event.getMessage().getMentionedMembers().get(0);
		} catch (Exception e) {member = event.getMember();}
		Role mentionedRole = event.getMessage().getMentionedRoles().get(0);
		if (mentionedRole == null) {
			new Answer("/commands/moderation/rolecheck:incomplete", event);
			this.wait(3200);
		} else {
		if (hasRole(member, mentionedRole)==true) {
			new Answer("/commands/moderation/rolecheck:found", event);
		} else {
			new Answer("/commands/moderation/rolecheck:notfound", event);
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