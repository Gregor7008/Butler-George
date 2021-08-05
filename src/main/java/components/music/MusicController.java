package components.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import base.Bot;
import net.dv8tion.jda.api.entities.Guild;

public class MusicController {
	
	private Guild guild;
	private AudioPlayer player;
		
	public MusicController(Guild guild) {
		this.guild = guild;
		this.player = Bot.INSTANCE.apm.createPlayer();
			
		this.guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(player));
		this.player.setVolume(20);
	}
	
	public Guild getGuild() {
		return guild;
	}
	
	public AudioPlayer getPlayer() {
		return player;
	}
}
