package components.utilities;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

import base.Bot;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class LevelEngine {
	
private static LevelEngine INSTANCE;
	
	public static LevelEngine getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new LevelEngine();
		}
		return INSTANCE;
	}
	
	public void messagereceived(GuildMessageReceivedEvent event) {
		Member member = event.getMember();
		if (event.getAuthor().isBot()) {
			return;
		}
		OffsetDateTime time = event.getMessage().getTimeCreated();
		this.givexp(member, time, 10, 30, event.getChannel());
	}
	
	public void slashcommand(SlashCommandEvent event) {
		if (event.getUser().isBot()) {
			return;
		}
		Member member = event.getMember();
		OffsetDateTime time = java.time.OffsetDateTime.now();
		this.givexp(member, time, 20, 30, event.getTextChannel());
	}

	public void voicejoin(GuildVoiceJoinEvent event) {
		if (event.getMember().getUser().isBot()) {
			return;
		}
		Member member = event.getMember();
		OffsetDateTime time = java.time.OffsetDateTime.now();
		TextChannel channel = null;
		List<TextChannel> channels = event.getGuild().getTextChannels();
		for (int i = 0; i < channels.size(); i++) {
			if (member.hasPermission(channels.get(i), Permission.MESSAGE_WRITE)) {
				channel = channels.get(i);
				i = i + 2 + channels.size();
			}
		}
		this.givexp(member, time, 50, 600, channel);
	}
	
	private void givexp(Member member, OffsetDateTime time, int amount, int mindiff, TextChannel channel) {
		OffsetDateTime now = OffsetDateTime.now();
		OffsetDateTime lastxpgotten = OffsetDateTime.parse(Configloader.INSTANCE.getUserConfig(member.getGuild(), member.getUser(), "lastxpgotten"));
		int difference = Duration.between(lastxpgotten, now).toSecondsPart();
		if(difference >= mindiff) {
			this.grantxp(member, amount, channel);
		}
	}

	private void grantxp(Member member, int amount, TextChannel channel) {
		try {
		int current = Integer.parseInt(Configloader.INSTANCE.getUserConfig(member.getGuild(), member.getUser(), "expe"));
		int newamount = current + amount;
		Configloader.INSTANCE.setUserConfig(member, "expe", String.valueOf(newamount));
		this.checklevel(member, channel);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Something went horribly wrong! Shutting down bot!");
			Bot.INSTANCE.shutdown();
		}
	}

	private void checklevel(Member member, TextChannel channel) {
		int currentlevel = Integer.valueOf(Configloader.INSTANCE.getUserConfig(member.getGuild(), member.getUser(), "level"));
		int currentxp = Integer.valueOf(Configloader.INSTANCE.getUserConfig(member.getGuild(), member.getUser(), "expe"));
		int xpneededfornextlevel = this.xpfornextlevel(member);
		if (currentxp > xpneededfornextlevel) {
			Configloader.INSTANCE.setUserConfig(member, "level", String.valueOf(currentlevel + 1));
			channel.sendMessageEmbeds(AnswerEngine.getInstance().buildMessage(":confetti_ball: Congrats\s" + member.getEffectiveName() + "\s! :confetti_ball:", "You just reached level\s" + String.valueOf(currentlevel+1) + "!")).queue();
		}
		this.checkforreward(member);
	}

	private void checkforreward(Member member) {
		String rawinput = Configloader.INSTANCE.getGuildConfig(member.getGuild(), "levelrewards");
		if (rawinput.equals("")) {
			return;
		}
		String[] rewards = rawinput.split(";");
		for (int i = 0; i < rewards.length; i++) {
			String[] reward = rewards[i].split("_");
			if (Integer.parseInt(Configloader.INSTANCE.getUserConfig(member.getGuild(), member.getUser(), "level")) >= Integer.parseInt(reward[1])) {
				Role rewardrole = member.getGuild().getRoleById(reward[0]);
				member.getGuild().addRoleToMember(member, rewardrole);
			}
		}
	}
	
	public int xpfornextlevel(Member member) {
		int currentlevel = Integer.parseInt(Configloader.INSTANCE.getUserConfig(member.getGuild(), member.getUser(), "level"));
		int xpperlevel = 100;
		int xpneededfornextlevel = (((currentlevel+1)^2 + (currentlevel+1)) / 2) * xpperlevel;
		return 100000000;
	}
}
