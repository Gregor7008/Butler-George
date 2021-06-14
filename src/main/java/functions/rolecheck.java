package functions;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class rolecheck {

	public rolecheck(GuildMessageReceivedEvent event, String object) {
		if (object == "none") {
			event.getChannel().sendMessage("Incomplete command (add a role)!").queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));
			this.wait(3200);
		} else {
		Member member = event.getMessage().getMember();
		if (hasRole(event, member, object)==true) {
			TextChannel logs = event.getGuild().getTextChannelsByName("logs", true).get(0);
			logs.sendMessage("Role found").queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));
		} else {
			TextChannel logs = event.getGuild().getTextChannelsByName("logs", true).get(0);
			logs.sendMessage("Role not found").queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));
		}
		}
	}
	
	public rolecheck(GuildMessageReceivedEvent event, String object, Member member ) {
		if (object == "none") {
			event.getChannel().sendMessage("Incomplete command (add a role)!").queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));
			this.wait(3200);
		} else {
		if (hasRole(event, member, object)==true) {
			TextChannel logs = event.getGuild().getTextChannelsByName("logs", true).get(0);
			logs.sendMessage("Role found on " + member.getAsMention()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));
		} else {
			TextChannel logs = event.getGuild().getTextChannelsByName("logs", true).get(0);
			logs.sendMessage("Role not found on " + member.getAsMention()).queue(response -> response.delete().queueAfter(3, TimeUnit.SECONDS));
		}
		}
	}
	
	public boolean hasRole(GuildMessageReceivedEvent event, Member member, String term) {
		Role temp1;
		String temp2;
		Role role = event.getGuild().getRolesByName(term, true).get(0);
		try {
			temp1 = member.getRoles().stream().filter(roles -> roles.getName().equalsIgnoreCase(role.getName())).collect(Collectors.toList()).get(0);
			temp2 = temp1.getName();
		} catch (Exception e) {
			temp2 = "CryptonicThingsHere";
		}
		if (temp2==role.getName()) {
			return (true);
		} else {
			return (false);
		}
	}
	
	private void wait(int time) {
		try { Thread.sleep(time);
        } catch (InterruptedException e){}
	}
}