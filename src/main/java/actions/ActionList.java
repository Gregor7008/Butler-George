package actions;

import java.util.concurrent.ConcurrentHashMap;

import actions.administration.AutoRole;
import actions.administration.BotAutoRole;
import actions.administration.CustomChannelRoles;
import actions.administration.DefaultAccessRoles;
import actions.administration.Goodbye;
import actions.administration.Join2Create;
import actions.administration.LevelChannel;
import actions.administration.LevelReward;
import actions.administration.Penalty;
import actions.administration.ReactionRole;
import actions.administration.ReportChannel;
import actions.administration.SuggestionChannel;
import actions.administration.SupportTalk;
import actions.administration.Welcome;
import actions.moderation.Mute;
import actions.moderation.TempBan;
import actions.moderation.TempMute;
import actions.moderation.Unmute;
import components.actions.ActionRequest;

public class ActionList {
	
	public ConcurrentHashMap<String, ActionRequest> actions = new ConcurrentHashMap<>();

	public ActionList() {
		//Administration
		this.actions.put("AutoRole", new AutoRole());
		this.actions.put("BotAutoRole", new BotAutoRole());
		this.actions.put("CustomChannelRoles", new CustomChannelRoles());
		this.actions.put("DefaultAccessRoles", new DefaultAccessRoles());
		this.actions.put("Goodbye", new Goodbye());
		this.actions.put("Join2Create", new Join2Create());
		this.actions.put("LevelChannel", new LevelChannel());
		this.actions.put("LevelRewards", new LevelReward());
		this.actions.put("Penalty", new Penalty());
		this.actions.put("ReactionRole", new ReactionRole());
		this.actions.put("ReportChannel", new ReportChannel());
		this.actions.put("SuggestionChannel", new SuggestionChannel());
		this.actions.put("SupportTalk", new SupportTalk());
		this.actions.put("Welcome", new Welcome());
		//Moderation
		this.actions.put("Mute", new Mute());
		this.actions.put("TempBan", new TempBan());
		this.actions.put("TempMute", new TempMute());
		this.actions.put("Unmute", new Unmute());
	}
}