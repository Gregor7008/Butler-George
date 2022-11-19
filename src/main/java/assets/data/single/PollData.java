package assets.data.single;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import assets.data.DataContainer;
import assets.data.DataTools;
import base.Bot;
import engines.base.Check;
import engines.base.Toolbox;
import engines.data.ConfigManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class PollData implements DataContainer {
    
    private final Guild guild;
    private final TextChannel text_channel;
    private final Message message;
    private final User user;
    private final PollType type;
    private String title = "N/A";
    private boolean anonymous, public_results = false;
    private OffsetDateTime time_limit = OffsetDateTime.now().plusWeeks(1);
    private List<String> options = new LinkedList<>();
    private List<Role> allowed_roles = new LinkedList<>();
    private ConcurrentHashMap<Member, int[]> votes = new ConcurrentHashMap<>();

	public PollData(Message message, JSONObject data) {
        this.guild = message.getGuild();
	    this.text_channel = message.getChannel().asTextChannel();
	    this.message = message;
	    this.user = Bot.INSTANCE.jda.retrieveUserById(data.getLong(Key.USER_ID)).complete();
	    this.type = PollType.valueOf(data.getString(Key.TYPE));
	    this.instanciateFromJSON(data);
	}
	
	public PollData(Message message, User user, PollType type) {
        this.guild = message.getGuild();
	    this.text_channel = message.getChannel().asTextChannel();
	    this.message = message;
	    this.user = user;
	    this.type = type;
	}

    @Override
    public DataContainer instanciateFromJSON(JSONObject data) {
        this.title = data.getString(Key.TITLE);
        
        this.anonymous = data.getBoolean(Key.ANONYMOUS);
        this.public_results = data.getBoolean(Key.PUBLIC_RESULTS);
        
        this.time_limit = OffsetDateTime.parse(data.getString(Key.TIME_LIMIT), ConfigManager.DATA_TIME_SAVE_FORMAT);
        
        JSONArray options_array = data.getJSONArray(Key.OPTIONS);
        for (int i = 0; i < options_array.length(); i++) {
            this.options.add(options_array.getString(i));
        }
        
        JSONArray allowed_roles_array = data.getJSONArray(Key.ALLOWED_ROLES);
        for (int i = 0; i < allowed_roles_array.length(); i++) {
            this.allowed_roles.add(guild.getRoleById(allowed_roles_array.getLong(i)));
            this.allowed_roles.removeAll(Collections.singleton(null));
        }
        
        JSONObject votes_object = data.getJSONObject(Key.VOTES);
        votes_object.keySet().forEach(key -> {
            JSONArray votes_array = votes_object.getJSONArray(key);
            int[] member_votes = new int[votes_array.length()];
            for (int i = 0; i < votes_array.length(); i++) {
                member_votes[i] = votes_array.getInt(i);
            }
            votes.put(guild.retrieveMemberById(key).complete(), member_votes);
        });
        votes.keySet().removeAll(Collections.singleton(null));
        
        return this;
    }

    @Override
    public JSONObject compileToJSON() {
        JSONObject compiledData = new JSONObject();
        
        compiledData.put(Key.USER_ID, user.getIdLong());
        compiledData.put(Key.TYPE, type.toString());
        
        compiledData.put(Key.TITLE, title);
        compiledData.put(Key.ANONYMOUS, anonymous);
        compiledData.put(Key.PUBLIC_RESULTS, public_results);
        
        compiledData.put(Key.TIME_LIMIT, time_limit.format(ConfigManager.DATA_TIME_SAVE_FORMAT));
        
        JSONArray options_data = new JSONArray();
        for (int i = 0; i < options.size(); i++) {
            options_data.put(i, options.get(i));
        }
        compiledData.put(Key.OPTIONS, options_data);
        
        JSONArray allowed_roles_data = new JSONArray();
        allowed_roles.forEach(role -> {
            if (Check.isValidRole(role)) {
                allowed_roles_data.put(role.getIdLong());
            }
        });
        compiledData.put(Key.ALLOWED_ROLES, allowed_roles_data);
        
        JSONObject votes_data = new JSONObject();
        Toolbox.filterValidMembers(votes.keySet(), guild);
        votes.forEach((member, member_votes) -> {
            votes_data.put(member.getId(), new JSONArray(member_votes));
        });
        compiledData.put(Key.VOTES, votes_data);
        
        return compiledData;
    }
    
    public Guild getGuild() {
        return guild;
    }

    public TextChannel getChannel() {
        return this.text_channel;
    }

    public Message getMessage() {
        return this.message;
    }
    
    public User getOwner() {
        return this.user;
    }
    
    public PollType getType() {
        return this.type;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public PollData setTitle(String title) {
        this.title = title;
        return this;
    }
    
    public boolean isAnonymous() {
        return this.anonymous;
    }
    
    public PollData setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
        return this;
    }
    
    public boolean areResultsPublic() {
        return this.public_results;
    }
    
    public PollData setResultsPublic(boolean public_results) {
        this.public_results = public_results;
        return this;
    }
    
    public OffsetDateTime getTimeLimit() {
        return this.time_limit;
    }
    
    public PollData setTimeLimit(OffsetDateTime time_limit) {
        this.time_limit = time_limit;
        return this;
    }
    
    public List<String> getOptions() {
        return this.options;
    }
    
    public PollData setOptions(List<String> options) {
        DataTools.setList(this.options, options);
        return this;
    }
    
    public PollData addOptions(String... options) {
        DataTools.addToList(this.options, options);
        return this;
    }
    
    public PollData removeOptions(int... indicies) {
        DataTools.removeFromList(this.options, indicies);
        return this;
    }
    
    public PollData removeOptionsByOption(String... options) {
        DataTools.removeFromList(this.options, options);
        return this;
    }
    
    private static abstract class Key {
        public static final String USER_ID = "user_id";
        public static final String TYPE = "type";
        public static final String TITLE = "title";
        public static final String OPTIONS = "options";
        public static final String VOTES = "votes";
        public static final String ANONYMOUS = "anomyous";
        public static final String ALLOWED_ROLES = "allowed_roles";
        public static final String PUBLIC_RESULTS = "public_results";
        public static final String TIME_LIMIT = "time_limit";
    }

    public static enum PollType {
        MULTIPLE_CHOICE,
        SINGLE_CHOICE,
        TRUE_OR_FALSE;
    }
}