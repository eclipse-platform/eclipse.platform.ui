package org.eclipse.debug.internal.ui.views;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.ui.AbstractDebugView;

/**
 * A debug view that uses an event handler to update its
 * view/viewer.
 */
public abstract class AbstractDebugEventHandlerView extends AbstractDebugView {

	/**
	 * Event handler for this view
	 */
	private AbstractDebugEventHandler fEventHandler;

	/**
	 * Sets the event handler for this view
	 * 
	 * @param eventHandler event handler
	 */
	protected void setEventHandler(AbstractDebugEventHandler eventHandler) {
		fEventHandler = eventHandler;
	}
	
	/**
	 * Returns the event handler for this view
	 * 
	 * @return The event handler for this view
	 */
	protected AbstractDebugEventHandler getEventHandler() {
		return fEventHandler;
	}	
	
	/**
	 * @see IWorkbenchPart#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (getEventHandler() != null) {
			getEventHandler().dispose();
		}	
	}
}
