package igrek.mgr.robopath.dispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import igrek.mgr.robopath.logger.Logs;

public class EventDispatcher {
	
	private static EventDispatcher instance = null;
	
	private static EventDispatcher getInstance() {
		if (instance == null) {
			instance = new EventDispatcher();
		}
		return instance;
	}
	
	private final Map<Class<? extends AbstractEvent>, List<IEventObserver>> eventObservers = new HashMap<>();
	
	private final List<AbstractEvent> eventsQueue = new ArrayList<>();
	
	private volatile boolean dispatching = false;
	
	private EventDispatcher() {
	}
	
	public static void registerEventObserver(Class<? extends AbstractEvent> eventClass, IEventObserver observer) {
		getInstance()._registerEventObserver(eventClass, observer);
	}
	
	public static void unregisterEventObserver(IEventObserver observer) {
		getInstance()._unregisterEventObserver(observer);
	}
	
	public static void sendEvent(AbstractEvent event) {
		getInstance()._sendEvent(event);
	}
	
	private void _registerEventObserver(Class<? extends AbstractEvent> eventClass, IEventObserver observer) {
		synchronized (eventObservers) {
			List<IEventObserver> observers = eventObservers.get(eventClass);
			if (observers == null) {
				observers = new ArrayList<>();
			}
			if (!observers.contains(observer)) {
				observers.add(observer);
			}
			eventObservers.put(eventClass, observers);
		}
	}
	
	private void _unregisterEventObserver(IEventObserver observer) {
		synchronized (eventObservers) {
			for (Class<? extends AbstractEvent> clazz : eventObservers.keySet()) {
				List<IEventObserver> observers = eventObservers.get(clazz);
				if (observers != null) {
					observers.removeIf(obs -> obs == observer);
				}
			}
		}
	}
	
	private void _sendEvent(AbstractEvent event) {
		synchronized (eventsQueue) {
			eventsQueue.add(event);
		}
		dispatchEvents();
	}
	
	private void clearEventObservers(Class<? extends AbstractEvent> eventClass) {
		List<IEventObserver> observers = eventObservers.get(eventClass);
		if (observers != null) {
			observers.clear();
		}
		eventObservers.put(eventClass, null);
	}
	
	private void dispatchEvents() {
		if (dispatching)
			return;
		dispatching = true;
		
		synchronized (eventsQueue) {
			while (!eventsQueue.isEmpty()) {
				dispatch(eventsQueue.get(0));
				eventsQueue.remove(0);
			}
		}
		
		dispatching = false;
	}
	
	private void dispatch(AbstractEvent event) {
		synchronized (eventObservers) {
			List<IEventObserver> observers = eventObservers.get(event.getClass());
			if (observers == null || observers.isEmpty()) {
				Logs.warn("no observer for event " + event.getClass().getName());
			}
			if (observers != null) {
				for (IEventObserver observer : observers) {
					observer.onEvent(event);
				}
			}
		}
	}
}
