package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.ui.*;
import org.eclipse.core.runtime.*;

/**
 * An object action extension in a popup menu.
 */
public class ObjectPluginAction extends PluginAction {
	private IWorkbenchPart activePart;
/**
 * Constructs a new ObjectPluginAction.
 */
public ObjectPluginAction(IConfigurationElement actionElement, String runAttribute) {
	super(actionElement, runAttribute);
}
/**
 * Creates an instance of the delegate class.
 * At this point in time the delegate class must implement
 * IActionDelegate or IObjectActionDelegate.  We allow the flexibility
 * for backwards compatability.
 */
protected IActionDelegate createDelegate() {
	IActionDelegate delegate = super.createDelegate();
	if (delegate != null) {
		if (delegate instanceof IObjectActionDelegate && activePart != null)
			((IObjectActionDelegate)delegate).setActivePart(this, activePart);
	}
	return delegate;
}
/**
 * Sets the active part for the delegate.
 * <p>
 * This method will be called every time the action appears in a popup menu.  The
 * targetPart may change with each invocation.
 * </p>
 *
 * @param action the action proxy that handles presentation portion of the action
 * @param targetPart the new part target
 */
public void setActivePart(IWorkbenchPart targetPart) {
	activePart = targetPart;
	IActionDelegate delegate = getDelegate();
	if (delegate != null && delegate instanceof IObjectActionDelegate)
		((IObjectActionDelegate)delegate).setActivePart(this, activePart);
}
}
