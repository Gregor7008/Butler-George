package slash_commands.music;

import base.engines.LanguageEngine;
import base.engines.Toolbox;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import slash_commands.assets.SlashCommandEventHandler;
import slash_commands.engines.GuildMusicManager;
import slash_commands.engines.PlayerManager;

public class Play implements SlashCommandEventHandler {

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final Member member = event.getMember();
		final TextChannel channel = guild.getTextChannelById(event.getMessageChannel().getIdLong());
		final String argument = event.getOption("title").getAsString();
		final Member self = guild.getSelfMember();
		final User user = event.getUser();
		final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);
		if (argument == null) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "wrongusage")).queue();
			return;
		}
		if (!member.getVoiceState().inAudioChannel()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "noVCdefined")).queue();
			return;
		}
		if (self.getVoiceState().inAudioChannel()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "alreadyinuse")).queue();
			return;
		}
		this.load(event, argument, musicManager, channel, member);
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("play", "Adds a new track to your music queue!")
									  .addOptions(new OptionData(OptionType.STRING, "title", "Hand over the title or the direct URL of your track!", true));
		command.setDefaultPermissions(DefaultMemberPermissions.ENABLED)
		   .setGuildOnly(true);
		return command;
	}
	
	private void load(SlashCommandInteractionEvent event, String argument, GuildMusicManager musicManager, TextChannel channel, Member member) {
		VoiceChannel vc = (VoiceChannel) member.getVoiceState().getChannel();
		channel.getGuild().getAudioManager().openAudioConnection(vc);
		if (!event.getGuild().getAudioManager().isConnected()) {
			if (vc.getUserLimit() != 0) {
				vc.getManager().setUserLimit(vc.getUserLimit() + 1).queue();
			}
		}
		if (!Toolbox.checkURL(argument)) {
			String term = "ytsearch:" + argument;
			musicManager.scheduler.player.setVolume(15);
			PlayerManager.getInstance().loadAndPlay(event, term);
		} else {
			musicManager.scheduler.player.setVolume(15);
			PlayerManager.getInstance().loadAndPlay(event, argument);
		}
	}

	@Override
	public boolean checkBotPermissions(SlashCommandInteractionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAvailableTo(Member member) {
		// TODO Auto-generated method stub
		return false;
	}
}