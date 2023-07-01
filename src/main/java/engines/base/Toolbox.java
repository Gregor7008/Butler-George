package engines.base;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.json.JSONArray;

import engines.data.ConfigLoader;
import engines.functions.GuildMusicManager;
import engines.functions.PlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;

public abstract class Toolbox {
    
    public static String convertDurationToString(Duration duration) {
        return String.format("%02d:%02d:%02d:%02d",
                duration.toDaysPart(),
                duration.toHoursPart(), 
                duration.toMinutesPart(), 
                duration.toSecondsPart());
    }
    
    public static Duration convertStringToDuration(String string) {
        String[] codes = string.split(":");
        long days = TimeUnit.DAYS.toSeconds(Integer.valueOf(codes[0]));
        long hours = TimeUnit.HOURS.toSeconds(Integer.valueOf(codes[1]));
        long minutes = TimeUnit.MINUTES.toSeconds(Integer.valueOf(codes[2]));
        long seconds = Long.valueOf(codes[3]);
        return Duration.ofSeconds(days + hours + minutes + seconds);
    }

	public static void forwardMessage(MessageChannel target, Message source) {
		List<Attachment> attachements = source.getAttachments();
		List<File> files = new ArrayList<>();
		List<String> names = new ArrayList<>();
		for (int i = 0; i < attachements.size(); i++) {
			File file = null;
			try {file = File.createTempFile(attachements.get(i).getFileName(), null);
			} catch (IOException e) {}
			Boolean deleted = true;
			if (file.exists()) {
				deleted = file.delete();
			}
			if (deleted) {
				try {
					attachements.get(i).getProxy().downloadToFile(file).get();
				} catch (InterruptedException | ExecutionException e) {}
				names.add(attachements.get(i).getFileName());
				files.add(file);
			}
		}
		MessageCreateAction messageAction = target.sendMessage(source.getContentRaw());
		for (int i = 0; i < files.size(); i++) {
			messageAction.addFiles(FileUpload.fromData(files.get(i), names.get(i)));
		}
		messageAction.queue(e -> files.forEach(f -> f.delete()));
	}
	
	public static boolean removeValueFromArray(JSONArray current, Object value) {
		for (int i = 0; i < current.length(); i++) {
			if (current.get(i).equals(value)) {
				current.remove(i);
				i = current.length();
				return true;
			}
		}
		return false;
	}
	
	public static String processAutoMessage(String input, Guild guild, User user, boolean mentions) {
		String output =  input.replace("{server}", guild.getName())
				.replace("{membercount}", Integer.toString(guild.getMemberCount()))
				.replace("{date}", OffsetDateTime.now().format(LanguageEngine.DEFAULT_TIME_FORMAT))
				.replace("{boosts}", String.valueOf(guild.getBoostCount()));
		if (user != null) {
		    output = output.replace("{level}", String.valueOf(ConfigLoader.get().getMemberConfig(guild, user).getInt("level")));
	        if (mentions) {
	            output = output.replace("{user}", user.getAsMention());
	        } else {
	            output = output.replace("{user}", user.getName());
	        }
		}
		return output;
	}
	
	public static void stopMusicAndLeaveOn(Guild guild) {
		final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);
		musicManager.scheduler.player.stopTrack();
		musicManager.scheduler.queue.clear();
		VoiceChannel vc = (VoiceChannel) guild.getSelfMember().getVoiceState().getChannel();
		guild.getAudioManager().closeAudioConnection();
		if (vc.getUserLimit() != 0) {
			vc.getManager().setUserLimit(vc.getUserLimit() - 1).queue();
		}
	}
	
    public static void sortRoles(Guild guild, Member member, List<Role> sorting_roles, Role group_role) {
            int match = 0;
            for (int i = 0; i < member.getRoles().size(); i++) {
                if (sorting_roles.contains(member.getRoles().get(i))) {
                    match++;
                }
            }
            if (match > 0 && !member.getRoles().contains(group_role)) {
                guild.addRoleToMember(member, group_role).queue();
            }
            if (match == 0 && member.getRoles().contains(group_role)) {
                guild.removeRoleFromMember(member, group_role).queue();
            }
    }
    
    public static void filterValidMembers(Collection<Member> members, Guild guild) {
        List<Member> validMembers = new ArrayList<>();
        validMembers.addAll(guild.retrieveMembersByIds(
                members.stream().map(member -> {
                    return member.getIdLong();
                })
                .collect(Collectors.toList()))
                .get());
        validMembers.removeAll(Collections.singleton(null));
        members.removeIf(member -> {
            return !validMembers.contains(member);
        });
    }
    
    public static Emoji convertIntegerToEmoji(int integer) {
        String unicode = "";
        switch (integer) {
            case 0:
                unicode = "\u0030\u20E3";
                break;
            case 1:
                unicode = "\u0031\u20E3";
                break;
            case 2:
                unicode = "\u0032\u20E3";
                break;
            case 3:
                unicode = "\u0033\u20E3";
                break;
            case 4:
                unicode = "\u0034\u20E3";
                break;
            case 5:
                unicode = "\u0035\u20E3";
                break;
            case 6:
                unicode = "\u0036\u20E3";
                break;
            case 7:
                unicode = "\u0037\u20E3";
                break;
            case 8:
                unicode = "\u0038\u20E3";
                break;
            case 9:
                unicode = "\u0039\u20E3";
                break;
            case 10:
                unicode = "\uD83D\uDD1F";
                break;
            default:
                unicode = "\u002A\u20E3";
        }
        return Emoji.fromUnicode(unicode);
    }
}