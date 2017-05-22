package igrek.mgr.robopath.dispatcher;

public interface IEventObserver {
	
	void registerEvents();
	
	void onEvent(AbstractEvent event);
	
}
