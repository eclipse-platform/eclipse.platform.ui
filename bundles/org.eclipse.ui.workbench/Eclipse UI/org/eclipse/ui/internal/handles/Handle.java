package org.eclipse.ui.internal.handles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.handles.IHandle;
import org.eclipse.ui.handles.IHandleEvent;
import org.eclipse.ui.handles.IHandleListener;
import org.eclipse.ui.handles.NotDefinedException;

public class Handle implements IHandle {

	private boolean defined;
	private IHandleEvent handleEvent;
	private List handleListeners;
	private String id;
	private Object object;

	public Handle(String id) {
		this.id = id;
	}
	
	public void addHandleListener(IHandleListener handleListener) {
		if (handleListener == null)
			throw new NullPointerException();
		
		if (handleListeners == null)
			handleListeners = new ArrayList();
		
		if (!handleListeners.contains(handleListener))
			handleListeners.add(handleListener);
	}

	public void define(Object object) {
		if (this.object != object && this.defined != true) {
			this.object = object;
			this.defined = true;
		}
	}
	
	public String getId() {
		return id;
	}

	public Object getObject()
		throws NotDefinedException {
		if (!defined) 
			throw new NotDefinedException();
			
		return object;
	}

	public boolean isDefined() {
		return defined;
	}

	public void removeHandleListener(IHandleListener handleListener) {
		if (handleListener == null)
			throw new NullPointerException();

		if (handleListeners != null) {
			handleListeners.remove(handleListener);
			
			if (handleListeners.isEmpty())
				handleListeners = null;
		}
	}
	
	public void undefine() {
		if (this.defined != false && this.object != null) {
			this.defined = false;
			this.object = null;
			fireHandleChanged();
		}
	}
	
	private void fireHandleChanged() {
		if (handleListeners != null) {
			// TODO copying to avoid ConcurrentModificationException
			Iterator iterator = new ArrayList(handleListeners).iterator();			
			
			if (iterator.hasNext()) {
				if (handleEvent == null)
					handleEvent = new HandleEvent(this);
				
				while (iterator.hasNext())	
					((IHandleListener) iterator.next()).handleChanged(handleEvent);
			}							
		}			
	}		
}
