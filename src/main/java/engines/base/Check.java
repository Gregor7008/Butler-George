package engines.base;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;

import assets.data.MessageConnection;
import base.Bot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

public abstract class Check {
    
    public static boolean isURL(String subject) {
        try {
            new URL(subject);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
    
    public static boolean isValidGuild(Long guild_id) {
        return Bot.getAPI().getGuildById(guild_id) != null;
    }
    
    public static boolean isValidGuild(Guild guild) {
        if (guild == null) {
            return false;
        }
        return Bot.getAPI().getGuildById(guild.getIdLong()) != null;
    }
    
    public static boolean isValidUser(Long user_id) {
        try {
            return Bot.getAPI().retrieveUserById(user_id).complete() != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean isValidUser(User user) {
        if (user == null) {
            return false;
        }
        return Bot.getAPI().retrieveUserById(user.getIdLong()).complete() != null;
    }
    
    public static boolean isValidMember(Long guild_id, Long user_id) {
        try {
            return Bot.getAPI().getGuildById(guild_id).retrieveMemberById(user_id).complete() != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean isValidMember(Member member) {
        if (member == null) {
            return false;
        }
        return member.getGuild().retrieveMemberById(member.getIdLong()).complete() != null;
    }
    
    public static boolean isValidRole(Long guild_id, Long role_id) {
        try {
            return Bot.getAPI().getGuildById(guild_id).getRoleById(role_id) != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean isValidRole(Role role) {
        if (role == null) {
            return false;
        }
        return role.getGuild().getRoleById(role.getIdLong()) != null;
    }
    
    public static boolean isValidCategory(Long guild_id, Long role_id) {
        try {
            return Bot.getAPI().getGuildById(guild_id).getCategoryById(role_id) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidCategory(Category category) {
        if (category == null) {
            return false;
        }
        return category.getGuild().getCategoryById(category.getIdLong()) != null;
    }
    
    public static boolean isValidChannel(Long guild_id, Long role_id) {
        try {
            return Bot.getAPI().getGuildById(guild_id).getRoleById(role_id) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidChannel(GuildChannel channel) {
        if (channel == null) {
            return false;
        }
        return channel.getGuild().getGuildChannelById(channel.getIdLong()) != null;
    }
    
    public static boolean isValidMessage(Long guild_id, Long channel_id, Long role_id) {
        try {
            return Bot.getAPI().getGuildById(guild_id).getTextChannelById(channel_id).retrieveMessageById(role_id) != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean isValidMessage(Message message) {
        if (message == null) {
            return false;
        }
        return message.getChannel().retrieveMessageById(message.getIdLong()).complete() != null;
    }

    public static <T extends MessageConnection> void isValidChannelMessageMap(@NotNull Guild guild, ConcurrentHashMap<Long, ConcurrentHashMap<Long, T>> source_map, boolean validate_messages) {
        List<Long> channel_ids_to_remove = new ArrayList<>();
        for (Map.Entry<Long, ConcurrentHashMap<Long, T>> entry : source_map.entrySet()) {
            TextChannel channel = guild.getTextChannelById(entry.getKey());
            if (channel == null) {
                channel_ids_to_remove.add(entry.getKey());
            } else if (validate_messages) {
                isValidMessageMap(channel, entry.getValue());
            }
        }
        for (long id : channel_ids_to_remove) {
            source_map.remove(id);
        }
    }
    
    public static <T> void isValidMessageMap(@NotNull TextChannel channel, ConcurrentHashMap<Long, T> source_map) {
        List<Long> message_ids_to_remove = new ArrayList<>();
        for (Map.Entry<Long, T> entry : source_map.entrySet()) {
            if (channel.retrieveMessageById(entry.getKey()) == null) {
                message_ids_to_remove.add(entry.getKey());
            }
        }
        for (long id : message_ids_to_remove) {
            source_map.remove(id);
        }
    }
    
    public static void isValidMemberIdList(long guild_id, List<Long> member_id_list) {
        List<Long> valid_ids = Bot.getAPI().getGuildById(guild_id).retrieveMembersByIds(member_id_list).get()
                .stream()
                .filter(member -> {return member != null;})
                .map(member -> member.getIdLong())
                .toList();
        member_id_list.clear();
        member_id_list.addAll(valid_ids);
    }
    
    public static void isValidRoleIdList(long guild_id, List<Long> role_id_list) {
        List<Long> valid_ids = Bot.getAPI().getGuildById(guild_id).getRoles()
                .stream()
                .map(role -> role.getIdLong())
                .filter(role_id -> {return role_id_list.contains(role_id);})
                .toList();
        role_id_list.clear();
        role_id_list.addAll(valid_ids);
    }
    
    public static void isValidChannelIdList(long guild_id, List<Long> channel_id_list) {
        List<Long> valid_ids = Bot.getAPI().getGuildById(guild_id).getChannels()
                .stream()
                .map(channel -> channel.getIdLong())
                .filter(channel_id -> {return channel_id_list.contains(channel_id);})
                .toList();
        channel_id_list.clear();
        channel_id_list.addAll(valid_ids);
    }
}