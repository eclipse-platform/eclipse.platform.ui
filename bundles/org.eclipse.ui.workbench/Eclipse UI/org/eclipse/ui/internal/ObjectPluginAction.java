/************************************************************************
Copyright (c) 2000, 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * An object action extension in a popup menu.
 * <p>
 * For backward compatibility, the delegate object can implement either
 * <code>IActionDelegate</code> or <code>IObjectActionDelegate</code>.
 * </p>
 */
public class ObjectPluginAction extends PluginAction {
	private IWorkbenchPart activePart;
	
	/**
	 * Constructs a new ObjectPluginAction.
	 */
	public ObjectPluginAction(IConfigurationElement actionElement, String runAttribute, String definitionId, int style) {
		super(actionElement, runAttribute, definitionId, style);
	}

	/* (non-Javadoc)
	 * Method declared on PluginAction.
	 */
	protected void initDelegate() {
		super.initDelegate();
		if (getDelegate() instanceof IObjectActionDelegate && activePart != null)
			((IObjectActionDelegate)getDelegate()).setActivePart(this, activePart);
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
			 ((IObjectActionDelegate) delegate).setActivePart(this, activePart);
	}
}
