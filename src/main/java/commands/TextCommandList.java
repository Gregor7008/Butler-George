package commands;

import java.util.concurrent.ConcurrentHashMap;

import commands.textcommands.Addpermission;
import commands.textcommands.Removepermission;

public class TextCommandList {

	public ConcurrentHashMap<String, TextCommand> textcmds = new ConcurrentHashMap<>();
	
	public TextCommandList() {
		this.textcmds.put("addpermission", new Addpermission());
		this.textcmds.put("removepermission", new Removepermission());
	}
}