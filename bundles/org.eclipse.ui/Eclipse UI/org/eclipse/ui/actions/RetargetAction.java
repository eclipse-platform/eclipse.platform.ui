package org.eclipse.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Event;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.SubActionBars;

/**
 * A <code>RetargetAction</code> tracks the active part in the workbench.  
 * Each RetargetAction has an ID.  If the active part provides an action 
 * handler for the ID the enable state of the RetargetAction is determined
 * from the enable state of the handler.  If the active part does not 
 * provide an action handler then this action is disabled.
 * <p>
 * This class may be instantiated. It is not intented to be subclassed.
 * </p>
 *
 * @since 2.0 
 */
public class RetargetAction extends PartEventAction {
	/**
	 * The help listener assigned to this action, or <code>null</code> if none.
	 */
	private HelpListener localHelpListener;

	private boolean enableAccelerator = true;
	private IAction handler;
	private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			RetargetAction.this.propogateChange(event);
		}
	};
/**
 * Constructs a RetargetAction.
 */
public RetargetAction(String actionID, String actionDefId) {
	super("");
	initializeFromRegistry(actionDefId);
	setId(actionID);
	setEnabled(false);
	super.setHelpListener(new HelpListener() {
		public void helpRequested(HelpEvent e) {
			HelpListener listener = null;
			if (handler != null) {
				// if we have a handler, see if it has a help listener
				listener = handler.getHelpListener();
				if (listener == null)
					// use our own help listener
					listener = localHelpListener;
			}
			if (listener != null)
				// pass on the event
				listener.helpRequested(e);
		}
	});
}
/**
 * Enables the accelerator for this action. 
 *
 * @param boolean the new enable state
 */
public void enableAccelerator(boolean b) {
	enableAccelerator = b;
}
/* (non-Javadoc)
 * Retaget actions do not have accelerators.  It is up to the
 * part to hook the accelerator.
 */
public int getAccelerator() {
	if (enableAccelerator)
		return super.getAccelerator();
	else
		return 0;
}
/**
 * A workbench part has been activated. Try to connect
 * to it.
 *
 * @param part the workbench part that has been activated
 */
public void partActivated(IWorkbenchPart part) {
	super.partActivated(part);
	IWorkbenchPartSite site = part.getSite();
	SubActionBars bars = (SubActionBars) ((PartSite)site).getActionBars();
	bars.addPropertyChangeListener(propertyChangeListener);
	setActionHandler(bars.getGlobalActionHandler(getId()));
}
/**
 * A workbench part has been closed. 
 *
 * @param part the workbench part that has been closed
 */
public void partClosed(IWorkbenchPart part) {
	if (part == getActivePart())
		setActionHandler(null);
	super.partClosed(part);
}
/**
 * A workbench part has been deactivated. Disconnect from it.
 *
 * @param part the workbench part that has been deactivated
 */
public void partDeactivated(IWorkbenchPart part) {
	super.partDeactivated(part);
	IWorkbenchPartSite site = part.getSite();
	SubActionBars bars = (SubActionBars) ((PartSite)site).getActionBars();
	bars.removePropertyChangeListener(propertyChangeListener);
	setActionHandler(null);
}
/**
 * Either the action handler itself has changed, or the configured action handlers on the action bars have changed.
 * Update self.
 */
protected void propogateChange(PropertyChangeEvent event) {
	if (event.getProperty().equals(Action.ENABLED)) {
		Boolean bool = (Boolean) event.getNewValue();
		setEnabled(bool.booleanValue());
	}
	else if (event.getProperty().equals(SubActionBars.P_ACTION_HANDLERS)) {
		setActionHandler(((IActionBars) event.getSource()).getGlobalActionHandler(getId()));
	}
}

/**
 * Invoked when an action occurs. 
 */
public void run() {
	if (handler != null)
		handler.run();
}

/**
 * Invoked when an action occurs. 
 */
public void runWithEvent(Event event) {
	if (handler != null)
		handler.runWithEvent(event);
}
/**
 * Set the action handler.  Update self.
 */
protected void setActionHandler(IAction newHandler) {
	// Optimize.
	if (newHandler == handler)
		return;
		
	// Clear old action.
	if (handler != null) {
		handler.removePropertyChangeListener(propertyChangeListener);
		handler = null;
	}

	// Set new action.
	handler = newHandler;
	if (handler == null) {
		setEnabled(false);
	} else {
		setEnabled(handler.isEnabled());
		handler.addPropertyChangeListener(propertyChangeListener);
	}
}
/** 
 * The <code>RetargetAction</code> implementation of this method
 * declared on <code>IAction</code> stores the help listener in
 * a local field. The supplied listener is only used if there is
 * no hanlder.
 */
public void setHelpListener(HelpListener listener) {
	localHelpListener = listener;
}
}
