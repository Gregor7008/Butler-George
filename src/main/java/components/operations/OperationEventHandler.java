package components.operations;

public interface OperationEventHandler {
	
	public void execute(OperationEvent event);
	public OperationData initialize();

}