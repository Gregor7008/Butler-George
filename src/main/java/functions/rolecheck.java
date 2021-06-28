package functions;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class rolecheck {

	public rolecheck(GuildMessageReceivedEvent event, Role mentionedRole) {
		if (mentionedRole == null) {
			event.getChannel().sendMessage("Incomplete command (add a role)!").queue();
			this.wait(3200);
		} else {
		Member member = event.getMessage().getMember();
		if (hasRole(member, mentionedRole)==true) {
			TextChannel channel = event.getChannel();
			channel.sendMessage("Role found").queue();
		} else {
			TextChannel channel = event.getChannel();
			channel.sendMessage("Role not found").queue();
		}
		}
	}
	
	public rolecheck(GuildMessageReceivedEvent event, Role mentionedRole, Member member) {
		if (mentionedRole == null) {
			event.getChannel().sendMessage("Incomplete command (add a role)!").queue();
			this.wait(3200);
		} else {
		if (hasRole(member, mentionedRole)==true) {
			TextChannel channel = event.getChannel();
			channel.sendMessage("Role found on " + member.getAsMention()).queue();
		} else {
			TextChannel channel = event.getChannel();
			channel.sendMessage("Role not found on " + member.getAsMention()).queue();
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