package commands;

import java.util.concurrent.ConcurrentHashMap;

import commands.moderation.Penalty;
import commands.moderation.AutoRole;
import commands.moderation.BotAutoRole;
import commands.moderation.Cleanup;
import commands.moderation.Clear;
import commands.moderation.Close;
import commands.moderation.ForbiddenWords;
import commands.moderation.Goodbye;
import commands.moderation.IgnoreChannel;
import commands.moderation.Join2Create;
import commands.moderation.Levelreward;
import commands.moderation.Move;
import commands.moderation.Mute;
import commands.moderation.ReactionRole;
import commands.moderation.Reset;
import commands.moderation.Rolesorting;
import commands.moderation.SetCustomChannelRole;
import commands.moderation.SetDefaultAccess;
import commands.moderation.SetLevelChannel;
import commands.moderation.SetModRole;
import commands.moderation.SetReportChannel;
import commands.moderation.SetSuggestionChannel;
import commands.moderation.SetSupportChannel;
import commands.moderation.SetSupportRole;
import commands.moderation.SetSupportTalk;
import commands.moderation.TempBan;
import commands.moderation.TempMute;
import commands.moderation.Warning;
import commands.moderation.Welcome;
import commands.music.Nowplaying;
import commands.music.Play;
import commands.music.Queue;
import commands.music.Skip;
import commands.music.Stop;
import commands.utilities.CreateChannel;
import commands.utilities.Embed;
import commands.utilities.Help;
import commands.utilities.Level;
import commands.utilities.Levelbackground;
import commands.utilities.PingAndMove;
import commands.utilities.Poll;
import commands.utilities.Report;
import commands.utilities.Serverinfo;
import commands.utilities.SetLanguage;
import commands.utilities.Suggest;
import commands.utilities.Userinfo;
import commands.utilities.Userpermission;
import commands.utilities.Webhook;
import components.utilities.Test;

public class CommandList {
	
	public ConcurrentHashMap<String, Command> utilitycmds = new ConcurrentHashMap<>();
	public ConcurrentHashMap<String, Command> moderationcmds = new ConcurrentHashMap<>();
	public ConcurrentHashMap<String, Command> musiccmds = new ConcurrentHashMap<>();
	
	public CommandList() {
		//Utilities
		this.utilitycmds.put("embed", new Embed());
		this.utilitycmds.put("help", new Help());
		this.utilitycmds.put("level", new Level());
		this.utilitycmds.put("levelbackground", new Levelbackground());
		this.utilitycmds.put("suggest", new Suggest());
		this.utilitycmds.put("userinfo", new Userinfo());
		this.utilitycmds.put("poll", new Poll());
		this.utilitycmds.put("report", new Report());
		this.utilitycmds.put("serverinfo", new Serverinfo());
		this.utilitycmds.put("setlanguage", new SetLanguage());
		this.utilitycmds.put("webhook", new Webhook());
		this.utilitycmds.put("createchannel", new CreateChannel());
		this.utilitycmds.put("userpermission", new Userpermission());
		this.utilitycmds.put("pingandmove", new PingAndMove());
		
		//Moderation
		this.moderationcmds.put("penalty", new Penalty());
		this.moderationcmds.put("autorole", new AutoRole());
		this.moderationcmds.put("botautorole", new BotAutoRole());
		this.moderationcmds.put("clear", new Clear());
		this.moderationcmds.put("goodbye", new Goodbye());
		this.moderationcmds.put("join2create", new Join2Create());
		this.moderationcmds.put("levelreward", new Levelreward());
		this.moderationcmds.put("mute", new Mute());
		this.moderationcmds.put("rolesort", new Rolesorting());
		this.moderationcmds.put("tempban", new TempBan());
		this.moderationcmds.put("tempmute", new TempMute());
		this.moderationcmds.put("warning", new Warning());
		this.moderationcmds.put("welcome", new Welcome());
		this.moderationcmds.put("close", new Close());
		this.moderationcmds.put("setsuggestionchannel", new SetSuggestionChannel());
		this.moderationcmds.put("setlevelchannel", new SetLevelChannel());
		this.moderationcmds.put("ignorechannel", new IgnoreChannel());
		this.moderationcmds.put("setmodrole", new SetModRole());
		this.moderationcmds.put("setreportchannel", new SetReportChannel());
		this.moderationcmds.put("reactionrole", new ReactionRole());
		this.moderationcmds.put("setsupportrole", new SetSupportRole());
		this.moderationcmds.put("setsupportchannel", new SetSupportChannel());
		this.moderationcmds.put("setsupporttalk", new SetSupportTalk());
		this.moderationcmds.put("forbiddenwords", new ForbiddenWords());
		this.moderationcmds.put("reset", new Reset());
		this.moderationcmds.put("move", new Move());
		this.moderationcmds.put("setdefaultaccess", new SetDefaultAccess());
		this.moderationcmds.put("setcustomchannelrole", new SetCustomChannelRole());
		//Developement
		this.moderationcmds.put("test", new Test());
		this.moderationcmds.put("cleanup", new Cleanup());
		
		//Music
		this.musiccmds.put("nowplaying", new Nowplaying());
		this.musiccmds.put("play", new Play());
		this.musiccmds.put("queue", new Queue());
		this.musiccmds.put("skip", new Skip());
		this.musiccmds.put("stop", new Stop());
		
		
	}

}
