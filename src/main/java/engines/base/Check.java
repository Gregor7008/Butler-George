package engines.base;

import java.net.MalformedURLException;
import java.net.URL;

import base.Bot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
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
    
    public static boolean isValidGuild(Guild guild) {
        if (guild == null) {
            return false;
        }
        return Bot.getAPI().getGuildById(guild.getIdLong()) != null;
    }
    
    public static boolean isValidUser(User user) {
        if (user == null) {
            return false;
        }
        return Bot.getAPI().retrieveUserById(user.getIdLong()).complete() != null;
    }
    
    public static boolean isValidMember(Member member) {
        if (member == null) {
            return false;
        }
        return member.getGuild().retrieveMemberById(member.getIdLong()).complete() != null;
    }
    
    public static boolean isValidRole(Role role) {
        if (role == null) {
            return false;
        }
        return role.getGuild().getRoleById(role.getIdLong()) != null;
    }

    public static boolean isValidCategory(Category category) {
        if (category == null) {
            return false;
        }
        return category.getGuild().getCategoryById(category.getIdLong()) != null;
    }

    public static boolean isValidChannel(GuildChannel channel) {
        if (channel == null) {
            return false;
        }
        return channel.getGuild().getGuildChannelById(channel.getIdLong()) != null;
    }
    
    public static boolean isValidMessage(Message message) {
        if (message == null) {
            return false;
        }
        return message.getChannel().retrieveMessageById(message.getIdLong()).complete() != null;
    }
}