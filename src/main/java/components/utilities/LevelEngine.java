package components.utilities;

import java.time.Duration;
import java.time.OffsetDateTime;

import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
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
		OffsetDateTime time = event.getMessage().getTimeCreated();
		this.givexp(member, time, 10, 30);
	}
	
	public void slashcommand(SlashCommandEvent event) {
		Member member = event.getMember();
		OffsetDateTime time = java.time.OffsetDateTime.now();
		this.givexp(member, time, 20, 30);
	}

	public void voicejoin(GuildVoiceJoinEvent event) {
		Member member = event.getMember();
		OffsetDateTime time = java.time.OffsetDateTime.now();
		this.givexp(member, time, 50, 600);
	}
	
	public void voicemove(GuildVoiceMoveEvent event) {
		Member member = event.getMember();
		OffsetDateTime time = java.time.OffsetDateTime.now();
		this.givexp(member, time, 50, 600);
	}
	
	private void givexp(Member member, OffsetDateTime time, int amount, int mindiff) {
		OffsetDateTime now = OffsetDateTime.now();
		OffsetDateTime lastxpgotten = OffsetDateTime.parse(Configloader.INSTANCE.getUserConfig(member.getGuild(), member.getUser(), "lastxpgotten"));
		long difference = Duration.between(lastxpgotten, now).toSeconds();
		if(difference >= Long.parseLong(String.valueOf(mindiff))) {
			Configloader.INSTANCE.setUserConfig(member, "levelspamcount", "0");
			this.grantxp(member, amount);
			this.checklevel(member);
			this.checkforreward(member);
		} else {
			int newcount = Integer.parseInt(Configloader.INSTANCE.getUserConfig(member.getGuild(), member.getUser(), "levelspamcount")) + 1;
			Configloader.INSTANCE.setUserConfig(member, "levelspamcount", String.valueOf(newcount));
			if (newcount > 20) {
				Configloader.INSTANCE.addUserConfig(member, "warnings", "Levelspamming");
				member.getUser().openPrivateChannel().complete()
						.sendMessageEmbeds(AnswerEngine.getInstance().buildMessage("Warning!", ":warning: | You have been warned on the " + member.getGuild().getName() + " server for spamming to level up faster!")).queue();
			}
		}
	}

	private void grantxp(Member member, int amount) {
		int current = Integer.parseInt(Configloader.INSTANCE.getUserConfig(member.getGuild(), member.getUser(), "expe"));
		int newamount = current + amount;
		Configloader.INSTANCE.setUserConfig(member, "expe", String.valueOf(newamount));
		Configloader.INSTANCE.setUserConfig(member, "lastxpgotten", OffsetDateTime.now().toString());
	}

	private void checklevel(Member member) {
		int currentlevel = Integer.valueOf(Configloader.INSTANCE.getUserConfig(member.getGuild(), member.getUser(), "level"));
		if (this.xpleftfornextlevel(member) < 1) {
			Configloader.INSTANCE.setUserConfig(member, "level", String.valueOf(currentlevel + 1));
			TextChannel channel = member.getGuild().getTextChannelById(Configloader.INSTANCE.getGuildConfig(member.getGuild(), "levelmsgch"));
			if (channel != null) {
				channel.sendMessageEmbeds(AnswerEngine.getInstance().buildMessage(":confetti_ball: Congrats\s" + member.getEffectiveName() + "\s! :confetti_ball:", "You just reached level\s" + String.valueOf(currentlevel+1) + "!")).queue();
			} else {
				Configloader.INSTANCE.setGuildConfig(member.getGuild(), "levelmsgch", "");
			}
		}
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
	
	public int xpneededforlevel(int currentlevel) {
		if (currentlevel == 0) {return 100;} else {
			return ((((currentlevel+1) * (currentlevel+1))+currentlevel+1)/2)*100;
		}
	}
	
	public int xpleftfornextlevel(Member member) {
		int xpneededfornextlevel = this.xpneededforlevel(Integer.parseInt(Configloader.INSTANCE.getUserConfig(member.getGuild(), member.getUser(), "level")));
		return xpneededfornextlevel - Integer.parseInt(Configloader.INSTANCE.getUserConfig(member.getGuild(), member.getUser(), "expe"));
	}
	
	public String devtest(Member member) {
		return String.valueOf(xpneededforlevel(Integer.parseInt(Configloader.INSTANCE.getUserConfig(member.getGuild(), member.getUser(), "level")))) 
				+ " | " + String.valueOf(xpleftfornextlevel(member)) + " | " + Configloader.INSTANCE.getUserConfig(member.getGuild(), member.getUser(), "expe");
	}
}
