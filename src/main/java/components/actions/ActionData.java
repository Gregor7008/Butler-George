package components.actions;

import net.dv8tion.jda.api.Permission;

public class ActionData {
	
	public static String ADMINISTRATION = "admin";
	public static String MODERATION = "mod";
	
	public static String SETCHANNEL = "setch";
	public static String SETROLE = "setrl";
	
	private ActionRequest actionRequest = null;
	private String name = null;
	private String info = null;
	private String category = null;
	private String subCategory = null;
	private String[] subActions = null;
	private Permission minimumPermission = null;
	
	public ActionData(ActionRequest actionRequest) {
		this.actionRequest = actionRequest;
	}

	public ActionData setName(String name) {
		this.name = name;
		return this;
	}
	
	public ActionData setInfo(String info) {
		this.info = info;
		return this;
	}
	
	public ActionData setMinimumPermission(Permission permission) {
		this.minimumPermission = permission;
		return this;
	}
	
	public ActionData setCategory(String categoryConstant) {
		if (!categoryConstant.equals(ActionData.ADMINISTRATION) || !categoryConstant.equals(ActionData.MODERATION)) {
			throw new IllegalArgumentException("Category constant provided is not a valid category");
		} else {
			this.category = categoryConstant;
		}
		return this;
	}
	
	public ActionData setSubCategory(String subCategoryConstant) {
		if (!subCategoryConstant.equals(ActionData.SETCHANNEL) || !subCategoryConstant.equals(ActionData.SETROLE)) {
			throw new IllegalArgumentException("Subcategory constant provided is not a valid category");
		} else {
			this.category = subCategoryConstant;
		}
		return this;
	}
	
	public ActionData setSubActions(String[] subActions) {
		this.subActions = subActions;
		return this;
	}
	
	public ActionData setSubAction(String subAction) {
		this.subActions = new String[] {subAction};
		return this;
	}
	
	public ActionRequest getActionRequest() {
		return this.actionRequest;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getInfo() {
		return this.info;	
	}
	
	public String getCategory() {
		return this.category;
	}
	
	public String getSubCategory() {
		return this.subCategory;
	}
	
	public String[] getSubActions() {
		return this.subActions;
	}
	
	public Permission getMinimumPermission() {
		return this.minimumPermission;
	}
 }
