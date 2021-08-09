package components;

import java.time.OffsetDateTime;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class LevelEngine {
	
	public LevelEngine(GuildMessageReceivedEvent event) {
		Member member = event.getMember();
		OffsetDateTime time = event.getMessage().getTimeCreated();
		this.givexp(member, time);
	}

	public LevelEngine(GuildVoiceJoinEvent event) {
		Member member = event.getMember();
		OffsetDateTime time = java.time.OffsetDateTime.now();
		this.givexp(member, time);
	}
	
	private void givexp(Member member, OffsetDateTime time) {
		// TODO Auto-generated method stub
		
	}
}
