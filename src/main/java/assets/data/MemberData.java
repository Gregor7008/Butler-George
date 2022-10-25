package assets.data;

import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import assets.data.single.WarningData;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class MemberData implements DataContainer {
    
    private Category custom_category;
    private int exeperience, last_penalty, level, levelcard_background, spam_count = 0;
    private String language = "en";
    private OffsetDateTime last_experience, last_modmail, last_suggestion, temporarily_banned_until;
    private ConcurrentHashMap<Integer, TextChannel> modmails = new ConcurrentHashMap<>();
    private boolean permanently_muted, temporarily_banned = false;
    private List<WarningData> warnings = new LinkedList<>();
    
    public MemberData(JSONObject data) {
        this.instanciateFromJSON(data);
    }
    
    public MemberData() {}
    
    @Override
    public DataContainer instanciateFromJSON(JSONObject data) {
        return null;
    }

    
    @Override
    public JSONObject compileToJSON() {
        JSONObject compiledData = new JSONObject();
        
        return compiledData;
    }
    
    private static class Key {
        public static final String GUILD_ID = "id";
        public static final String GUILD_NAME = "name";
        public static final String CUSTOM_CATEGORY = "custom_category";
        public static final String EXPERIENCE = "experience";
        public static final String LANGUAGE = "language";
        public static final String LAST_EXPERIENCE = "last_experience";
        public static final String LAST_MODMAIL = "last_modmail";
        public static final String LAST_PENALTY = "last_penalty";
        public static final String LAST_SUGGESTION = "last_suggestion";
        public static final String LEVEL = "level";
        public static final String LEVELCARD_BACKGROUND = "levelcard_background";
        public static final String MODMAILS = "modmails";
        public static final String PERMANENTLY_MUTED = "permanently_muted";
        public static final String SPAM_COUNT = "spam_count";
        public static final String TEMPORARILY_BANNED = "temporarily_banned";
        public static final String TEMPORARILY_BANNED_UNTIL = "temporarily_banned_until";
        public static final String WARNINGS = "warnings";
    }
}
