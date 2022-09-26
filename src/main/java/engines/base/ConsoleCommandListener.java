package engines.base;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import assets.logging.Logger;
import base.Bot;
import base.Bot.ShutdownReason;
import base.GUI;
import engines.configs.ConfigLoader;
import engines.configs.ConfigManager;
import engines.logging.ConsoleEngine;
import net.dv8tion.jda.api.JDA;

public class ConsoleCommandListener implements ActionListener {
	
	private static Logger LOG = ConsoleEngine.getLogger(ConsoleCommandListener.class);

	@Override
	public void actionPerformed(ActionEvent e) {
		if (Bot.INSTANCE != null && !Bot.INSTANCE.isShutdown()) {
			String input = GUI.INSTANCE.consoleIn.getText();
			LOG.debug("Executing command \"" + input + "\"...");
			this.processCommand(input);
		} else {
			LOG.warn("Input ignored as the bot is offline!");
		}
		GUI.INSTANCE.consoleIn.setText("");
	}
	
	private void processCommand(String line) {
		JDA jda = Bot.INSTANCE.jda;
		String[] insplit = line.split(" ");
		String command = insplit[0];
		switch (command) {
			case "stop":
				GUI.INSTANCE.shutdownBot(ShutdownReason.OFFLINE, null);
				break;
			case "exit":
				System.exit(0);
				break;
			case "warn":
				try {
					ConfigLoader.INSTANCE.getMemberConfig(jda.getGuildById(insplit[1]), jda.getUserById(insplit[2])).getJSONArray("warnings").put("Administrative actions");
					LOG.info("User " + jda.retrieveUserById(insplit[2]).complete().getName() + " was successfully warned on " + jda.getGuildById(insplit[1]).getName());
				} catch (IndexOutOfBoundsException e) {
					LOG.error("Invalid arguments - Please try again!");
				}
				break;
			case "pushCache":
				if (ConfigLoader.INSTANCE.manager.pushCache()) {
					ConsoleEngine.getLogger(ConfigManager.class).info("Cache successfully pushed");
				}
				break;
			case "printCache":
				ConfigLoader.INSTANCE.manager.log();
				break;
			case "clearCache":
				ConfigLoader.INSTANCE.manager.getGuildCache().clear();
				ConfigLoader.INSTANCE.manager.getUserCache().clear();
				ConsoleEngine.getLogger(ConfigManager.class).info("Cache cleared successfully!");
				break;
			case "printEventAwaiter":
				EventAwaiter.INSTANCE.log();
				break;
			case "clearEventAwaiter":
				EventAwaiter.INSTANCE.clear();
				break;
			default:
				LOG.error("Unknown command!");
		}
	}
}