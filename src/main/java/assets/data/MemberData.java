package assets.data;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import assets.data.single.ModMailData;
import assets.data.single.WarningData;
import engines.base.LanguageEngine.Language;
import engines.data.ConfigLoader;
import engines.data.ConfigManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class MemberData implements DataContainer {
    
    private final Guild guild;
    private Category custom_category;
    private int experience, last_penalty_index, level, levelcard_background, spam_count = 0;
    private Language language = Language.ENGLISH;
    private OffsetDateTime last_experience, last_modmail, last_suggestion, temporarily_banned_until = OffsetDateTime.now().minusDays(1L);
    private ConcurrentHashMap<Integer, ModMailData> modmails = new ConcurrentHashMap<>();
    private boolean permanently_muted = false;
    private List<WarningData> warnings = new LinkedList<>();
    
    public MemberData(Guild guild, JSONObject data) {
        this.guild = guild;
        this.instanciateFromJSON(data);
    }
    
    public MemberData(Guild guild) {
        this.guild = guild;
    }
    
    @Override
    public DataContainer instanciateFromJSON(JSONObject data) {
        this.custom_category = guild.getCategoryById(data.getLong(Key.CUSTOM_CATEGORY));
        
        this.experience = data.getInt(Key.EXPERIENCE);
        this.last_penalty_index = data.getInt(Key.LAST_PENALTY_INDEX);
        this.level = data.getInt(Key.LEVEL);
        this.levelcard_background = data.getInt(Key.LEVELCARD_BACKGROUND);
        this.spam_count = data.getInt(Key.SPAM_COUNT);
        
        this.language = Language.valueOf(data.getString(Key.LANGUAGE));
        
        this.last_experience = OffsetDateTime.parse(data.getString(Key.LAST_EXPERIENCE), ConfigManager.DATA_TIME_SAVE_FORMAT);
        this.last_modmail = OffsetDateTime.parse(data.getString(Key.LAST_MODMAIL), ConfigManager.DATA_TIME_SAVE_FORMAT);
        this.last_suggestion = OffsetDateTime.parse(data.getString(Key.LAST_SUGGESTION), ConfigManager.DATA_TIME_SAVE_FORMAT);
        this.temporarily_banned_until = OffsetDateTime.parse(data.getString(Key.TEMPORARILY_BANNED_UNTIL), ConfigManager.DATA_TIME_SAVE_FORMAT);
        
        permanently_muted = data.getBoolean(Key.PERMANENTLY_MUTED);
        
        JSONObject modmails_object = data.getJSONObject(Key.MODMAILS);
        modmails_object.keySet().forEach(key -> {
           TextChannel channel = guild.getTextChannelById(modmails_object.getLong(key));
           if (channel != null) {
               modmails.put(Integer.parseInt(key), ConfigLoader.INSTANCE.getGuildData(guild).getModMail(channel));
           }
        });
        modmails.values().removeAll(Collections.singleton(null));
        
        JSONArray warnings_array = data.getJSONArray(Key.WARNINGS);
        for (int i = 0; i < warnings_array.length(); i++) {
            JSONObject warning_object = warnings_array.getJSONObject(i);
            warnings.add(new WarningData(warning_object));
        }
        
        return this;
    }
    
    @Override
    public JSONObject compileToJSON() {
        JSONObject compiledData = new JSONObject();
        
        compiledData.put(Key.GUILD_NAME, guild.getName());
        
        compiledData.put(Key.CUSTOM_CATEGORY, custom_category.getIdLong());
        
        compiledData.put(Key.EXPERIENCE, experience);
        compiledData.put(Key.LAST_PENALTY_INDEX, last_penalty_index);
        compiledData.put(Key.LEVEL, level);
        compiledData.put(Key.LEVELCARD_BACKGROUND, levelcard_background);
        compiledData.put(Key.SPAM_COUNT, spam_count);
        
        compiledData.put(Key.LANGUAGE, language.toString());
        
        compiledData.put(Key.LAST_EXPERIENCE, last_experience.format(ConfigManager.DATA_TIME_SAVE_FORMAT));
        compiledData.put(Key.LAST_MODMAIL, last_modmail.format(ConfigManager.DATA_TIME_SAVE_FORMAT));
        compiledData.put(Key.LAST_SUGGESTION, last_suggestion.format(ConfigManager.DATA_TIME_SAVE_FORMAT));
        compiledData.put(Key.TEMPORARILY_BANNED_UNTIL, temporarily_banned_until.format(ConfigManager.DATA_TIME_SAVE_FORMAT));
        
        compiledData.put(Key.PERMANENTLY_MUTED, permanently_muted);
        
        JSONObject modmails_data = new JSONObject();
        modmails.forEach((ticket_id, modmail) -> modmails_data.put(String.valueOf(ticket_id), modmail.getGuildChannel().getIdLong()));
        compiledData.put(Key.MODMAILS, modmails_data);
        
        JSONArray warnings_data = new JSONArray();
        for (int i = 0; i < warnings.size(); i++) {
            warnings_data.put(i, warnings.get(i).compileToJSON());
        }
        compiledData.put(Key.WARNINGS, warnings_data);
        
        return compiledData;
    }
    
    public Guild getGuild() {
        return this.guild;
    }
    
    public Category getCustomCategory() {
        return this.custom_category;
    }
    
    public MemberData setCustomCategory(Category custom_category) {
        this.custom_category = custom_category;
        return this;
    }
    
    public int getExperience() {
        return this.experience;
    }
    
    public MemberData setExperience(int experience) {
        this.experience = experience;
        return this;
    }
    
    public MemberData addToExperience(int additional_experience) {
        this.experience += additional_experience;
        return this;
    }
    
    public MemberData removeFromExperience(int deductional_experience) {
        this.experience -= deductional_experience;
        return this;
    }
    
    public int getLastPenaltyIndex() {
        return this.last_penalty_index;
    }
    
    public MemberData setLastPenaltyIndex(int index) {
        this.last_penalty_index = index;
        return this;
    }
    
    public MemberData addToLastPenaltyIndex(int additional_indicies) {
        this.last_penalty_index += additional_indicies;
        return this;
    }
    
    public MemberData removeFromLastPenaltyIndex(int deductional_indicies) {
        this.last_penalty_index -= deductional_indicies;
        return this;
    }
    
    public int getLevel() {
        return this.level;
    }
    
    public MemberData setLevel(int index) {
        this.level = index;
        return this;
    }
    
    public MemberData addToLevel(int additional_levels) {
        this.level += additional_levels;
        return this;
    }
    
    public MemberData removeFromLevel(int deductional_levels) {
        this.level -= deductional_levels;
        return this;
    }
    
    public int getLevelCardBackground() {
        return this.levelcard_background;
    }
    
    public MemberData setLevelCardBackground(int index) {
        this.level = index;
        return this;
    }
    
    public MemberData addToLevelCardBackground(int additional_levels) {
        this.level += additional_levels;
        return this;
    }
    
    public MemberData removeFromLevelCardBackground(int deductional_levels) {
        this.level -= deductional_levels;
        return this;
    }
    
    public int getSpamCount() {
        return this.spam_count;
    }
    
    public MemberData setSpamCount(int index) {
        this.spam_count = index;
        return this;
    }
    
    public MemberData addToSpamCount(int additional_count) {
        this.spam_count += additional_count;
        return this;
    }
    
    public MemberData removeFromSpamCount(int deductional_count) {
        this.spam_count -= deductional_count;
        return this;
    }
    
    public Language getLanguage() {
        return this.language;
    }
    
    public MemberData setLanguage(Language language) {
        this.language = language;
        return this;
    }
    
    public OffsetDateTime getLastExperience() {
        return last_experience;
    }
    
    public MemberData setLastExperience(OffsetDateTime last_experience) {
        this.last_experience = last_experience;
        return this;
    }
    
    public MemberData updateLastExperience() {
        this.last_experience = OffsetDateTime.now();
        return this;
    }
    
    public OffsetDateTime getLastModmail() {
        return last_modmail;
    }
    
    public MemberData setLastModmail(OffsetDateTime last_modmail) {
        this.last_modmail = last_modmail;
        return this;
    }
    
    public MemberData updateLastModmail() {
        this.last_modmail = OffsetDateTime.now();
        return this;
    }
    
    public OffsetDateTime getLastSuggestion() {
        return last_suggestion;
    }
    
    public MemberData setLastSuggestion(OffsetDateTime last_suggestion) {
        this.last_suggestion = last_suggestion;
        return this;
    }
    
    public MemberData updateLastSuggestion() {
        this.last_suggestion = OffsetDateTime.now();
        return this;
    }
    
    public OffsetDateTime isTemporaryBannedUntil() {
        return temporarily_banned_until;
    }
    
    public MemberData setTemporaryBannedUntil(OffsetDateTime temporarily_banned_until) {
        this.temporarily_banned_until = temporarily_banned_until;
        return this;
    }
    
    public ConcurrentHashMap<Integer, ModMailData> getModmails() {
        return this.modmails;
    }
    
    public ModMailData getModmail(int ticket_id) {
        return this.modmails.get(ticket_id);
    }
    
    public MemberData setModmails(ConcurrentHashMap<Integer, ModMailData> modmails) {
        DataTools.setMap(this.modmails, modmails);
        return this;
    }
    
    public MemberData addModmails(ModMailData... datas) {
        for (int i = 0; i < datas.length; i++) {
            modmails.put(datas[i].getTicketId(), datas[i]);
        }
        return this;
    }
    
    public MemberData removeModmails(int... ticket_ids) {
        for (int i = 0; i < ticket_ids.length; i++) {
            modmails.remove(ticket_ids[i]);
        }
        return this;
    }
    
    public MemberData removeModmailsByData(ModMailData... datas) {
        for (int i = 0; i < datas.length; i++) {
            modmails.remove(datas[i].getTicketId());
        }
        return this;
    }
    
    public boolean isPermanentlyMuted() {
        return permanently_muted;
    }
    
    public MemberData setPermanentlyMuted(boolean permanently_muted) {
        this.permanently_muted = permanently_muted;
        return this;
    }
    
    public List<WarningData> getWarnings() {
        return this.warnings;
    }
    
    public WarningData getWarning(int index) {
        return warnings.get(index);
    }
    
    public MemberData setWarnings(List<WarningData> warnings) {
        DataTools.setList(this.warnings, warnings);
        return this;
    }

    public MemberData addWarnings(WarningData... datas) {
        DataTools.addToList(this.warnings, datas);
        return this;
    }

    public MemberData removeWarnings(int... indicies) {
        DataTools.removeFromList(this.warnings, indicies);
        return this;
    }

    public MemberData removeWarningsByData(WarningData... datas) {
        DataTools.removeFromList(this.warnings, datas);
        return this;
    }
    
    private static abstract class Key {
        public static final String GUILD_NAME = "name";
        public static final String CUSTOM_CATEGORY = "custom_category";
        public static final String EXPERIENCE = "experience";
        public static final String LANGUAGE = "language";
        public static final String LAST_EXPERIENCE = "last_experience";
        public static final String LAST_MODMAIL = "last_modmail";
        public static final String LAST_PENALTY_INDEX = "last_penalty_index";
        public static final String LAST_SUGGESTION = "last_suggestion";
        public static final String LEVEL = "level";
        public static final String LEVELCARD_BACKGROUND = "levelcard_background";
        public static final String MODMAILS = "modmails";
        public static final String PERMANENTLY_MUTED = "permanently_muted";
        public static final String SPAM_COUNT = "spam_count";
        public static final String TEMPORARILY_BANNED_UNTIL = "temporarily_banned_until";
        public static final String WARNINGS = "warnings";
    }
}