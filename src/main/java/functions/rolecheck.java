package functions;

import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class rolecheck {

	public rolecheck(GuildMessageReceivedEvent event, Role mentionedRole) {
		if (mentionedRole == null) {
			event.getChannel().sendMessage("Incomplete command (add a role)!").queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));
			this.wait(3200);
		} else {
		Member member = event.getMessage().getMember();
		if (hasRole(event, member, mentionedRole)==true) {
			TextChannel logs = event.getGuild().getTextChannelsByName("logs", true).get(0);
			logs.sendMessage("Role found").queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));
		} else {
			TextChannel logs = event.getGuild().getTextChannelsByName("logs", true).get(0);
			logs.sendMessage("Role not found").queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));
		}
		}
	}
	
	public rolecheck(GuildMessageReceivedEvent event, Role mentionedRole, Member member) {
		if (mentionedRole == null) {
			event.getChannel().sendMessage("Incomplete command (add a role)!").queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));
			this.wait(3200);
		} else {
		if (hasRole(event, member, mentionedRole)==true) {
			TextChannel logs = event.getGuild().getTextChannelsByName("logs", true).get(0);
			logs.sendMessage("Role found on " + member.getAsMention()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));
		} else {
			TextChannel logs = event.getGuild().getTextChannelsByName("logs", true).get(0);
			logs.sendMessage("Role not found on " + member.getAsMention()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));
		}
		}
	}
	
	public boolean hasRole(GuildMessageReceivedEvent event, Member member, Role role) {
		return member.getRoles().contains(role);
	}
	
	private void wait(int time) {
		try { Thread.sleep(time);
        } catch (InterruptedException e){}
	}
}