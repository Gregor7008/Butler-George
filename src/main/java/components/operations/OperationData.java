package components.operations;

import net.dv8tion.jda.api.Permission;

public class OperationData {
	
	public static String ADMINISTRATION = "admin";
	public static String MODERATION = "mod";
	
	private OperationEventHandler operationEventHandler = null;
	private String name = null;
	private String info = null;
	private String category = null;
	private SubOperationData[] subOperations = null;
	private Permission minimumPermission = null;
	
	public OperationData(OperationEventHandler operationEventHandler) {
		this.operationEventHandler = operationEventHandler;
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
		if (!categoryConstant.equals(OperationData.ADMINISTRATION) && !categoryConstant.equals(OperationData.MODERATION)) {
			throw new IllegalArgumentException("Category constant provided is not a valid category");
		} else {
			this.category = categoryConstant;
		}
		return this;
	}
	
	public OperationData setSubOperations(SubOperationData[] subOperations) {
		this.subOperations = subOperations;
		return this;
	}
	
	public OperationData setSubOperation(SubOperationData subOperation) {
		this.subOperations = new SubOperationData[] {subOperation};
		return this;
	}
	
	public OperationEventHandler getOperationEventHandler() {
		return this.operationEventHandler;
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
	
	public SubOperationData[] getSubOperations() {
		return this.subOperations;
	}
	
	public Permission getMinimumPermission() {
		return this.minimumPermission;
	}
 }
