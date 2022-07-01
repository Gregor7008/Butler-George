package components.operations;

public class OperationData {
	
	private OperationEventHandler operationEventHandler = null;
	private String name = null;
	private String info = null;
	private SubOperationData[] subOperations = null;
	
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
	
	public SubOperationData[] getSubOperations() {
		return this.subOperations;
	}
 }
