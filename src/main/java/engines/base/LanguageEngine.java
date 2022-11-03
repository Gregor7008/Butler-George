package engines.base;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import assets.base.MutableMessageEmbed;
import assets.logging.Logger;
import base.Bot;
import engines.logging.ConsoleEngine;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public abstract class LanguageEngine {
	
	public static DateTimeFormatter DEFAULT_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyy - HH:mm");
	public static DateTimeFormatter SHORTENED_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.");
	public static String SEPERATOR = "; ";
	public static int EMBED_DEFAULT_COLOR = 56575;
	
	private static Logger LOG = ConsoleEngine.getLogger(LanguageEngine.class);
	
	public static MutableMessageEmbed getMessageEmbed(Guild guild, User user, Object requester, String key)  {
		String raw = getRaw(guild, user, requester, key);
	    return LanguageEngine.buildMessageEmbed(raw.split(SEPERATOR));
	}
	
	public static String getMessage(Guild guild, User user, Object requester, String key) {
	    String raw = getRaw(guild, user, requester, key);
	    return LanguageEngine.buildMessage(raw.split(SEPERATOR));
	}
	
	public static String getTitle(Guild guild, User user, Object requester, String key) {
		return LanguageEngine.getRaw(guild, user, requester, key).split(SEPERATOR)[0];
	}

	public static String getDescription(Guild guild, User user, Object requester, String key) {
		try {
		    return LanguageEngine.getRaw(guild, user, requester, key).split(SEPERATOR)[1];
		} catch (IndexOutOfBoundsException e) {
		    return null;
		}
	}
	
	public static String getFooter(Guild guild, User user, Object requester, String key) {
	    try {
	        return buildFooter(LanguageEngine.getRaw(guild, user, requester, key).split(SEPERATOR)[2]);
	    } catch (IndexOutOfBoundsException e) {
	        return null;
	    }
	}
	
	public static String getRaw(Guild guild, User user, Object requester, String key) {
		Language language = Language.ENGLISH;
//		if (user != null && guild != null) {
//		    language = ConfigLoader.INSTANCE.getMemberData(guild, user).getLanguage();
//		}
		return LanguageEngine.getRaw(language, requester, key);
	}
	
	public static String getRaw(Language language, Object requester, String key) {
	    String path = "general";
        if (requester != null) {
            String requesterName = "";
            if (requester instanceof Class) {
                Class<?> castedRequester = (Class<?>) requester;
                requesterName = castedRequester.getName();
            } else {
                requesterName = requester.getClass().getName();
            }
            path = requesterName.replace('.', '/').replaceAll("\\$[0-9]+", "").toLowerCase();
        }
        String fullpath = "languages/" + language.toString().toLowerCase() + "/" + path + ".properties";
        Properties properties = new Properties();
        try {
            properties.load(LanguageEngine.class.getClassLoader().getResourceAsStream(fullpath));
            String temp2 = properties.getProperty(key);
            if (temp2 == null) {
                throw new NullPointerException();
            } else {
                return temp2;
            }
        } catch (NullPointerException | IOException e) {
            LOG.error("Couldn't find language file for " + fullpath + ":" + key);
            return "Error!; :x: | Couldn't find language files!\nContact support immediately!";
        }
	}
    
    public static MutableMessageEmbed buildMessageEmbed(String... args) {
        args = processArguments(args);
        EmbedBuilder eb = new EmbedBuilder();
        if (args[0] != null) {
            eb.setTitle(args[0]);
        }
        eb.setColor(EMBED_DEFAULT_COLOR);
        if (args.length > 1 && args[1] != null) {
            eb.setDescription(args[1]);
        }
        String footer = "";
        if (args.length > 2 && args[2] != null) {
            footer = args[2];
        } else {
            footer = Bot.DEFAULT_FOOTER;
        }
        eb.setFooter(buildFooter(footer));
        return new MutableMessageEmbed(eb.build());
    }
    
    public static String buildMessage(String... args) {
        args = processArguments(args);
        StringBuilder sB = new StringBuilder();
        if (args[0] != null) {
            sB.append("**" + args[0] + "**");
            if (args.length >= 2) {
                sB.append("\n");
            }
        }
        if (args.length > 1 && args[1] != null) {
            sB.append(args[1]);
        }
        String footer = "";
        if (args.length > 2 && args[2] != null) {
            footer = args[2];
        } else {
            footer = Bot.DEFAULT_FOOTER;
        }
        sB.append("\n\n" + buildFooter(footer));
        return sB.toString();
    }
    
    public static String buildFooter(String raw_footer) {
        return raw_footer.replace("{time}", OffsetDateTime.now().format(LanguageEngine.SHORTENED_TIME_FORMAT))
                    .replace("{bot_name}", Bot.NAME)
                    .replace("{bot_version}", Bot.VERSION);
    }
    
    private static String[] processArguments(String[] args) {
        if (args.length == 1 && args[0].contains(SEPERATOR)) {
            args = args[0].split(SEPERATOR);
        }
        List<String> arguments = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i] != null && !args[i].isBlank()) {
                arguments.add(args[i]);
            }
        }
        if (arguments.size() < 1) {
            throw new IllegalArgumentException("At least one message part has to be provided!");
        }
        return args;
    }
	
	public static enum Language {
	   ENGLISH,
	   GERMAN,
	   SPANISH,
	   FRENCH,
	   DUTCH;
	}
}