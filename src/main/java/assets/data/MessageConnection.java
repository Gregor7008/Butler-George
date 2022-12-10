package assets.data;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public interface MessageConnection {

    public TextChannel getChannel();
    public Message getMessage();
    public Long getChannelId();
    public Long getMessageId();
    
}