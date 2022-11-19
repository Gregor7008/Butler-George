package functions.guild_utilities;

import java.util.List;

import assets.functions.GuildUtilities;
import base.Bot;
import base.Bot.ShutdownReason;
import engines.base.Toolbox;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;

public class FunAndGames_Utilities extends GuildUtilities {
	
	private static final long GUILD_ID = 708381749826289666L;
	
	private Guild guild;
	
	@Override
	public long getGuildId() {
		return GUILD_ID;
	}
	
	@Override
	public void onStartup() {
		guild = Bot.INSTANCE.jda.getGuildById(GUILD_ID);
		if (guild != null) {
			this.openGuildChannel(guild.getVoiceChannelById(938425419860959242L));
			
			this.closeGuildChannel(guild.getVoiceChannelById(1008793448154923048L));
			this.closeGuildChannel(guild.getVoiceChannelById(1008793481843593308L));
		}
	}
	
	@Override
	public void onShutdown(ShutdownReason reason) {
		if (guild != null) {
			this.closeGuildChannel(guild.getVoiceChannelById(938425419860959242L));
			
			this.openGuildChannel(guild.getVoiceChannelById(1008793448154923048L));
			this.openGuildChannel(guild.getVoiceChannelById(1008793481843593308L));
		}
	}
	
	@Override
	public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
		this.rolecheck(event.getGuild());
	}
	
	@Override
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
		this.rolecheck(event.getGuild());
	}
	
	@Override
	public void onRoleDelete(RoleDeleteEvent event) {
		this.rolecheck(event.getGuild());
	}
	
	private void openGuildChannel(StandardGuildChannel channel) {
		if (channel != null) {
		    channel.upsertPermissionOverride(guild.getPublicRole()).grant(Permission.VIEW_CHANNEL).queue();
		}
	}
	
	private void closeGuildChannel(StandardGuildChannel channel) {
	    if (channel != null) {
	        channel.upsertPermissionOverride(guild.getPublicRole()).deny(Permission.VIEW_CHANNEL).queue();
	    }
	}
	
	private void rolecheck(Guild eventGuild) {
		if (eventGuild != null && eventGuild.getIdLong() == GUILD_ID) {
			try {
			    final List<Member> members = guild.getMembers();
	            Role gr1 = guild.getRoleById("837742608604332052");
	            Role gr2 = guild.getRoleById("837744376712265728");
	            Role gr3 = guild.getRoleById("870231144300441671");
	            int gr1p = gr1.getPosition();
	            int gr2p = gr2.getPosition();
	            int gr3p = gr3.getPosition();
	            List<Role> sr1 = guild.getRoles().stream().filter(e -> e.getPosition() < gr1p && e.getPosition() > gr2p).toList();
	            List<Role> sr2 = guild.getRoles().stream().filter(e -> e.getPosition() < gr2p && e.getPosition() > gr3p).toList();
	            List<Role> sr3 = guild.getRoles().stream().filter(e -> e.getPosition() < gr3p && e.getPosition() >= guild.getRoleById("864136501653798932").getPosition()).toList();
	            for (int i = 0; i < members.size(); i++) {
	                Member member = members.get(i);
	                if (!member.getUser().isBot()) {
	                    Toolbox.sortRoles(guild, member, sr1, gr1);
	                    Toolbox.sortRoles(guild, member, sr2, gr2);
	                    Toolbox.sortRoles(guild, member, sr3, gr3);
	                }
	            }
			} catch (NullPointerException e) {
			    LOG.error("NullPointerException in GuildUtilities for \"Fun & Games\" Probably as of it having deleted a role included in the automatic role sorting!");
			}
		}
	}
}