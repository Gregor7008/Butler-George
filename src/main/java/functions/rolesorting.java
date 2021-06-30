package functions;

import java.util.Arrays;
import java.util.List;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class rolesorting {
	
	Role grouping1, grouping2;
	List<Role> subroles1, subroles2;
	Member member;

	public rolesorting(GuildMessageReceivedEvent event, Member tocheck) {
		grouping1 = event.getGuild().getRolesByName("᲼᲼᲼᲼᲼᲼᲼᲼᲼᲼᲼᲼Gruppen᲼᲼᲼᲼᲼᲼᲼᲼᲼", true).get(0);
		grouping2 = event.getGuild().getRolesByName("᲼᲼᲼᲼᲼᲼᲼᲼᲼᲼᲼᲼᲼Abos᲼᲼᲼᲼᲼᲼᲼᲼᲼᲼᲼᲼᲼", true).get(0);
		subroles1 = Arrays.asList(event.getGuild().getRolesByName("Freunde", true).get(0),
								  event.getGuild().getRolesByName("Bewerber", true).get(0),
								  event.getGuild().getRolesByName("MC-Server", true).get(0));
		subroles2 = event.getGuild().getRoles().subList(event.getGuild().getRoles().size() - event.getGuild().getRolesByName("Minecraft", true).get(0).getPosition() - 2,
				  										event.getGuild().getRoles().size() - event.getGuild().getRolesByName("Valorant", true).get(0).getPosition() - 1);
		member = tocheck;
		rolesorter(event);
	}
	
	private void rolesorter(GuildMessageReceivedEvent event) {
		int size = member.getRoles().size();
		int match1 = 0;
		int match2 = 0;
		for (int i = 1; i < size; i++) {
			if (subroles1.contains(member.getRoles().get(i))) {
				event.getGuild().addRoleToMember(member, grouping1).queue();
				match1++;
			}
			if (subroles2.contains(member.getRoles().get(i))) {
				event.getGuild().addRoleToMember(member, grouping2).queue();
				match2++;	
			}
		}
		if (match1 == 0 && member.getRoles().contains(grouping1)) {
			event.getGuild().removeRoleFromMember(member, grouping1).queue();
		}
		if (match2 == 0 && member.getRoles().contains(grouping2)) {
			event.getGuild().removeRoleFromMember(member, grouping2).queue();
		}
	}
}
