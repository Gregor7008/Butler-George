package base;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.dv8tion.jda.api.entities.Guild;

public class Configloader {
	
	public static Configloader INSTANCE;
	private Properties properties;
	
	
	public Configloader() {
		INSTANCE = this;
		properties = new Properties();
	}
	
	public String getConfigs(Guild guild, String key) {
		File propertiesFile = new File("./src/main/resources/configs/" + guild.getId() + ".properties");
		if (!propertiesFile.exists()) {
			try {
				propertiesFile.createNewFile();
				properties.setProperty("prefix", "#");
				properties.setProperty("welcomemsg", "");
				properties.setProperty("goodbyemsg", "");
				properties.setProperty("join2create", "");
				properties.setProperty("suggest", "");
				properties.setProperty("autoroles", "");
				properties.setProperty("autobotroles", "");
				properties.store(new FileOutputStream(propertiesFile), null);
			} catch (IOException e) {e.printStackTrace();}
		}
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(propertiesFile))) {
		  properties.load(bis);
		} catch (Exception e) {}
		return properties.getProperty(key);
	}
	public File getConfigFile(Guild guild) {
		File configFile = new File("./src/main/resources/configs/" + guild.getId() + ".properties");
		return configFile;
	}

}
