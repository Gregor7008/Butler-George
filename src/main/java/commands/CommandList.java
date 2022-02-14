package commands;

import java.util.concurrent.ConcurrentHashMap;

import commands.moderation.AutoRole;
import commands.moderation.BotAutoRole;
import commands.moderation.Cleanup;
import commands.moderation.Clear;
import commands.moderation.Close;
import commands.moderation.CustomChannelRole;
import commands.moderation.DefaultAccessRoles;
import commands.moderation.ForbiddenWords;
import commands.moderation.Goodbye;
import commands.moderation.IgnoreChannel;
import commands.moderation.Join2Create;
import commands.moderation.LevelChannel;
import commands.moderation.Levelreward;
import commands.moderation.ModRole;
import commands.moderation.Move;
import commands.moderation.Mute;
import commands.moderation.Penalty;
import commands.moderation.ReactionRole;
import commands.moderation.ReportChannel;
import commands.moderation.Reset;
import commands.moderation.Rolesorting;
import commands.moderation.SuggestionChannel;
import commands.moderation.SupportChannel;
import commands.moderation.SupportRole;
import commands.moderation.SupportTalk;
import commands.moderation.TempBan;
import commands.moderation.TempMute;
import commands.moderation.Unmute;
import commands.moderation.Warning;
import commands.moderation.Welcome;
import commands.music.Nowplaying;
import commands.music.Play;
import commands.music.Queue;
import commands.music.Skip;
import commands.music.Stop;
import commands.utilities.Channelpermission;
import commands.utilities.CreateChannel;
import commands.utilities.Embed;
import commands.utilities.Help;
import commands.utilities.Language;
import commands.utilities.Leave;
import commands.utilities.Level;
import commands.utilities.Levelbackground;
import commands.utilities.PingAndMove;
import commands.utilities.Poll;
import commands.utilities.Report;
import commands.utilities.Serverinfo;
import commands.utilities.Suggest;
import commands.utilities.Userinfo;
import commands.utilities.Webhook;

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
		this.utilitycmds.put("language", new Language());
		this.utilitycmds.put("webhook", new Webhook());
		this.utilitycmds.put("createchannel", new CreateChannel());
		this.utilitycmds.put("channelpermission", new Channelpermission());
		this.utilitycmds.put("pingandmove", new PingAndMove());
		this.utilitycmds.put("leave", new Leave());
		//this.utilitycmds.put("giveaway", new Giveaway());
		
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
		this.moderationcmds.put("suggestionchannel", new SuggestionChannel());
		this.moderationcmds.put("levelchannel", new LevelChannel());
		this.moderationcmds.put("ignorechannel", new IgnoreChannel());
		this.moderationcmds.put("modrole", new ModRole());
		this.moderationcmds.put("reportchannel", new ReportChannel());
		this.moderationcmds.put("reactionrole", new ReactionRole());
		this.moderationcmds.put("supportrole", new SupportRole());
		this.moderationcmds.put("supportchannel", new SupportChannel());
		this.moderationcmds.put("supporttalk", new SupportTalk());
		this.moderationcmds.put("forbiddenwords", new ForbiddenWords());
		this.moderationcmds.put("reset", new Reset());
		this.moderationcmds.put("move", new Move());
		this.moderationcmds.put("defaultaccessrole", new DefaultAccessRoles());
		this.moderationcmds.put("customchannelrole", new CustomChannelRole());
		this.moderationcmds.put("unmute", new Unmute());
		//Developement
		this.moderationcmds.put("cleanup", new Cleanup());
		
		//Music
		this.musiccmds.put("nowplaying", new Nowplaying());
		this.musiccmds.put("play", new Play());
		this.musiccmds.put("queue", new Queue());
		this.musiccmds.put("skip", new Skip());
		this.musiccmds.put("stop", new Stop());
		
		
	}

}
