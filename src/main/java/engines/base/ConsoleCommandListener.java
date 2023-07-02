package engines.base;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import assets.data.single.WarningData;
import assets.logging.Logger;
import base.Bot;
import base.Bot.ShutdownReason;
import base.GUI;
import engines.data.ConfigLoader;
import engines.logging.ConsoleEngine;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class ConsoleCommandListener implements ActionListener {
	
	private static Logger LOG = ConsoleEngine.getLogger(ConsoleCommandListener.class);

	@Override
	public void actionPerformed(ActionEvent e) {
        String input = GUI.INSTANCE.consoleIn.getText();
		if (!Bot.isShutdown()) {
			LOG.debug("Executing command \"" + input + "\"...");
			this.processCommand(input);
		} else {
			if (input.equals("exit")) {
			    System.exit(0);
			} else {
			    LOG.warn("Input ignored as the bot is offline!");
			}
		}
		GUI.INSTANCE.consoleIn.setText("");
	}
	
	private void processCommand(String line) {
		JDA jda = Bot.getAPI();
		String[] insplit = line.split(" ");
		String command = insplit[0];
		switch (command) {
			case "stop":
				boolean sendMessage = true;
				try {
				    sendMessage = Boolean.parseBoolean(insplit[1]);
				} catch (IndexOutOfBoundsException e) {}
				GUI.INSTANCE.shutdownBot(ShutdownReason.OFFLINE, sendMessage, null);
				break;
			case "restart":
			    GUI.INSTANCE.restartBot();
			    break;
			case "exit":
				System.exit(0);
				break;
			case "warn":
				try {
				    User user = jda.retrieveUserById(insplit[2]).complete();
				    Guild guild = jda.getGuildById(insplit[1]);
					ConfigLoader.get().getMemberData(guild, user).addWarnings(WarningData.create("Administrative Action", user));
					LOG.info("User " + user.getName() + " was successfully warned on " + guild.getName());
				} catch (IndexOutOfBoundsException e) {
					LOG.error("Invalid arguments - Please try again!");
				}
				break;
			case "pushCache":
				if (ConfigLoader.get().pushCache()) {
					ConsoleEngine.getLogger(ConfigLoader.class).info("Cache successfully pushed - All changes were saved!");
				} else {
				    ConsoleEngine.getLogger(ConfigLoader.class).info("Cache push cancelled - There's nothing to push!");
				}
				break;
			case "printCache":
				ConfigLoader.get().printCache();
				break;
			case "clearCache":
				ConfigLoader.get().getGuildCache().clear();
				ConfigLoader.get().getUserCache().clear();
				ConsoleEngine.getLogger(ConfigLoader.class).info("Cache cleared successfully!");
				break;
			case "printEventAwaiter":
				EventAwaiter.INSTANCE.log();
				break;
			case "clearEventAwaiter":
				EventAwaiter.INSTANCE.clear();
				break;
			case "devtest":
			    for (int i = 0; i < 50; i++) {
			        LOG.info(String.valueOf(i) + ". *devtest message*");
			    }
			    break;
			default:
				LOG.error("Unknown command!");
		}
	}
}