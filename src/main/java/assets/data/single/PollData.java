package assets.data.single;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import assets.data.DataContainer;
import assets.data.DataTools;
import base.Bot;
import engines.base.Check;
import engines.base.LanguageEngine;
import engines.base.LanguageEngine.Language;
import engines.base.Toolbox;
import engines.data.ConfigManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.TimeFormat;

public class PollData implements DataContainer {
    
    private final Guild guild;
    private final TextChannel text_channel;
    private final User user;
    private final PollType type;
    private Message message;
    private String title, description = "N/A";
    private boolean anonymous, public_results = false;
    private int max_total_votes, max_votes_per_option = 1;
    private OffsetDateTime time_limit = OffsetDateTime.now().plusWeeks(1);
    private List<String> options = new LinkedList<>();
    private List<Role> allowed_roles = new LinkedList<>();
    private ConcurrentHashMap<Member, List<Integer>> votes = new ConcurrentHashMap<>();

	public PollData(Message message, JSONObject data) {
        this.guild = message.getGuild();
	    this.text_channel = message.getChannel().asTextChannel();
	    this.message = message;
	    this.user = Bot.INSTANCE.jda.retrieveUserById(data.getLong(Key.USER_ID)).complete();
	    this.type = PollType.valueOf(data.getString(Key.TYPE));
	    this.instanciateFromJSON(data);
	}
	
	public PollData(Guild guild, TextChannel channel, User user, PollType type) {
        this.guild = guild;
	    this.text_channel = channel;
	    this.user = user;
	    this.type = type;
	}

    @Override
    public DataContainer instanciateFromJSON(JSONObject data) {
        this.title = data.getString(Key.TITLE);
        this.description = this.message.getEmbeds().get(0).getDescription();
        
        this.anonymous = data.getBoolean(Key.ANONYMOUS);
        this.public_results = data.getBoolean(Key.PUBLIC_RESULTS);
        
        this.max_total_votes = data.getInt(Key.MAX_TOTAL_VOTES);
        this.max_votes_per_option = data.getInt(Key.MAX_VOTES_PER_OPTION);
        
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
            LinkedList<Integer> votes_list = new LinkedList<>();
            for (int i = 0; i < votes_array.length(); i++) {
                votes_list.add(votes_array.getInt(i));
            }
            votes.put(guild.retrieveMemberById(key).complete(), votes_list);
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
        
        compiledData.put(Key.MAX_TOTAL_VOTES, max_total_votes);
        compiledData.put(Key.MAX_VOTES_PER_OPTION, max_votes_per_option);
        
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
    
    public User getOwner() {
        return this.user;
    }
    
    public PollType getType() {
        return this.type;
    }

    public Message getMessage() {
        return this.message;
    }
    
    public PollData setMessage(Message message) {
        this.message = message;
        return this;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public PollData setTitle(String title) {
        this.title = title;
        return this;
    }
    
    public String getDescription() {
        if (this.description.equalsIgnoreCase("N/A") && this.message != null) {
            this.description = this.message.getEmbeds().get(0).getDescription();
        }
        return this.description;
    }
    
    public PollData setDescription(String description) {
        this.description = description;
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
    
    public int getMaxTotalVotes() {
        return this.max_total_votes;
    }
    
    public PollData setMaxTotalVotes(int limit) {
        this.max_total_votes = limit;
        return this;
    }
    
    public int getMaxVotesPerOption() {
        return this.max_votes_per_option;
    }
    
    public PollData setMaxVotesPerOption(int limit) {
        this.max_votes_per_option = limit;
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
    
    public ConcurrentHashMap<Member, List<Integer>> getVotes() {
        return this.votes;
    }
    
    public List<Integer> getVotesByMember(Member member) {
        return this.votes.get(member);
    }
    
    public PollData setVotes(ConcurrentHashMap<Member, List<Integer>> votes) {
        DataTools.setMap(this.votes, votes);
        return this;
    }
    
    public PollData addVotes(ConcurrentHashMap<Member, List<Integer>> votes) {
        votes.forEach((member, vote_array) -> {
            if (member != null) {
                this.addVotesForMember(member, vote_array.stream().mapToInt(Integer::intValue).toArray());
            }
        });
        return this;
    }
    
    public PollData addVotesForMember(Member member, int... votes) {
        List<Integer> current = this.votes.get(member);
        for (int i = 0; i < votes.length; i++) {
            current.add(votes[i]);
        }
        return this;
    }
    
    public PollData removeVotes(Member... members) {
        for (int i = 0; i < members.length; i++) {
            this.votes.remove(members[i]);
        }
        return this;
    }
    
    public PollData removeVotesOfMember(Member member, int... votes) {
        List<Integer> current = this.getVotesByMember(member);
        ArrayList<Integer> votes_to_remove = new ArrayList<>();
        ArrayList<Integer> indicies_to_remove = new ArrayList<>();
        for (int i = 0; i < votes.length; i++) {
            votes_to_remove.add(i, votes[i]);
        }
        for (int i = 0; i < current.size(); i++) {
            if (votes_to_remove.remove(current.get(i))) {
                indicies_to_remove.add(i);
            }
        }
        indicies_to_remove.forEach(index -> current.remove(index));
        this.votes.put(member, current);
        return this;
    }
    
    public boolean checkVoteValidity(Member member, int vote) {
        List<Integer> current = this.votes.get(member);
        if (current.size() >= this.max_total_votes) {
            return false;
        } else if (Collections.frequency(current, vote) >= this.max_votes_per_option) {
                return false;
        } else {
            return true;
        }
    }
    
    public Message sendPollMessage() {
        MessageCreateAction mca = this.text_channel.sendMessageEmbeds(this.buildPollEmbed(true, false));
        if (this.type != PollType.TRUE_OR_FALSE) {
            ItemComponent[] components = new ItemComponent[]{};
            for (int i = 0; i < this.options.size(); i++) {
                components[i] = Button.secondary("poll:" + String.valueOf(i), Toolbox.convertIntegerToEmoji(i+1));
            }
            if (components.length < 5) {
                mca.addActionRow(components);
            } else {
                int actionRowsNeeded = components.length / 5;
                int rest = components.length % 5;
                if (rest > 0) {
                    actionRowsNeeded++;
                }
                int progress = 0;
                for (int i = 0; i < actionRowsNeeded; i++) {
                    List<ItemComponent> componentsToBeInserted = new ArrayList<>();
                    for (ItemComponent cmp : Arrays.copyOfRange(components, progress, progress + 5)) {
                        if (cmp != null) {
                            componentsToBeInserted.add(cmp);
                        }
                    }
                    mca.addActionRow(componentsToBeInserted);
                    progress += 5;
                }
            }
        } else {
            mca.addActionRow(Button.secondary("poll:0", Emoji.fromUnicode("\u274C")), Button.secondary("poll:1", Emoji.fromUnicode("\u2705")));
        }
        this.message = mca.complete();
        return this.message;
    }
    
    public MessageEmbed buildPollEmbed(boolean show_settings, boolean results) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setAuthor(this.user.getName(), "https://www.discordapp.com/users/" + this.user.getId(), this.user.getEffectiveAvatarUrl());
        eb.setColor(LanguageEngine.EMBED_DEFAULT_COLOR);
        if (results) {
            eb.setTitle("**Results** | " + this.title);
        } else {
            eb.setTitle(this.title);
        }
        eb.setDescription(this.description);
        eb.setThumbnail("https://github.com/Gregor7008/Gregor7008/blob/37c084226d19be80e8e2b907eeeff67a3e3fa5c9/resources/graphics/emojis/bar_chart.png?raw=true");

        String[] texts = LanguageEngine.getRaw(Language.ENGLISH, this, "titles").split(LanguageEngine.SEPERATOR);
        if (this.anonymous) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < this.options.size(); i++) {
                sb.append(Toolbox.convertIntegerToEmoji(i+1).getFormatted()
                        + " "
                        + this.options.get(i)
                        + String.valueOf(this.getVotersListByOptionIndex(i).size()));
                if (i++ < this.options.size()) {
                    sb.append("\n");
                }
            }
            eb.addField(":arrow_right: " + texts[0], sb.toString(), false);
        } else {
            for (int i = 0; i < this.options.size(); i++) {
                eb.addField(Toolbox.convertIntegerToEmoji(i+1).getFormatted()
                        + " "
                        + this.options.get(i)
                        + String.valueOf(this.getVotersListByOptionIndex(i).size()),
                        this.getVotersDisplayByOptionIndex(i), false);
            }
        }
        
        if (show_settings) {
            eb.addField(texts[1], texts[2] + ": " + String.valueOf(anonymous) + "\n" + texts[3] + ": " + TimeFormat.DATE_TIME_SHORT.format(this.time_limit), false);    
        }
        eb.setFooter(LanguageEngine.buildFooter());
        eb.setTimestamp(Instant.now());
        return eb.build();
    }
    
    private String getVotersDisplayByOptionIndex(int index) {
        StringBuilder sb = new StringBuilder();
        List<Member> list = this.getVotersListByOptionIndex(index);
        if (!list.isEmpty()) {
            ConcurrentHashMap<Member, Integer> counting_cache = new ConcurrentHashMap<>();
            for (int i = 0; i < list.size(); i++) {
                Member member = list.get(i);
                Integer current = counting_cache.get(member);
                if (current != null) {
                    counting_cache.put(member, current++);
                } else {
                    counting_cache.put(member, 1);
                }
            }
            for (Map.Entry<Member, Integer> entry : counting_cache.entrySet()) {
                int progress = 0;
                int vote_count = entry.getValue();
                if (vote_count > 0) {
                    if (progress > 0) {
                        sb.append(", ");
                    }
                    sb.append(entry.getKey().getAsMention());
                    if (vote_count > 1) {
                        sb.append(" " + String.valueOf(vote_count));
                    }
                    progress++;
                }
            }
        }
        return sb.toString();
    }
    
    private List<Member> getVotersListByOptionIndex(int index) {
        ArrayList<Member> list = new ArrayList<>();
        if (!this.votes.isEmpty()) {
            for (Map.Entry<Member, List<Integer>> entry : this.votes.entrySet()) {
                List<Integer> votes_list = entry.getValue();
                for (int a = 0; a < votes_list.size(); a++) {
                    if (votes_list.get(a) == index) {
                        list.add(entry.getKey());
                    }
                }
            }
        }
        list.removeAll(Collections.singleton(null));
        return list;
    }
    
    private static abstract class Key {
        public static final String USER_ID = "user_id";
        public static final String TYPE = "type";
        public static final String TITLE = "title";
        public static final String MAX_TOTAL_VOTES = "max_total_votes";
        public static final String MAX_VOTES_PER_OPTION = "max_votes_per_option";
        public static final String OPTIONS = "options";
        public static final String VOTES = "votes";
        public static final String ANONYMOUS = "anonymous";
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