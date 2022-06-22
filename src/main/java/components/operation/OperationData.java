package components.operation;

import net.dv8tion.jda.api.Permission;

public class OperationData {
	
	public static String ADMINISTRATION = "admin";
	public static String MODERATION = "mod";
	
	public static String SETCHANNEL = "setch";
	public static String SETROLE = "setrl";
	
	private OperationRequest operationRequest = null;
	private String name = null;
	private String info = null;
	private String category = null;
	private String subCategory = null;
	private String[] subActions = null;
	private Permission minimumPermission = null;
	
	public OperationData(OperationRequest operationRequest) {
		this.operationRequest = operationRequest;
	}

	public OperationData setName(String name) {
		this.name = name;
		return this;
	}
	
	public OperationData setInfo(String info) {
		this.info = info;
		return this;
	}
	
	public OperationData setMinimumPermission(Permission permission) {
		this.minimumPermission = permission;
		return this;
	}
	
	public OperationData setCategory(String categoryConstant) {
		if (!categoryConstant.equals(OperationData.ADMINISTRATION) || !categoryConstant.equals(OperationData.MODERATION)) {
			throw new IllegalArgumentException("Category constant provided is not a valid category");
		} else {
			this.category = categoryConstant;
		}
		return this;
	}
	
	public OperationData setSubCategory(String subCategoryConstant) {
		if (!subCategoryConstant.equals(OperationData.SETCHANNEL) || !subCategoryConstant.equals(OperationData.SETROLE)) {
			throw new IllegalArgumentException("Subcategory constant provided is not a valid category");
		} else {
			this.category = subCategoryConstant;
		}
		return this;
	}
	
	public OperationData setSubActions(String[] subActions) {
		this.subActions = subActions;
		return this;
	}
	
	public OperationData setSubAction(String subAction) {
		this.subActions = new String[] {subAction};
		return this;
	}
	
	public OperationRequest getActionRequest() {
		return this.operationRequest;
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
