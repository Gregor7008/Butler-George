package commands.moderation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import base.Configloader;
import commands.Commands;
import components.AnswerEngine;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class SetPrefix implements Commands{

	@Override
	public void perform(GuildMessageReceivedEvent event, String argument) {
		final File configFile = Configloader.INSTANCE.getConfigFile(event.getGuild());
		final Properties properties = new Properties();
		FileInputStream in;
		if (argument == "") {
			AnswerEngine.getInstance().fetchMessage("/commands/moderation/setprefix:noargs", event);
			return;
		}
		try {
			in = new FileInputStream(configFile);
			properties.load(in);
			in.close();
		} catch (Exception e) {return;}
		try {
			FileOutputStream out = new FileOutputStream(configFile);
			properties.setProperty("prefix", argument);
			properties.store(out, null);
			out.close();
		} catch (Exception e) {return;}
		AnswerEngine.getInstance().buildMessage("Prefix set!", ":white_check_mark: | Your prefix was successfully set to" + argument, event.getChannel());
	}

}
