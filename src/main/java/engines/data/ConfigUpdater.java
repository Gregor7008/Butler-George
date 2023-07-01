package engines.data;

import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ConfigUpdater extends ListenerAdapter {

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        //Voice Channels: J2C, 
        //Text Channels: Modmails, Polls, Giveaways, Reactionroles
    }
    
    @Override
    public void onRoleDelete(RoleDeleteEvent event) {
        //Level-Rewards, Penalties, 
    }
    
    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        //Polls, Giveaways, Reactionroles, Modmails
    }
}