package components.operation;

public interface OperationRequest {
	
	public void execute(OperationEvent event);
	public OperationData initialize();

}