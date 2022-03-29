package components.utilities;

import java.time.Duration;
import java.time.OffsetDateTime;

import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class LevelEngine {
	
	private static LevelEngine INSTANCE;
	
	public static LevelEngine getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new LevelEngine();
		}
		return INSTANCE;
	}
	
	public void messagereceived(MessageReceivedEvent event) {
		OffsetDateTime time = event.getMessage().getTimeCreated();
		this.givexp(event.getGuild(), event.getAuthor(), time, 10, 30);
	}
	
	public void slashcommand(SlashCommandInteractionEvent event) {
		OffsetDateTime time = java.time.OffsetDateTime.now();
		this.givexp(event.getGuild(), event.getUser(), time, 20, 30);
	}

	public void voicejoin(GuildVoiceJoinEvent event) {
		OffsetDateTime time = java.time.OffsetDateTime.now();
		this.givexp(event.getGuild(), event.getMember().getUser(), time, 50, 600);
	}
	
	public void voicemove(GuildVoiceMoveEvent event) {
		OffsetDateTime time = java.time.OffsetDateTime.now();
		this.givexp(event.getGuild(), event.getMember().getUser(), time, 50, 600);
	}
	
	private void givexp(Guild guild, User user, OffsetDateTime time, int amount, int mindiff) {
		OffsetDateTime now = OffsetDateTime.now();
		OffsetDateTime lastxpgotten = OffsetDateTime.parse(Configloader.INSTANCE.getUserConfig(guild, user, "lastxpgotten"));
		long difference = Duration.between(lastxpgotten, now).toSeconds();
		if(difference >= Long.parseLong(String.valueOf(mindiff))) {
			Configloader.INSTANCE.setUserConfig(guild, user, "levelspamcount", "0");
			this.grantxp(guild, user, amount);
			this.checklevel(guild, user);
			this.checkforreward(guild, user);
		} else {
			int newcount = Integer.parseInt(Configloader.INSTANCE.getUserConfig(guild, user, "levelspamcount")) + 1;
			Configloader.INSTANCE.setUserConfig(guild, user, "levelspamcount", String.valueOf(newcount));
			if (newcount > 20) {
				Configloader.INSTANCE.addUserConfig(guild, user, "warnings", "Levelspamming");
				user.openPrivateChannel().complete()
						.sendMessageEmbeds(AnswerEngine.ae.buildMessage("Warning!", ":warning: | You have been warned on the " + guild.getName() + " server for spamming to level up faster!")).queue();
			}
		}
	}

	private void grantxp(Guild guild, User user, int amount) {
		int current = Integer.parseInt(Configloader.INSTANCE.getUserConfig(guild, user, "expe"));
		int newamount = current + amount;
		Configloader.INSTANCE.setUserConfig(guild, user, "expe", String.valueOf(newamount));
		Configloader.INSTANCE.setUserConfig(guild, user, "lastxpgotten", OffsetDateTime.now().toString());
	}

	private void checklevel(Guild guild, User user) {
		int currentlevel = Integer.valueOf(Configloader.INSTANCE.getUserConfig(guild, user, "level"));
		if (this.xpleftfornextlevel(guild, user) < 1) {
			Configloader.INSTANCE.setUserConfig(guild, user, "level", String.valueOf(currentlevel + 1));
			String id = Configloader.INSTANCE.getGuildConfig(guild, "levelmsgch");
			if (id.equals("")) {
				return;
			}
			TextChannel channel = guild.getTextChannelById(id);
			if (channel != null) {
				channel.sendMessageEmbeds(AnswerEngine.ae.buildMessage(":confetti_ball: Congrats\s" + guild.getMember(user).getEffectiveName()
						+ "\s! :confetti_ball:", "You just reached level\s" + String.valueOf(currentlevel+1) + "!")).queue();
			} else {
				Configloader.INSTANCE.setGuildConfig(guild, "levelmsgch", "");
			}
		}
	}

	private void checkforreward(Guild guild, User user) {
		String rawinput = Configloader.INSTANCE.getGuildConfig(guild, "levelrewards");
		if (rawinput.equals("")) {
			return;
		}
		String[] rewards = rawinput.split(";");
		for (int i = 0; i < rewards.length; i++) {
			String[] reward = rewards[i].split("_");
			if (Integer.parseInt(Configloader.INSTANCE.getUserConfig(guild, user, "level")) >= Integer.parseInt(reward[1])) {
				Role rewardrole = guild.getRoleById(reward[0]);
				guild.addRoleToMember(guild.getMember(user), rewardrole);
			}
		}
	}
	
	public int xpneededforlevel(int currentlevel) {
		if (currentlevel == 0) {return 100;} else {
			return ((((currentlevel+1) * (currentlevel+1))+currentlevel+1)/2)*100;
		}
	}
	
	public int xpleftfornextlevel(Guild guild, User user) {
		int xpneededfornextlevel = this.xpneededforlevel(Integer.parseInt(Configloader.INSTANCE.getUserConfig(guild, user, "level")));
		return xpneededfornextlevel - Integer.parseInt(Configloader.INSTANCE.getUserConfig(guild, user, "expe"));
	}
	
	public String devtest(Guild guild, User user) {
		return String.valueOf(xpneededforlevel(Integer.parseInt(Configloader.INSTANCE.getUserConfig(guild, user, "level")))) 
				+ " | " + String.valueOf(xpleftfornextlevel(guild, user)) + " | " + Configloader.INSTANCE.getUserConfig(guild, user, "expe");
	}
}
