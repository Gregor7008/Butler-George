package functions.guild_utilities;

import assets.functions.GuildUtilities;
import base.Bot;
import base.Bot.ShutdownReason;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

public class FunAndGames_Utilities extends GuildUtilities {
	
	private static final long GUILD_ID = 708381749826289666L;
	
	private Guild guild;
	
	@Override
	public long getGuildId() {
		return GUILD_ID;
	}
	
	@Override
	public void onStartup() {
		guild = Bot.getAPI().getGuildById(GUILD_ID);
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
	
	private void openGuildChannel(VoiceChannel channel) {
		if (channel != null) {
		    channel.upsertPermissionOverride(guild.getPublicRole()).grant(Permission.VIEW_CHANNEL).queue();
		}
	}
	
	private void closeGuildChannel(VoiceChannel channel) {
	    if (channel != null) {
	        channel.upsertPermissionOverride(guild.getPublicRole()).deny(Permission.VIEW_CHANNEL).queue();
	    }
	}
}