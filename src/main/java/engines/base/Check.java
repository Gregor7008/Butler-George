package engines.base;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONException;

import base.Bot;
import engines.data.ConfigLoader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

public abstract class Check {
    
    public static User isUserCategory(Category category) {
        try {
            return Bot.INSTANCE.jda.getUserById(ConfigLoader.INSTANCE.getGuildConfig(category.getGuild(), "customchannelcategories").getLong(category.getId()));
        } catch (JSONException e) {
            return null;
        }
    }
    
    public static boolean isURL(String subject) {
        try {
            new URL(subject);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
    
    public static boolean isValidGuild(Guild guild) {
        return Bot.INSTANCE.jda.getGuildById(guild.getIdLong()) != null;
    }
    
    public static boolean isValidUser(User user) {
        return Bot.INSTANCE.jda.retrieveUserById(user.getIdLong()).complete() != null;
    }
    
    public static boolean isValidMember(Member member) {
        return member.getGuild().retrieveMemberById(member.getIdLong()).complete() != null;
    }
    
    public static List<Member> areValidMembers(Collection<Member> members, Guild guild) {
        List<Member> validMembers = new ArrayList<>();
        validMembers.addAll(guild.retrieveMembersByIds(
                members.stream().map(member -> {
                    return member.getIdLong();
                })
                .collect(Collectors.toList()))
                .get());
        validMembers.removeAll(Collections.singleton(null));
        return validMembers;
    }
    
    public static boolean isValidRole(Role role) {
        return role.getGuild().getRoleById(role.getIdLong()) != null;
    }

    public static boolean isValidCategory(Category category) {
        return category.getGuild().getCategoryById(category.getIdLong()) != null;
    }

    public static boolean isValidChannel(GuildChannel channel) {
        return channel.getGuild().getGuildChannelById(channel.getIdLong()) != null;
    }
}