package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
public ObjectPluginAction(IConfigurationElement actionElement, String runAttribute,String definitionId) {
	super(actionElement, runAttribute,definitionId);
}

/** 
 * Initialize an action delegate.
 * Subclasses may override this.
 */
protected IActionDelegate initDelegate(Object obj) 
	throws WorkbenchException
{
	IActionDelegate result = super.initDelegate(obj);
	if (obj instanceof IObjectActionDelegate && activePart != null) {
		((IObjectActionDelegate)obj).setActivePart(this, activePart);
	}
	return result;
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
