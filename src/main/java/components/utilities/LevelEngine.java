package components.utilities;

import java.time.Duration;
import java.time.OffsetDateTime;

import org.json.JSONException;
import org.json.JSONObject;

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
		JSONObject userconfig = ConfigLoader.run.getMemberConfig(guild, user);
		OffsetDateTime now = OffsetDateTime.now();
		OffsetDateTime lastxpgotten = OffsetDateTime.parse(ConfigLoader.run.getMemberConfig(guild, user).getString("lastxpgotten"), ConfigManager.dateTimeFormatter);
		long difference = Duration.between(lastxpgotten, now).toSeconds();
		if(difference >= Long.parseLong(String.valueOf(mindiff))) {
			userconfig.put("levelspamcount", 0);
			this.grantxp(guild, user, amount);
			this.checklevel(guild, user);
			this.checkforreward(guild, user);
		} else {
			int newcount = ConfigLoader.run.getMemberConfig(guild, user).getInt("levelspamcount") + 1;
			userconfig.put("levelspamcount", newcount);
			if (newcount > 20) {
				userconfig.getJSONArray("warnings").put("Spamming for Levels");
				user.openPrivateChannel().complete()
						.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user, "/components/utilities/levelengine:levelspam")
								.replaceDescription("{guild}", guild.getName()).convert()).queue();
			}
		}
	}

	private void grantxp(Guild guild, User user, int amount) {
		JSONObject userconfig = ConfigLoader.run.getMemberConfig(guild, user);
		int current = ConfigLoader.run.getMemberConfig(guild, user).getInt("experience");
		int newamount = current + amount;
		userconfig.put("experience", newamount);
		userconfig.put("lastxpgotten", OffsetDateTime.now().format(ConfigManager.dateTimeFormatter));
	}

	private void checklevel(Guild guild, User user) {
		JSONObject userconfig = ConfigLoader.run.getMemberConfig(guild, user);
		int currentlevel = ConfigLoader.run.getMemberConfig(guild, user).getInt("level");
		if (this.xpleftfornextlevel(guild, user) < 1) {
			userconfig.put("level", currentlevel + 1);
			Long id = ConfigLoader.run.getGuildConfig(guild).getLong("levelmsgchannel");
			if (id == 0) {
				return;
			}
			TextChannel channel = guild.getTextChannelById(id);
			if (channel != null) {
				channel.sendMessageEmbeds(AnswerEngine.build.fetchMessage(guild, user, "/components/utilities/levelengine:levelup")
						.replaceTitle("{user}", guild.getMember(user).getEffectiveName())
						.replaceDescription("{level}", String.valueOf(currentlevel+1)).convert()).queue();
			} else {
				ConfigLoader.run.getGuildConfig(guild).put("levelmsgchannel",0L);
			}
		}
	}

	private void checkforreward(Guild guild, User user) {
		JSONObject levelrewards = ConfigLoader.run.getGuildConfig(guild).getJSONObject("levelrewards");
		if (levelrewards.isEmpty()) {
			return;
		}
		try {
			Long rewardID = levelrewards.getLong(String.valueOf(ConfigLoader.run.getMemberConfig(guild, user).getInt("level")));
			Role rewardrole = guild.getRoleById(rewardID);
			guild.addRoleToMember(guild.getMember(user), rewardrole);
		} catch (JSONException e) {}
	}
	
	public int xpneededforlevel(int currentlevel) {
		if (currentlevel == 0) {return 100;} else {
			return ((((currentlevel+1) * (currentlevel+1))+currentlevel+1)/2)*100;
		}
	}
	
	private int xpleftfornextlevel(Guild guild, User user) {
		int xpneededfornextlevel = this.xpneededforlevel(ConfigLoader.run.getMemberConfig(guild, user).getInt("level"));
		return xpneededfornextlevel - ConfigLoader.run.getMemberConfig(guild, user).getInt("experience");
	}
}
