package commands.moderation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import base.Configloader;
import commands.Commands;
import components.AnswerEngine;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class Autorole implements Commands{

	@Override
	public void perform(GuildMessageReceivedEvent event, String argument) {
		final File configFile = Configloader.INSTANCE.getConfigFile(event.getGuild());
		final Properties properties = new Properties();
		final Role role;
		if (argument.startsWith("add")) {
			try {
				role = event.getMessage().getMentionedRoles().get(0);
			} catch (Exception e) {
				AnswerEngine.getInstance().fetchMessage("/commands/moderation/autorole:needrole", event);
				return;
			}
			try {
			FileInputStream in = new FileInputStream(configFile);
			properties.load(in);
			String autoroles = properties.getProperty("autoroles");
			in.close();
			String newautorole = role.getId();
			String newautoroles;
			if (autoroles == "") {
				newautoroles = newautorole;
			} else {
				newautoroles = autoroles + ";" + newautorole;
			}
			FileOutputStream out = new FileOutputStream(configFile);
			properties.setProperty(autoroles, newautoroles);
			properties.store(out, null);
			out.close();
			} catch (IOException e) {e.printStackTrace();}
			AnswerEngine.getInstance().buildMessage("Role added!", ":white_check_mark: | Everytime a new member joins the " + role.getAsMention() + " Role will be given to him!", event.getChannel());
			return;
		}
		if (argument.startsWith("remove")) {
			try {
				role = event.getMessage().getMentionedRoles().get(0);
			} catch (Exception e) {
				AnswerEngine.getInstance().fetchMessage("/commands/moderation/autorole:needrole", event);
				return;
			}
			try {
				FileInputStream in = new FileInputStream(configFile);
				properties.load(in);
				String autoroles = properties.getProperty("autoroles");
				in.close();
				if (autoroles == "") {
					AnswerEngine.getInstance().fetchMessage("/commands/moderation/autorole:noautoroles", event);
					return;
				}
				String outautorole = role.getId();
				String newautoroles = autoroles.replace(outautorole + ";", "");
				FileOutputStream out = new FileOutputStream(configFile);
				properties.setProperty(autoroles, newautoroles);
				properties.store(out, null);
				out.close();
			} catch (IOException e) {e.printStackTrace();}
			AnswerEngine.getInstance().buildMessage("Role removed!", ":white_check_mark: | Everytime a new member joins the " + role.getAsMention() + " Role will no longer be given to him!", event.getChannel());
			return;
		}
		if (argument.startsWith("list")) {
			return;
		}
	}

}
