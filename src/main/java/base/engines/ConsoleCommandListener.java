package base.engines;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import base.Bot;
import base.GUI;
import base.Bot.ShutdownReason;
import base.engines.configs.ConfigLoader;
import base.engines.logging.ConsoleEngine;
import base.engines.logging.Logger;
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
				ConfigLoader.INSTANCE.getMemberConfig(jda.getGuildById(insplit[1]), jda.getUserById(insplit[2])).getJSONArray("warnings").put("Administrative actions");
				LOG.info("User " + jda.retrieveUserById(insplit[2]).complete().getName() + " was successfully warned on " + jda.getGuildById(insplit[1]).getName());
				break;
			case "pushCache":
				if (ConfigLoader.INSTANCE.manager.pushCache()) {
					LOG.info("Cache successfully pushed");
				}
				break;
			case "printCache":
				LOG.title("User-Cache");
				ConfigLoader.INSTANCE.manager.getUserCache().forEach((id, obj) -> {
					LOG.info("->" + Bot.INSTANCE.jda.getUserById(id).getName());
				});
				if (ConfigLoader.INSTANCE.manager.getUserCache().isEmpty()) {
					LOG.info("EMPTY");
				}
				LOG.title("Guild-Cache");
				ConfigLoader.INSTANCE.manager.getGuildCache().forEach((id, obj) -> {
					LOG.info("->" + Bot.INSTANCE.jda.getGuildById(id).getName());
				});
				if (ConfigLoader.INSTANCE.manager.getGuildCache().isEmpty()) {
					LOG.info("EMPTY");
				}
				break;
			case "clearCache":
				ConfigLoader.INSTANCE.manager.getGuildCache().clear();
				ConfigLoader.INSTANCE.manager.getUserCache().clear();
				LOG.info("Cache cleared successfully!");
				break;
			default:
				LOG.error("Unknown command!");
		}
	}
}