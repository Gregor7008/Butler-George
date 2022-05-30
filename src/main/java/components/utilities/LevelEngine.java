package components.utilities;

import java.time.Duration;
import java.time.OffsetDateTime;

import components.base.AnswerEngine;
import components.base.ConfigLoader;
import components.base.assets.ConfigManager;
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
		OffsetDateTime lastxpgotten = OffsetDateTime.parse(ConfigLoader.run.getUserConfig(guild, user).getString("lastxpgotten"), ConfigManager.dateTimeFormatter);
		long difference = Duration.between(lastxpgotten, now).toSeconds();
		if(difference >= Long.parseLong(String.valueOf(mindiff))) {
			ConfigLoader.run.getUserConfig(guild, user).put("levelspamcount", Integer.valueOf(0));
			this.grantxp(guild, user, amount);
			this.checklevel(guild, user);
			this.checkforreward(guild, user);
		} else {
			int newcount = Integer.parseInt(ConfigLoader.run.getUserConfig(guild, user, "levelspamcount")) + 1;
			ConfigLoader.run.setUserConfig(guild, user, "levelspamcount", String.valueOf(newcount));
			if (newcount > 20) {
				ConfigLoader.run.addUserConfig(guild, user, "warnings", "Levelspamming");
				user.openPrivateChannel().complete()
						.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/components/utilities/levelengine:levelspam")
								.replaceDescription("{guild}", guild.getName()).convert()).queue();
			}
		}
	}

	private void grantxp(Guild guild, User user, int amount) {
		int current = Integer.parseInt(ConfigLoader.run.getUserConfig(guild, user, "expe"));
		int newamount = current + amount;
		ConfigLoader.run.setUserConfig(guild, user, "expe", String.valueOf(newamount));
		ConfigLoader.run.setUserConfig(guild, user, "lastxpgotten", OffsetDateTime.now().toString());
	}

	private void checklevel(Guild guild, User user) {
		int currentlevel = Integer.valueOf(ConfigLoader.run.getUserConfig(guild, user, "level"));
		if (this.xpleftfornextlevel(guild, user) < 1) {
			ConfigLoader.run.setUserConfig(guild, user, "level", String.valueOf(currentlevel + 1));
			String id = ConfigLoader.run.getGuildConfig(guild, "levelmsgch");
			if (id.equals("")) {
				return;
			}
			TextChannel channel = guild.getTextChannelById(id);
			if (channel != null) {
				channel.sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/components/utilities/levelengine:levelup")
						.replaceTitle("{user}", guild.getMember(user).getEffectiveName())
						.replaceDescription("{level}", String.valueOf(currentlevel+1)).convert()).queue();
			} else {
				ConfigLoader.run.setGuildConfig(guild, "levelmsgch", "");
			}
		}
	}

	private void checkforreward(Guild guild, User user) {
		String rawinput = ConfigLoader.run.getGuildConfig(guild, "levelrewards");
		if (rawinput.equals("")) {
			return;
		}
		String[] rewards = rawinput.split(";");
		for (int i = 0; i < rewards.length; i++) {
			String[] reward = rewards[i].split("_");
			if (Integer.parseInt(ConfigLoader.run.getUserConfig(guild, user, "level")) >= Integer.parseInt(reward[1])) {
				Role rewardrole = guild.getRoleById(reward[0]);
				guild.addRoleToMember(guild.getMember(user), rewardrole);
			}
		}
	}
	
	private int xpneededforlevel(int currentlevel) {
		if (currentlevel == 0) {return 100;} else {
			return ((((currentlevel+1) * (currentlevel+1))+currentlevel+1)/2)*100;
		}
	}
	
	private int xpleftfornextlevel(Guild guild, User user) {
		int xpneededfornextlevel = this.xpneededforlevel(Integer.parseInt(ConfigLoader.run.getUserConfig(guild, user, "level")));
		return xpneededfornextlevel - Integer.parseInt(ConfigLoader.run.getUserConfig(guild, user, "expe"));
	}
}
