package base;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;

import assets.data.single.Join2CreateChannelData;
import assets.data.single.ModMailData;
import assets.logging.Logger;
import engines.base.CentralTimer;
import engines.base.EventAwaiter;
import engines.base.LanguageEngine;
import engines.base.Toolbox;
import engines.data.ConfigLoader;
import engines.functions.ModController;
import engines.logging.ConsoleEngine;
import functions.configuration_options.ServerConfigurationOptionsList;
import functions.configuration_options.UserConfigurationOptionsList;
import functions.context_menu_commands.MessageContextCommandList;
import functions.context_menu_commands.UserContextCommandList;
import functions.guild_utilities.GuildUtilitiesList;
import functions.slash_commands.SlashCommandList;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class Bot {
	
	public static String VERSION = "V2.1-dev.0";
	public static String NAME = "Butler George";
	public static String ID = "853887837823959041";
	public static String HOME = "708381749826289666";

    private static Bot INSTANCE;
	private static Logger LOG = ConsoleEngine.getLogger(Bot.class);
	
    private JDA jda;
	private Thread shutdownThread = null;
	private boolean shutdown = false;
	
	public static Bot get() {
	    if (Bot.INSTANCE != null) {
	        return Bot.INSTANCE;
	    } else {
	        return null;
	    }
	}
	
	public static JDA getAPI() {
	    if (Bot.isShutdown()) {
            return null;
        } else {
            return Bot.getAPI();
        }
	}
    
    public static boolean isShutdown() {
        if (Bot.get() != null) {
            return Bot.get().shutdown;
        } else {
            return true;
        }
    }
	
	public Bot() throws InterruptedException {
	    INSTANCE = this;
//	    List Creation
        ServerConfigurationOptionsList.create();
        UserConfigurationOptionsList.create();
        MessageContextCommandList.create();
        UserContextCommandList.create();
        GuildUtilitiesList.create();
        SlashCommandList.create();
        LOG.debug("Successfully created lists");
//      API Login
		JDABuilder builder = JDABuilder.createDefault(ConfigLoader.get().getSystemData().getBotToken());
		builder.addEventListeners(new EventProcessor(), new EventAwaiter());
		Object[] serverUtils = GuildUtilitiesList.getEngines().values().toArray();
		builder.addEventListeners(serverUtils);
		builder.setRawEventsEnabled(true);
		builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.MESSAGE_CONTENT);
		builder.setMemberCachePolicy(MemberCachePolicy.ALL);
    	jda = builder.build().awaitReady();
		jda.getPresence().setStatus(OnlineStatus.ONLINE);	    
	    jda.getPresence().setActivity(Activity.playing(VERSION));
	    LOG.debug("Bot online");
//      Setup Runtime
        Runtime.getRuntime().addShutdownHook(this.getShutdownThread());
        Thread.setDefaultUncaughtExceptionHandler(ConsoleEngine.getInstance());
//      Startup Engines
        new ModController();
//      GUI Udates
        GUI.INSTANCE.setBotRunning(true);
        GUI.INSTANCE.updateStatistics();
        GUI.INSTANCE.startRuntimeMeasuring();
//      Startup Operations
        GuildUtilitiesList.getEngines().forEach((id, handler) -> handler.onStartup());
        this.processMissedModMailMessages();
//      Debug Logging
        LOG.debug("Post-Startup operations completed");
	}
	
	private void processMissedModMailMessages() {
		List<Guild> guilds = jda.getGuilds();
    	List<Long> processedUserIds = new ArrayList<>();
    	for (int i = 0; i < guilds.size(); i++) {
    		final Guild guild = guilds.get(i);
    		final ConcurrentHashMap<Long, ModMailData> modmailsData = ConfigLoader.get().getGuildData(guild).getModmailIds();
    		modmailsData.keySet().forEach(ticketChannelId -> {
    			final ModMailData modmailData = modmailsData.get(ticketChannelId);
    			final long ticketOwner = modmailData.getUserId();
                final ModMailData selectedTicket = ConfigLoader.get().getUserData(ticketOwner).getSelectedModMail();
    			if (!processedUserIds.contains(ticketOwner)) {
//    				Process users messages to selected ticket
    				List<Message> messagesByUser = null;
    				try {
    					messagesByUser = modmailData.getUser().openPrivateChannel().complete().getHistoryAfter(selectedTicket.getLastUserMessageId(), 100).complete().getRetrievedHistory();
    				} catch (JSONException e) {}
    				if (messagesByUser != null) {
    					for (int a = messagesByUser.size() - 1 ; a >= 0 ; a--) {
    					    Message message = messagesByUser.get(a);
    						if (!message.getAuthor().isBot() && message.getIdLong() != selectedTicket.getLastUserMessageId()) {
    							Toolbox.forwardMessage(selectedTicket.getGuildChannel(), message);
    						}
    					}
    				}
//    				Process servers messages to user, if the user has the ticket selected
    				if (guild.getIdLong() == selectedTicket.getGuildId() && modmailData.getTicketId() == selectedTicket.getTicketId()) {
    					List<Message> messagesByServer = null;
    					try {
    						messagesByServer = modmailData.getGuildChannel().getHistoryAfter(modmailData.getLastGuildMessageId(), 100).complete().getRetrievedHistory();
    					} catch (JSONException e) {}
    					if (messagesByServer != null) {
    						for (int a = messagesByServer.size() - 1 ; a >= 0 ; a--) {
    						    Message message = messagesByServer.get(a);
    							if (!message.getAuthor().isBot() && message.getIdLong() != modmailData.getLastGuildMessageId()) {
    								Toolbox.forwardMessage(modmailData.getUser().openPrivateChannel().complete(), message);
    							}
    						}
    					}
    				}
    				processedUserIds.add(ticketOwner);
    			}
    		});
    	}
	}
	
	public void shutdown(ShutdownReason reason, boolean sendMessage, @Nullable String additionalMessage, @Nullable Runnable followUp) {
		List<Guild> guilds = jda.getGuilds();
		for (int i = 0; i < guilds.size(); i++) {
    		Guild guild = guilds.get(i);
//    		Delete channels created with Join2Create channels
    		ConcurrentHashMap<Long, Join2CreateChannelData> join2createChannels = ConfigLoader.get().getGuildData(guild).getJoin2CreateChannelDataIds();
    		join2createChannels.forEach((channel_id, data) -> {
    		    List<Long> createdChannels = data.getChildrenIds();
                if (!createdChannels.isEmpty()) {
                    createdChannels.forEach(channel -> guild.getVoiceChannelById(channel).delete().queue());
                }
    		});
//    		Send offline message
    		if (sendMessage) {
    		    TextChannel communityInbox = ConfigLoader.get().getGuildData(guild).getCommunityInboxChannel();
                if (communityInbox == null) {
                    communityInbox = guild.getTextChannels().stream().filter(c -> {return guild.getSelfMember().hasPermission(c, Permission.MESSAGE_SEND);}).toList().get(0);
                }
                StringBuilder offlineMessageBuilder = new StringBuilder();
                offlineMessageBuilder.append(LanguageEngine.getRaw(guild, null, this, reason.toString().toLowerCase()));
                offlineMessageBuilder.append(LanguageEngine.getRaw(guild, null, this, "information"));
                if (additionalMessage != null && !additionalMessage.isBlank()) {
                    offlineMessageBuilder.append(LanguageEngine.getRaw(guild, null, this, "addonpresent"));
                    offlineMessageBuilder.append(additionalMessage);
                }
                Message message = communityInbox.sendMessageEmbeds(LanguageEngine.buildMessageEmbed(offlineMessageBuilder.toString())).complete();
                ConfigLoader.get().getGuildData(guild).setOfflineMessage(message);
    		}
    	}
//		Stop period operations
		EventAwaiter.INSTANCE.clear();
		CentralTimer.get().cancelAll();
//		Shutdown operations
		GuildUtilitiesList.getEngines().forEach((id, handler) -> handler.onShutdown(reason));
//		Shutdown bot
		jda.getPresence().setStatus(OnlineStatus.OFFLINE);
		jda.shutdown();
		GUI.INSTANCE.setBotRunning(false);
		GUI.INSTANCE.stopRuntimeMeasuring();
		ConfigLoader.get().pushCache();
		shutdown = true;
//		Debug logging
		LOG.debug("Bot offline");
//		Follow Up
		if (followUp != null) {
	        followUp.run();
		}
	}
    
    public void kill() {
        CentralTimer.get().cancelAll();
        INSTANCE = null;
        this.shutdown = true;
    }
	
	public Thread getShutdownThread() {
		if (shutdownThread == null) {
			shutdownThread = new Thread(() -> {
				if (!shutdown) {
					shutdown(ShutdownReason.OFFLINE, true, null, null);
				}
			});
		}
		return shutdownThread;
	}
	
	public static enum ShutdownReason {
		OFFLINE,
		MAINTENANCE,
		RESTART,
		FATAL_ERROR;
	}
}