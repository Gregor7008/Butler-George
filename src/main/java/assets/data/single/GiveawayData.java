package assets.data.single;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import assets.base.exceptions.EntityNotFoundException;
import assets.base.exceptions.EntityNotFoundException.ReferenceType;
import assets.data.DataTools;
import assets.data.MessageConnection;
import base.Bot;
import engines.base.CentralTimer;
import engines.base.Check;
import engines.data.ConfigLoader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

public class GiveawayData implements MessageConnection {

    private final long guild_id, text_channel_id, user_id;
    private long message_id;
    private String title, description = "N/A";
    private boolean anonymous = false;
    private OffsetDateTime time_limit = OffsetDateTime.now().plusWeeks(1);
    private List<String> prizes = new ArrayList<>();
    private List<Long> sign_ups = new ArrayList<>(); // List<MemberId>
    private List<Long> allowed_roles = new ArrayList<>(); // List<RoleId>

//  Temporary runtime data
    private long timer_operation_id = 0L;

    public GiveawayData(TextChannel channel, Message message, JSONObject data) throws EntityNotFoundException {
        this.guild_id = message.getGuild().getIdLong();
        this.text_channel_id = message.getChannel().asTextChannel().getIdLong();
        this.message_id = message.getIdLong();
        long raw_user_id = data.getLong(Key.USER_ID);
        try {
            this.user_id = Bot.getAPI().retrieveUserById(raw_user_id).complete().getIdLong();
        } catch (ErrorResponseException | NullPointerException e) {
            throw new EntityNotFoundException(ReferenceType.USER, e, raw_user_id);
        }
        this.instanciateFromJSON(data);
    }

    public GiveawayData(Guild guild, Message message, User user) {
        this.guild_id = message.getGuild().getIdLong();
        this.text_channel_id = message.getChannel().asTextChannel().getIdLong();
        this.message_id = message.getIdLong();
        this.user_id = user.getIdLong();
    }

    public GiveawayData instanciateFromJSON(JSONObject data) {
        this.title = data.getString(Key.TITLE);
        this.description = data.getString(Key.DESCRIPTION);

        this.anonymous = data.getBoolean(Key.ANONYMOUS);

        this.time_limit = OffsetDateTime.parse(data.getString(Key.TIME_LIMIT), ConfigLoader.DATA_TIME_SAVE_FORMAT);

        JSONArray prizes_array = data.getJSONArray(Key.PRIZES);
        for (int i = 0; i < prizes_array.length(); i++) {
            this.prizes.add(prizes_array.getString(i));
        }

        JSONArray sign_ups_array = data.getJSONArray(Key.SIGN_UPS);
        List<Long> memberIds = DataTools.convertJSONArrayListToLongList(sign_ups_array);
        Check.isValidMemberIdList(guild_id, memberIds);
        this.sign_ups.addAll(memberIds);

        JSONArray allowed_roles_array = data.getJSONArray(Key.ALLOWED_ROLES);
        List<Long> roleIds = DataTools.convertJSONArrayListToLongList(allowed_roles_array);
        Check.isValidRoleIdList(guild_id, roleIds);
        this.allowed_roles = roleIds;

        this.timer_operation_id = CentralTimer.get().schedule(() -> closeGivewawayEntry(), this.time_limit);
        return this;
    }

    public JSONObject compileToJSON() {
        JSONObject compiledData = new JSONObject();
        return compiledData;
    }

    public Guild getGuild() {
        return Bot.getAPI().getGuildById(guild_id);
    }

    @Override
    public Long getChannelId() {
        return this.text_channel_id;
    }

    @Override
    public TextChannel getChannel() {
        return this.getGuild().getTextChannelById(text_channel_id);
    }

    @Override
    public Long getMessageId() {
        return this.message_id;
    }

    @Override
    public Message getMessage() {
        return this.getChannel().retrieveMessageById(message_id).complete();
    }

    public Long getUserId() {
        return this.user_id;
    }

    public User getUser() {
        return Bot.getAPI().retrieveUserById(user_id).complete();
    }

    private MessageEmbed buildEndEmbed() {
        // TODO
        return null;
    }

    private void closeGivewawayEntry() {
        // TODO
    }

    public static abstract class Key {
        public static final String USER_ID = "user_id";
        public static final String TITLE = "title";
        public static final String DESCRIPTION = "description";
        public static final String PRIZES = "prizes";
        public static final String SIGN_UPS = "sign_ups";
        public static final String ANONYMOUS = "anonymous";
        public static final String ALLOWED_ROLES = "allowed_roles";
        public static final String TIME_LIMIT = "time_limit";
    }
}