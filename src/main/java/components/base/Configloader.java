package components.base;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
		Properties properties1 = new Properties();
		File propertiesFile1 = this.findorCreateGuildConfig(guild);
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(propertiesFile1))) {
			properties1.load(bis);
			bis.close();
		} catch (Exception e) {e.printStackTrace();}
		return properties1.getProperty(key);
	}
	
	public String getUserConfig(Guild guild, User user, String key) {
		Properties properties2 = new Properties();
		File propertiesFile2 = this.findorCreateUserConfig(guild, user);
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(propertiesFile2))) {
			properties2.load(bis);
			bis.close();
		} catch (Exception e) {e.printStackTrace();}
		return properties2.getProperty(key);
	}
	
	public void setUserConfig(Member member, String key, String value) {
		Properties properties3 = new Properties();
		File propertiesFile3 = this.findorCreateUserConfig(member.getGuild(), member.getUser());
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(propertiesFile3))) {
			properties3.load(bis);
			bis.close();
			FileOutputStream out1 = new FileOutputStream(propertiesFile3);
			properties3.setProperty(key, value);
			properties3.store(out1, null);
			out1.close();
		} catch (Exception e) {e.printStackTrace();}
		
	}
	
	public void setGuildConfig(Guild guild, String key, String value) {
		Properties properties4 = new Properties();
		File propertiesFile4 = this.findorCreateGuildConfig(guild);
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(propertiesFile4))) {
			properties4.load(bis);
			bis.close();
			FileOutputStream out1 = new FileOutputStream(propertiesFile4);
			properties4.setProperty(key, value);
			properties4.store(out1, null);
			out1.close();
		} catch (Exception e) {e.printStackTrace();}
	}
	
	public void addUserConfig(Member member, String key, String value) {
		Properties properties5 = new Properties();
		File propertiesFile5 = this.findorCreateUserConfig(member.getGuild(), member.getUser());
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(propertiesFile5));){
			String current = this.getUserConfig(member.getGuild(), member.getUser(), key);
			properties5.load(bis);
			bis.close();
			FileOutputStream out4 = new FileOutputStream(propertiesFile5);
			if (current.equals("")) {
				properties5.setProperty(key, value);
			} else {
				properties5.setProperty(key, current + ";" + value);
			}
			properties5.store(out4, null);
			out4.close();
		} catch (Exception e) {e.printStackTrace();}
	}
	
	public void addGuildConfig(Guild guild, String key, String value) {
		Properties properties6 = new Properties();
		File propertiesFile6 = this.findorCreateGuildConfig(guild);
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(propertiesFile6));){
			String current = this.getGuildConfig(guild, key);
			properties6.load(bis);
			bis.close();
			FileOutputStream out4 = new FileOutputStream(propertiesFile6);
			if (current.equals("")) {
				properties6.setProperty(key, value);
			} else {
				properties6.setProperty(key, current + ";" + value);
			}
			properties6.store(out4, null);
			out4.close();
		} catch (Exception e) {e.printStackTrace();}
	}
	
	public void deleteGuildConfig(Guild guild, String key, String value) {
		Properties properties7 = new Properties();
		File propertiesFile7 = this.findorCreateGuildConfig(guild);
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(propertiesFile7));){
			String currentraw = this.getGuildConfig(guild, key);
			properties7.load(bis);
			bis.close();
			FileOutputStream out5 = new FileOutputStream(propertiesFile7);
			String[] current = currentraw.split(";");
			if (current.length == 1) {
				properties7.setProperty(key, "");
			} else {
				if(current[0].equals(value)) {
					properties7.setProperty(key, currentraw.replace(value + ";", ""));
				} else {
					properties7.setProperty(key, currentraw.replace(";" + value, ""));
				}
			}
			properties7.store(out5, null);
			out5.close();
		} catch (Exception e) {e.printStackTrace();}
	}
	
	public void deleteUserConfig(Member member, String key, String value) {
		Properties properties8 = new Properties();
		File propertiesFile8 = this.findorCreateUserConfig(member.getGuild(), member.getUser());
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(propertiesFile8));){
			String currentraw = this.getUserConfig(member.getGuild(), member.getUser(), key);
			properties8.load(bis);
			bis.close();
			FileOutputStream out6 = new FileOutputStream(propertiesFile8);
			String[] current = currentraw.split(";");
			if (current.length == 1) {
				properties8.setProperty(key, "");
			} else {
				if(current[0].equals(value)) {
					properties8.setProperty(key, currentraw.replace(value + ";", ""));
				} else {
					properties8.setProperty(key, currentraw.replace(";" + value, ""));
				}
			}
			properties8.store(out6, null);
			out6.close();
		} catch (Exception e) {}
	}

	public File findorCreateGuildConfig(Guild guild) {
		Properties properties9 = new Properties();
		File guildpropertiesFile = new File(Bot.INSTANCE.getBotConfig("resourcepath") + "/configs/guild/" + guild.getId() + ".properties");
		if (!guildpropertiesFile.exists()) {
			try {
				guildpropertiesFile.createNewFile();
				properties9.setProperty("welcomemsg", "");
				properties9.setProperty("goodbyemsg", "");
				properties9.setProperty("join2create", "");
				properties9.setProperty("suggest", "");
				properties9.setProperty("autoroles", "");
				properties9.setProperty("botautoroles", "");
				properties9.setProperty("modrole", "");
				properties9.setProperty("autopunish", "");
				properties9.setProperty("levelrewards", "");
				properties9.store(new FileOutputStream(guildpropertiesFile), null);
			} catch (IOException e) {e.printStackTrace();}
		}
		return guildpropertiesFile;
	}
	
	public File findorCreateUserConfig(Guild guild, User user) {
		Properties properties10 = new Properties();
		File guilddir = new File(Bot.INSTANCE.getBotConfig("resourcepath") + "/configs/user/" + guild.getId());
		if (!guilddir.exists()) {
			guilddir.mkdirs();
		}
		
		File userpropertiesFile = new File(Bot.INSTANCE.getBotConfig("resourcepath") + "/configs/user/" + guild.getId() + "/" + user.getId() + ".properties");
		if (!userpropertiesFile.exists()) {
			try {
				userpropertiesFile.createNewFile();
				properties10.setProperty("warnings", "");
				properties10.setProperty("muted", "false");
				properties10.setProperty("tempmuted", "false");
				properties10.setProperty("tmuntil", "");
				properties10.setProperty("tempbanned", "false");
				properties10.setProperty("tbuntil", "");
				properties10.setProperty("lastxpgotten", java.time.OffsetDateTime.now().toString());
				properties10.setProperty("level", "0");
				properties10.setProperty("expe", "0");
				properties10.setProperty("levelbackground", "0");
				properties10.store(new FileOutputStream(userpropertiesFile), null);
			} catch (IOException e) {e.printStackTrace();}
		}
		return userpropertiesFile;
	}
}