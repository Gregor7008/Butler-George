package engines.functions;

import java.time.Duration;
import java.time.OffsetDateTime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import engines.base.LanguageEngine;
import engines.base.Toolbox;
import engines.data.ConfigLoader;
import engines.data.ConfigManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
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

	public void voicejoin(GuildVoiceUpdateEvent event) {
		OffsetDateTime time = java.time.OffsetDateTime.now();
		this.givexp(event.getGuild(), event.getMember().getUser(), time, 50, 600);
	}
	
	public void voicemove(GuildVoiceUpdateEvent event) {
		OffsetDateTime time = java.time.OffsetDateTime.now();
		this.givexp(event.getGuild(), event.getMember().getUser(), time, 50, 600);
	}
	
	private void givexp(Guild guild, User user, OffsetDateTime time, int amount, int mindiff) {
		JSONObject userconfig = ConfigLoader.INSTANCE.getMemberConfig(guild, user);
		OffsetDateTime now = OffsetDateTime.now();
		OffsetDateTime lastxpgotten = OffsetDateTime.parse(ConfigLoader.INSTANCE.getMemberConfig(guild, user).getString("lastxpgotten"), ConfigManager.DATA_TIME_SAVE_FORMAT);
		long difference = Duration.between(lastxpgotten, now).toSeconds();
		if(difference >= Long.parseLong(String.valueOf(mindiff))) {
			userconfig.put("levelspamcount", 0);
			this.grantxp(guild, user, amount);
			this.checklevel(guild, user);
			this.checkforreward(guild, user);
		} else {
			int newcount = ConfigLoader.INSTANCE.getMemberConfig(guild, user).getInt("levelspamcount") + 1;
			userconfig.put("levelspamcount", newcount);
			if (newcount > 20) {
				userconfig.getJSONArray("warnings").put("Spamming for Levels");
				user.openPrivateChannel().complete()
						.sendMessageEmbeds(LanguageEngine.getMessageEmbed(guild, user, this, "levelspam")
								.replaceDescription("{guild}", guild.getName())).queue();
			}
		}
	}

	private void grantxp(Guild guild, User user, int amount) {
		JSONObject userconfig = ConfigLoader.INSTANCE.getMemberConfig(guild, user);
		int current = ConfigLoader.INSTANCE.getMemberConfig(guild, user).getInt("experience");
		int newamount = current + amount;
		userconfig.put("experience", newamount);
		userconfig.put("lastxpgotten", OffsetDateTime.now().format(ConfigManager.DATA_TIME_SAVE_FORMAT));
	}

	private void checklevel(Guild guild, User user) {
		JSONObject userconfig = ConfigLoader.INSTANCE.getMemberConfig(guild, user);
		int currentlevel = ConfigLoader.INSTANCE.getMemberConfig(guild, user).getInt("level");
		if (this.xpleftfornextlevel(guild, user) < 1) {
			userconfig.put("level", currentlevel + 1);
			JSONArray levelmessage = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONArray("levelmsg");
			if (!levelmessage.isEmpty() && levelmessage.getBoolean(3)) {
				String title = Toolbox.processAutoMessage(levelmessage.getString(1), guild, user, false);
				String message = Toolbox.processAutoMessage(levelmessage.getString(2), guild, user, true);
				guild.getTextChannelById(levelmessage.getLong(0)).sendMessageEmbeds(LanguageEngine.buildMessageEmbed(title, message)).queue();
				return;
			}
		}
	}

	private void checkforreward(Guild guild, User user) {
		JSONObject levelrewards = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONObject("levelrewards");
		if (levelrewards.isEmpty()) {
			return;
		}
		try {
			Long rewardID = levelrewards.getLong(String.valueOf(ConfigLoader.INSTANCE.getMemberConfig(guild, user).getInt("level")));
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
		int xpneededfornextlevel = this.xpneededforlevel(ConfigLoader.INSTANCE.getMemberConfig(guild, user).getInt("level"));
		return xpneededfornextlevel - ConfigLoader.INSTANCE.getMemberConfig(guild, user).getInt("experience");
	}
}
