package components.base;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Properties;

import base.Bot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class Configloader {
	
	public static Configloader INSTANCE;
	private String respath = Bot.environment;
	
	public Configloader() {
		INSTANCE = this;
	}
	
	//Change file in any way
	public String getGuildConfig(Guild guild, String key) {
		return this.getProperty(this.findorCreateGuildConfig(guild), key);
	}
	
	public String getUserConfig(Guild guild, User user, String key) {
		return this.getProperty(this.findorCreateUserConfig(guild, user), key);
	}
	
	public String getMailConfig1(String randomNumber) {
		return this.getProperty(this.findorCreateMailConfig1(), randomNumber);
	}
	
	public String getMailConfig2(String userID) {
		return this.getProperty(this.findorCreateMailConfig2(), userID);
	}
	
	public String getReactionroleConfig(Guild guild, TextChannel channel, String msgid) {
		return this.getProperty(this.findorCreateRRConfig(guild, channel), msgid);
	}
	
	public String getPollConfig(Guild guild, String title, String key) {
		return this.getProperty(this.findPollConfig(guild, title), key);
	}
	
	public void setUserConfig(Guild guild, User user, String key, String value) {
		this.setProperty(this.findorCreateUserConfig(guild, user), key, value);
	}
	
	public void setGuildConfig(Guild guild, String key, String value) {
		this.setProperty(this.findorCreateGuildConfig(guild), key, value);
	}
	
	public void setMailConfig(String randomNumber, String userID) {
		this.setProperty(this.findorCreateMailConfig1(), randomNumber, userID);
		this.setProperty(this.findorCreateMailConfig2(), userID, randomNumber);
	}
	
	public void setReactionroleConfig(Guild guild, TextChannel channel, String msgid, String value) {
		this.setProperty(this.findorCreateRRConfig(guild, channel), msgid, value);
	}
	
	public void setPollConfig(Guild guild, String msgid, String key, String value) {
		this.setProperty(this.findPollConfig(guild, msgid), key, value);
	}
	
	public void addUserConfig(Guild guild, User user, String key, String value) {
		this.addProperty(this.findorCreateUserConfig(guild, user), key, value, this.getUserConfig(guild, user, key));
	}
	
	public void addGuildConfig(Guild guild, String key, String value) {
		this.addProperty(this.findorCreateGuildConfig(guild), key, value, this.getGuildConfig(guild, key));
	}
	
	public void addReactionroleConfig(Guild guild, TextChannel channel, String msgid, String value) {
		this.addProperty(this.findorCreateRRConfig(guild, channel), msgid, value, this.getReactionroleConfig(guild, channel, msgid));
	}
	
	public void deleteGuildConfig(Guild guild, String key, String value) {
		this.deleteProperty(this.findorCreateGuildConfig(guild), key, value, this.getGuildConfig(guild, key));
	}
	
	public void deleteUserConfig(Guild guild, User user, String key, String value) {
		this.deleteProperty(this.findorCreateUserConfig(guild, user), key, value, this.getUserConfig(guild, user, key));
	}
	
	public void removeReactionRoleConfig(Guild guild, TextChannel channel, String msgid) {
		this.removeProperty(this.findorCreateRRConfig(guild, channel), msgid);
	}
	
	public void removeMailConfig(String key) {
		if (this.getMailConfig1(key) != null) {
			String userID = this.getMailConfig1(key);
			this.removeProperty(this.findorCreateMailConfig1(), key);
			this.removeProperty(this.findorCreateMailConfig2(), userID);
		} else {
			String randomNumber = this.getMailConfig2(key);
			this.removeProperty(this.findorCreateMailConfig2(), key);
			this.removeProperty(this.findorCreateMailConfig1(), randomNumber);
		}
	}
	
	public void deletePollConfig(Guild guild, String title) {
		File pollpropertiesFile = this.findPollConfig(guild, title);
		pollpropertiesFile.delete();
	}
	
	//Find or create files
	public File findorCreateMailConfig1() {
		File mailpropertiesFile = new File(respath + "/configs/modmail/708381749826289666/cache1.properties");
		if (!mailpropertiesFile.exists()) {
			try {
				mailpropertiesFile.createNewFile();
			} catch (IOException e) {e.printStackTrace();}
		}
		return mailpropertiesFile;
	}
	
	public File findorCreateMailConfig2() {
		File mailpropertiesFile = new File(respath + "/configs/modmail/708381749826289666/cache2.properties");
		if (!mailpropertiesFile.exists()) {
			try {
				mailpropertiesFile.createNewFile();
			} catch (IOException e) {e.printStackTrace();}
		}
		return mailpropertiesFile;
	}
	
	public File findorCreateRRConfig(Guild guild, TextChannel channel) {
		File guilddir = new File(respath + "/configs/reactionroles/" + guild.getId());
		if (!guilddir.exists()) {
			guilddir.mkdirs();
		}
		File rrpropertiesFile = new File(respath + "/configs/reactionroles/" + guild.getId() + "/" + channel.getId() + ".properties");
		if (!rrpropertiesFile.exists()) {
			try {
				rrpropertiesFile.createNewFile();
			} catch (IOException e) {e.printStackTrace();}
		}
		return rrpropertiesFile;
	}

	public File findorCreateGuildConfig(Guild guild) {
		Properties pps = new Properties();
		File pFile = new File(respath + "/configs/guild/" + guild.getId() + ".properties");
		if (!pFile.exists()) {
			try {
				FileOutputStream fop = new FileOutputStream(pFile);
				pFile.createNewFile();
				pps.setProperty("welcomemsg", "");
				pps.setProperty("goodbyemsg", "");
				pps.setProperty("join2create", "");
				pps.setProperty("suggest", "");
				pps.setProperty("autoroles", "");
				pps.setProperty("botautoroles", "");
				pps.setProperty("modrole", "");
				pps.setProperty("penalties", "");
				pps.setProperty("levelrewards", "");
				pps.setProperty("muterole", "");
				pps.setProperty("j2cs", "");
				pps.setProperty("levelmsgch", "");
				pps.setProperty("ignored", "");
				pps.setProperty("forbidden", "\\//\\//\\//");
				pps.setProperty("reportchannel", "");
				pps.setProperty("supportrole", "");
				pps.setProperty("supportchat", "");
				pps.setProperty("supporttalk", "");
				pps.setProperty("supportcategory", "");
				pps.setProperty("ccrole", "");
				pps.setProperty("ccdefaccess", "");
				pps.setProperty("ticketcount", "00001");
				pps.setProperty("ccctgies", "");
				pps.store(fop, null);
				fop.close();
			} catch (IOException e) {e.printStackTrace();}
		}
		return pFile;
	}
	
	public File findorCreateUserConfig(Guild guild, User user) {
		Properties pps = new Properties();
		File guilddir = new File(respath + "/configs/user/" + guild.getId());
		if (!guilddir.exists()) {
			guilddir.mkdirs();
		}
		File pFile = new File( respath + "/configs/user/" + guild.getId() + "/" + user.getId() + ".properties");
		if (!pFile.exists()) {
			try {
				FileOutputStream fop = new FileOutputStream(pFile);
				pFile.createNewFile();
				pps.setProperty("warnings", "");
				pps.setProperty("muted", "false");
				pps.setProperty("tempmuted", "false");
				pps.setProperty("tmuntil", "");
				pps.setProperty("tempbanned", "false");
				pps.setProperty("tbuntil", "");
				pps.setProperty("lastxpgotten", OffsetDateTime.now().toString());
				pps.setProperty("level", "0");
				pps.setProperty("expe", "0");
				pps.setProperty("levelbackground", "0");
				pps.setProperty("lastmail", OffsetDateTime.now().toString());
				pps.setProperty("lastsuggestion", OffsetDateTime.now().toString());
				pps.setProperty("language", "en");
				pps.setProperty("levelspamcount", "0");
				pps.setProperty("cccategory", "");
				pps.setProperty("penaltycount", "");
				pps.store(fop, null);
				fop.close();
			} catch (IOException e) {e.printStackTrace();}
		}
		return pFile;
	}
	
	public File findPollConfig(Guild guild, String msgid) {
		File pollpropertiesFile = new File(respath + "/configs/polls/" + guild.getId() + "/" + msgid + ".properties");
		if (pollpropertiesFile.exists()) {
			return pollpropertiesFile;
		} else {
			return null;
		}
	}
	
	public File createPollConfig(Guild guild, String title) {
		Properties pps = new Properties();
		File guilddir = new File(respath + "/configs/polls/" + guild.getId());
		if (!guilddir.exists()) {
			guilddir.mkdirs();
		}
		File pollpropertiesFile = new File(respath + "/configs/polls/" + guild.getId() + "/" + title + ".properties");
		try {
			FileOutputStream fop = new FileOutputStream(pollpropertiesFile);
			pollpropertiesFile.createNewFile();
			pps.setProperty("description", "");
			pps.setProperty("title", "");
			pps.setProperty("answers", "");
			pps.setProperty("answercount", "");
			pps.setProperty("thumbnail", "");
			pps.setProperty("days", "");
			pps.setProperty("anonymous", "");
			pps.setProperty("guild", "");
			pps.setProperty("owner", "");
			pps.setProperty("channel", "");
			pps.setProperty("footer", "");
			pps.setProperty("users", "");
			pps.setProperty("creation", OffsetDateTime.now().toString());
			pps.store(fop, null);
			fop.close();
		} catch (IOException e) {e.printStackTrace();}
		return pollpropertiesFile;
	}
	
	//Tool-Methods
	private String getProperty(File file, String key) {
		Properties pps = new Properties();
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
			pps.load(bis);
			bis.close();
		} catch (Exception e) {e.printStackTrace();}
		return pps.getProperty(key);
	}
	
	private boolean setProperty(File file, String key, String value) {
		Properties pps = new Properties();
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
			pps.load(bis);
			bis.close();
			FileOutputStream out = new FileOutputStream(file);
			pps.setProperty(key, value);
			pps.store(out, null);
			out.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private boolean addProperty(File file, String key, String value, String current) {
		Properties pps = new Properties();
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
			pps.load(bis);
			bis.close();
			FileOutputStream out = new FileOutputStream(file);
			if (current.equals("") || current.equals(null)) {
				pps.setProperty(key, value);
			} else {
				pps.setProperty(key, current + ";" + value);
			}
			pps.store(out, null);
			out.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private boolean deleteProperty(File file, String key, String value, String currentraw) {
		Properties pps = new Properties();
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
			pps.load(bis);
			bis.close();
			FileOutputStream out = new FileOutputStream(file);
			String[] current = currentraw.split(";");
			if (current.length <= 1) {
				pps.setProperty(key, "");
			} else {
				if(current[0].equals(value)) {
					pps.setProperty(key, currentraw.replace(value + ";", ""));
				} else {
					pps.setProperty(key, currentraw.replace(";" + value, ""));
				}
			}
			pps.store(out, null);
			out.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private boolean removeProperty(File file, String key) {
		Properties pps = new Properties();
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
			pps.load(bis);
			bis.close();
			FileOutputStream out1 = new FileOutputStream(file);
			pps.remove(key);
			pps.store(out1, null);
			out1.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
 }