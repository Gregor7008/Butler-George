package components.actions;

public interface ActionRequest {
	
	public void execute(Action event);
	public ActionData initialize();

}