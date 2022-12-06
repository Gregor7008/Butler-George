package assets.data.single;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import assets.base.exceptions.EntityNotFoundException.ReferenceType;
import assets.data.DataContainer;
import base.Bot;
import engines.base.CentralTimer;
import engines.data.ConfigManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class GiveawayData implements DataContainer {
    
    private final Guild guild;
    private final TextChannel text_channel;
    private final User user;
    private Message message;
    private String title, description = "N/A";
    private boolean anonymous = false;
    private OffsetDateTime time_limit = OffsetDateTime.now().plusWeeks(1);
    private List<String> prizes = new ArrayList<>();
    private List<Member> sign_ups = new ArrayList<>();
    private List<Role> allowed_roles = new ArrayList<>();
    
//  Temporary runtime data
    private long timer_operation_id = 0L;

	public GiveawayData(Message message, JSONObject data) {
	    this.guild = message.getGuild();
	    this.text_channel = message.getChannel().asTextChannel();
	    this.message = message;
	    this.user = Bot.getAPI().retrieveUserById(data.getLong(Key.USER_ID)).complete();
	    this.instanciateFromJSON(data);
	}
	
	public GiveawayData(Guild guild, TextChannel channel, User user) {
	    this.guild = guild;
	    this.text_channel = channel;
	    this.user = user;
	}

    @Override
    public DataContainer instanciateFromJSON(JSONObject data) {
        this.title = data.getString(Key.TITLE);
        this.description = this.message.getEmbeds().get(0).getDescription();
        
        this.anonymous = data.getBoolean(Key.ANONYMOUS);
        
        this.time_limit = OffsetDateTime.parse(data.getString(Key.TIME_LIMIT), ConfigManager.DATA_TIME_SAVE_FORMAT);
        this.timer_operation_id = CentralTimer.get().schedule(() ->  message.editMessageEmbeds(buildEndEmbed()).setComponents().queue(), this.time_limit);
        
        JSONArray prizes_array = data.getJSONArray(Key.PRIZES);
        for (int i = 0; i < prizes_array.length(); i++) {
            this.prizes.add(prizes_array.getString(i));
        }
        
        JSONArray sign_ups_array = data.getJSONArray(Key.SIGN_UPS);
        List<Long> memberIds = new ArrayList<>();
        for (int i = 0; i < sign_ups_array.length(); i++) {
            memberIds.add(sign_ups_array.getLong(i));
        }
        this.sign_ups.addAll(guild.retrieveMembersByIds(memberIds).get());
        this.sign_ups.removeAll(Collections.singleton(null));
        
        JSONArray allowed_roles_array = data.getJSONArray(Key.ALLOWED_ROLES);
        for (int i = 0; i < allowed_roles_array.length(); i++) {
            this.allowed_roles.add(guild.getRoleById(allowed_roles_array.getLong(i)));
            this.allowed_roles.removeAll(Collections.singleton(null));
        }
        return this;
    }

    @Override
    public JSONObject compileToJSON() {
        JSONObject compiledData = new JSONObject();
        return compiledData;
    }

    @Override
    public boolean verify(ReferenceType type) {
        // TODO Auto-generated method stub
        return false;
    }

    public TextChannel getChannel() {
        return this.text_channel;
    }

    public Message getMessage() {
        return this.message;
    }
    
    public User getUser() {
        return this.user;
    }

    private MessageEmbed buildEndEmbed() {
//      TODO Implement 'buildEndEmbed()' method
        return null;
    }
    
    private static abstract class Key {
        public static final String USER_ID = "user_id";
        public static final String TITLE = "title";
        public static final String PRIZES = "prizes";
        public static final String SIGN_UPS = "sign_ups";
        public static final String ANONYMOUS = "anonymous";
        public static final String ALLOWED_ROLES = "allowed_roles";
        public static final String TIME_LIMIT = "time_limit";
    }
}