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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class Configloader {
	
	public static Configloader INSTANCE;
	
	public Configloader() {
		INSTANCE = this;
	}
	
	public String getGuildConfig(Guild guild, String key) {
		Properties pps = new Properties();
		File pFile = this.findorCreateGuildConfig(guild);
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(pFile))) {
			pps.load(bis);
			bis.close();
		} catch (Exception e) {e.printStackTrace();}
		return pps.getProperty(key);
	}
	
	public String getUserConfig(Guild guild, User user, String key) {
		Properties pps = new Properties();
		File pFile = this.findorCreateUserConfig(guild, user);
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(pFile))) {
			pps.load(bis);
			bis.close();
		} catch (Exception e) {e.printStackTrace();}
		return pps.getProperty(key);
	}
	
	public String getMailConfig1(String randomNumber) {
		Properties pps = new Properties();
		File pFile = this.findorCreateMailConfig1();
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(pFile))) {
			pps.load(bis);
			bis.close();
		} catch (Exception e) {e.printStackTrace();}
		return pps.getProperty(randomNumber);
	}
	
	public String getMailConfig2(String userID) {
		Properties pps = new Properties();
		File pFile = this.findorCreateMailConfig2();
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(pFile))) {
			pps.load(bis);
			bis.close();
		} catch (Exception e) {e.printStackTrace();}
		return pps.getProperty(userID);
	}
	
	public String getPollConfig(Guild guild, String title, String key) {
		Properties pps = new Properties();
		File pFile = this.findPollConfig(guild, title);
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(pFile))) {
			pps.load(bis);
			bis.close();
		} catch (Exception e) {e.printStackTrace();}
		return pps.getProperty(key);
	}
	
	public void setUserConfig(Member member, String key, String value) {
		Properties pps = new Properties();
		File pFile = this.findorCreateUserConfig(member.getGuild(), member.getUser());
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(pFile))) {
			pps.load(bis);
			bis.close();
			FileOutputStream out1 = new FileOutputStream(pFile);
			pps.setProperty(key, value);
			pps.store(out1, null);
			out1.close();
		} catch (Exception e) {e.printStackTrace();}
		
	}
	
	public void setGuildConfig(Guild guild, String key, String value) {
		Properties pps = new Properties();
		File pFile = this.findorCreateGuildConfig(guild);
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(pFile))) {
			pps.load(bis);
			bis.close();
			FileOutputStream out1 = new FileOutputStream(pFile);
			pps.setProperty(key, value);
			pps.store(out1, null);
			out1.close();
		} catch (Exception e) {e.printStackTrace();}
	}
	
	public void setMailConfig(String randomNumber, String userID) {
		Properties pps1 = new Properties();
		File pFile1 = this.findorCreateMailConfig1();
		try (BufferedInputStream bis1 = new BufferedInputStream(new FileInputStream(pFile1))) {
			pps1.load(bis1);
			bis1.close();
			FileOutputStream out1 = new FileOutputStream(pFile1);
			pps1.setProperty(randomNumber, userID);
			pps1.store(out1, null);
			out1.close();
		} catch (Exception e) {e.printStackTrace();}
		Properties pps2 = new Properties();
		File pFile2 = this.findorCreateMailConfig2();
		try (BufferedInputStream bis2 = new BufferedInputStream(new FileInputStream(pFile2))) {
			pps2.load(bis2);
			bis2.close();
			FileOutputStream out2 = new FileOutputStream(pFile2);
			pps2.setProperty(userID, randomNumber);
			pps2.store(out2, null);
			out2.close();
		} catch (Exception e) {e.printStackTrace();}
	}
	
	public void setPollConfig(Guild guild, String title, String key, String value) {
		Properties pps = new Properties();
		File pFile = this.findPollConfig(guild, title);
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(pFile))) {
			pps.load(bis);
			bis.close();
			FileOutputStream out1 = new FileOutputStream(pFile);
			pps.setProperty(key, value);
			pps.store(out1, null);
			out1.close();
		} catch (Exception e) {e.printStackTrace();}
	}
	
	public void addUserConfig(Member member, String key, String value) {
		Properties pps = new Properties();
		File pFile = this.findorCreateUserConfig(member.getGuild(), member.getUser());
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(pFile));){
			String current = this.getUserConfig(member.getGuild(), member.getUser(), key);
			pps.load(bis);
			bis.close();
			FileOutputStream out4 = new FileOutputStream(pFile);
			if (current.equals("")) {
				pps.setProperty(key, value);
			} else {
				pps.setProperty(key, current + ";" + value);
			}
			pps.store(out4, null);
			out4.close();
		} catch (Exception e) {e.printStackTrace();}
	}
	
	public void addGuildConfig(Guild guild, String key, String value) {
		Properties pps = new Properties();
		File pFile = this.findorCreateGuildConfig(guild);
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(pFile));){
			String current = this.getGuildConfig(guild, key);
			pps.load(bis);
			bis.close();
			FileOutputStream out4 = new FileOutputStream(pFile);
			if (current.equals("")) {
				pps.setProperty(key, value);
			} else {
				pps.setProperty(key, current + ";" + value);
			}
			pps.store(out4, null);
			out4.close();
		} catch (Exception e) {e.printStackTrace();}
	}
	
	public void deleteGuildConfig(Guild guild, String key, String value) {
		Properties pps = new Properties();
		File pFile = this.findorCreateGuildConfig(guild);
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(pFile));){
			String currentraw = this.getGuildConfig(guild, key);
			pps.load(bis);
			bis.close();
			FileOutputStream out5 = new FileOutputStream(pFile);
			String[] current = currentraw.split(";");
			if (current.length == 1) {
				pps.setProperty(key, "");
			} else {
				if(current[0].equals(value)) {
					pps.setProperty(key, currentraw.replace(value + ";", ""));
				} else {
					pps.setProperty(key, currentraw.replace(";" + value, ""));
				}
			}
			pps.store(out5, null);
			out5.close();
		} catch (Exception e) {e.printStackTrace();}
	}
	
	public void deleteUserConfig(Member member, String key, String value) {
		Properties pps = new Properties();
		File pFile = this.findorCreateUserConfig(member.getGuild(), member.getUser());
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(pFile));){
			String currentraw = this.getUserConfig(member.getGuild(), member.getUser(), key);
			pps.load(bis);
			bis.close();
			FileOutputStream out6 = new FileOutputStream(pFile);
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
			pps.store(out6, null);
			out6.close();
		} catch (Exception e) {}
	}
	
	public void deleteMailConfig(String key) {
		if (this.getMailConfig1(key) != null) {
			String userID = "";
			Properties pps1 = new Properties();
			File pFile1 = this.findorCreateMailConfig1();
			try (BufferedInputStream bis1 = new BufferedInputStream(new FileInputStream(pFile1));){
				pps1.load(bis1);
				bis1.close();
				FileOutputStream out1 = new FileOutputStream(pFile1);
				userID = pps1.getProperty(key);
				pps1.remove(key);
				pps1.store(out1, null);
				out1.close();
			} catch (Exception e) {}
			Properties pps2 = new Properties();
			File pFile2 = this.findorCreateMailConfig2();
			try (BufferedInputStream bis2 = new BufferedInputStream(new FileInputStream(pFile2));){
				pps2.load(bis2);
				bis2.close();
				FileOutputStream out2 = new FileOutputStream(pFile2);
				pps2.remove(userID);
				pps2.store(out2, null);
				out2.close();
			} catch (Exception e) {}
		} else {
			String randomNumber = "";
			Properties pps1 = new Properties();
			File pFile1 = this.findorCreateMailConfig2();
			try (BufferedInputStream bis1 = new BufferedInputStream(new FileInputStream(pFile1));){
				pps1.load(bis1);
				bis1.close();
				FileOutputStream out1 = new FileOutputStream(pFile1);
				randomNumber = pps1.getProperty(key);
				pps1.remove(key);
				pps1.store(out1, null);
				out1.close();
			} catch (Exception e) {}
			Properties pps2 = new Properties();
			File pFile2 = this.findorCreateMailConfig1();
			try (BufferedInputStream bis2 = new BufferedInputStream(new FileInputStream(pFile2));){
				pps2.load(bis2);
				bis2.close();
				FileOutputStream out2 = new FileOutputStream(pFile2);
				pps2.remove(randomNumber);
				pps2.store(out2, null);
				out2.close();
			} catch (Exception e) {}
		}
	}
	
	public void deletePollConfig(Guild guild, String title) {
		File pollpropertiesFile = this.findPollConfig(guild, title);
		pollpropertiesFile.delete();
	}

	public File findorCreateGuildConfig(Guild guild) {
		Properties pps = new Properties();
		File pFile = new File(Bot.INSTANCE.getBotConfig("resourcepath") + "/configs/guild/" + guild.getId() + ".properties");
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
				pps.setProperty("autopunish", "");
				pps.setProperty("levelrewards", "");
				pps.setProperty("muterole", "");
				pps.setProperty("j2cs", "");
				pps.setProperty("levelmsgch", "");
				pps.setProperty("ignored", "");
				pps.setProperty("forbidden", "");
				pps.setProperty("reportchannel", "");
				pps.store(fop, null);
				fop.close();
			} catch (IOException e) {e.printStackTrace();}
		}
		return pFile;
	}
	
	public File findorCreateUserConfig(Guild guild, User user) {
		Properties pps = new Properties();
		File guilddir = new File(Bot.INSTANCE.getBotConfig("resourcepath") + "/configs/user/" + guild.getId());
		if (!guilddir.exists()) {
			guilddir.mkdirs();
		}
		
		File pFile = new File(Bot.INSTANCE.getBotConfig("resourcepath") + "/configs/user/" + guild.getId() + "/" + user.getId() + ".properties");
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
				pps.store(fop, null);
				fop.close();
			} catch (IOException e) {e.printStackTrace();}
		}
		return pFile;
	}
	
	public File findorCreateMailConfig1() {
		File mailpropertiesFile = new File(Bot.INSTANCE.getBotConfig("resourcepath") + "/modmail/cache1.properties");
		if (!mailpropertiesFile.exists()) {
			try {
				mailpropertiesFile.createNewFile();
			} catch (IOException e) {e.printStackTrace();}
		}
		return mailpropertiesFile;
	}
	
	public File findorCreateMailConfig2() {
		File mailpropertiesFile = new File(Bot.INSTANCE.getBotConfig("resourcepath") + "/modmail/cache2.properties");
		if (!mailpropertiesFile.exists()) {
			try {
				mailpropertiesFile.createNewFile();
			} catch (IOException e) {e.printStackTrace();}
		}
		return mailpropertiesFile;
	}
	
	public File findPollConfig(Guild guild, String msgid) {
		File pollpropertiesFile = new File(Bot.INSTANCE.getBotConfig("resourcepath") + "/configs/polls/" + guild.getId() + "/" + msgid + ".properties");
		if (pollpropertiesFile.exists()) {
			return pollpropertiesFile;
		} else {
			return null;
		}
	}
	
	public File createPollConfig(Guild guild, String title) {
		Properties pps = new Properties();
		File guilddir = new File(Bot.INSTANCE.getBotConfig("resourcepath") + "/configs/polls/" + guild.getId());
		if (!guilddir.exists()) {
			guilddir.mkdirs();
		}
		File pollpropertiesFile = new File(Bot.INSTANCE.getBotConfig("resourcepath") + "/configs/polls/" + guild.getId() + "/" + title + ".properties");
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
 }