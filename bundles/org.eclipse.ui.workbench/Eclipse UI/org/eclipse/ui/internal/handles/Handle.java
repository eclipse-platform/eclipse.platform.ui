package org.eclipse.ui.internal.handles;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.internal.handles.api.IHandle;
import org.eclipse.ui.internal.handles.api.IHandleEvent;
import org.eclipse.ui.internal.handles.api.IHandleListener;
import org.eclipse.ui.internal.handles.api.NotDefinedException;

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

		if (handleListeners != null)
			handleListeners.remove(handleListener);
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
			for (int i = 0; i < handleListeners.size(); i++) {
				if (handleEvent == null)
					handleEvent = new HandleEvent(this);
							
				((IHandleListener) handleListeners.get(i)).handleChanged(handleEvent);
			}				
		}	
	}		
}
