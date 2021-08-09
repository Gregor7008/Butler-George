package base;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class Configloader {
	
	public static Configloader INSTANCE;
	private Properties properties;
	
	
	public Configloader() {
		INSTANCE = this;
		properties = new Properties();
	}
	
	public String getGuildConfig(Guild guild, String key) {
		File propertiesFile = this.getGuildConfigFile(guild);
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(propertiesFile))) {
		  properties.load(bis);
		} catch (Exception e) {}
		return properties.getProperty(key);
	}
	
	public String getUserConfig(Member member, String key) {
		File propertiesFile = this.getUserConfigFile(member);
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(propertiesFile))) {
		  properties.load(bis);
		} catch (Exception e) {}
		return properties.getProperty(key);
	}
	
	public void setUserConfig(Member member, String key, String value) {
		try {
		File propertiesFile = this.getUserConfigFile(member);
		FileOutputStream out = new FileOutputStream(propertiesFile);
		properties.setProperty(key, value);
		properties.store(out, null);
		out.close();
		} catch (Exception e) {}
	}
	
	public void setGuildConfig(Guild guild, String key, String value) {
		try {
			File propertiesFile = this.getGuildConfigFile(guild);
			FileOutputStream out = new FileOutputStream(propertiesFile);
			properties.setProperty(key, value);
			properties.store(out, null);
			out.close();
			} catch (Exception e) {}
	}
	
	public void addUserConfig(Member member, String key, String value) {
		try {
			File propertiesFile = this.getUserConfigFile(member);
			String current = this.getUserConfig(member, key);
			FileOutputStream out = new FileOutputStream(propertiesFile);
			if (current.equals("")) {
				properties.setProperty(key, value);
			} else {
				properties.setProperty(key, current + ";" + value);
			}
			properties.store(out, null);
			out.close();
		} catch (Exception e) {}
	}
	
	public void addGuildConfig(Guild guild, String key, String value) {
		try {
			File propertiesFile = this.getGuildConfigFile(guild);
			String current = this.getGuildConfig(guild, key);
			FileOutputStream out = new FileOutputStream(propertiesFile);
			if (current.equals("")) {
				properties.setProperty(key, value);
			} else {
				properties.setProperty(key, current + ";" + value);
			}
			properties.store(out, null);
			out.close();
		} catch (Exception e) {}
	}
	
	public void deleteGuildConfig(Guild guild, String key, String value) {
		try {
			File propertiesFile = this.getGuildConfigFile(guild);
			String currentraw = this.getGuildConfig(guild, key);
			FileOutputStream out = new FileOutputStream(propertiesFile);
			String[] current = currentraw.split(";");
			if (current.length == 1) {
				properties.setProperty(key, "");
			} else {
				if(current[0].equals(value)) {
					properties.setProperty(key, currentraw.replace(value + ";", ""));
				} else {
					properties.setProperty(key, currentraw.replace(";" + value, ""));
				}
			}
			properties.store(out, null);
			out.close();
		} catch (Exception e) {e.printStackTrace();}
	}
	
	public void deleteUserConfig(Member member, String key, String value) {
		try {
			File propertiesFile = this.getUserConfigFile(member);
			String currentraw = this.getUserConfig(member, key);
			FileOutputStream out = new FileOutputStream(propertiesFile);
			String[] current = currentraw.split(";");
			if (current.length == 1) {
				properties.setProperty(key, "");
			} else {
				if(current[0].equals(value)) {
					properties.setProperty(key, currentraw.replace(value + ";", ""));
				} else {
					properties.setProperty(key, currentraw.replace(";" + value, ""));
				}
			}
			properties.store(out, null);
			out.close();
		} catch (Exception e) {}
	}

	private File findorCreateGuildConfig(String path) {
		File propertiesFile = new File(path);
		if (!propertiesFile.exists()) {
			try {
				propertiesFile.createNewFile();
				properties.setProperty("welcomemsg", "");
				properties.setProperty("goodbyemsg", "");
				properties.setProperty("join2create", "");
				properties.setProperty("suggest", "");
				properties.setProperty("autoroles", "");
				properties.setProperty("autobotroles", "");
				properties.setProperty("modrole", "");
				properties.setProperty("autopunish", "");
				properties.store(new FileOutputStream(propertiesFile), null);
			} catch (IOException e) {e.printStackTrace();}
		}
		return propertiesFile;
	}
	public File getGuildConfigFile(Guild guild) {
		File configFile = this.findorCreateGuildConfig("./src/main/resources/configs/guild/" + guild.getId() + ".properties");
		return configFile;
	}
	
	private File findorCreateUserConfig(String path) {
		File guilddir = new File(path);
		if (guilddir.exists() && guilddir.isDirectory()) {
		} else {
			guilddir.mkdirs();
		}
		File propertiesFile = new File(path);
		if (!propertiesFile.exists()) {
			try {
				propertiesFile.createNewFile();
				properties.setProperty("warnings", "");
				properties.setProperty("muted", "");
				properties.setProperty("tempmuted", "");
				properties.setProperty("tmuntil", "");
				properties.setProperty("banned", "");
				properties.setProperty("tempbanned", "");
				properties.setProperty("tbuntil", "");
				properties.setProperty("lastxpgotten", "");
				properties.setProperty("level", "");
				properties.setProperty("expe", "");
				properties.store(new FileOutputStream(propertiesFile), null);
			} catch (IOException e) {e.printStackTrace();}
		}
		return propertiesFile;
	}
	private File getUserConfigFile(Member member) {
		File configFile =this.findorCreateUserConfig("./src/main/resources/configs/user/" + member.getGuild().getId() + "/" + member.getUser().getId() + ".properties");
		return configFile;
	}
}
