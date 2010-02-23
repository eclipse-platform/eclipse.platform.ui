/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.WorkbenchException;

/**
 * This class extends regular plugin action with the
 * additional requirement that the delegate has
 * to implement interface IViewActionDeelgate.
 * This interface has one additional method (init)
 * whose purpose is to initialize the delegate with
 * the view part in which the action is intended to run.
 */
public final class ViewPluginAction extends PartPluginAction {
    private IViewPart viewPart;

    /**
     * This class adds the requirement that action delegates
     * loaded on demand implement IViewActionDelegate
     */
    public ViewPluginAction(IConfigurationElement actionElement,
            IViewPart viewPart, String id, int style) {
        super(actionElement, id, style);
        this.viewPart = viewPart;
        registerSelectionListener(viewPart);
    }

    /* (non-Javadoc)
     * Method declared on PluginAction.
     */
    protected IActionDelegate validateDelegate(Object obj)
            throws WorkbenchException {
        if (obj instanceof IViewActionDelegate) {
			return (IViewActionDelegate) obj;
		} else {
			throw new WorkbenchException(
                    "Action must implement IViewActionDelegate"); //$NON-NLS-1$
		}
    }

    /* (non-Javadoc)
     * Method declared on PluginAction.
     */
    protected void initDelegate() {
        super.initDelegate();
        ((IViewActionDelegate) getDelegate()).init(viewPart);
    }

    /**
     * Returns true if the view has been set
     * The view may be null after the constructor is called and
     * before the view is stored.  We cannot create the delegate
     * at that time.
     */
    public boolean isOkToCreateDelegate() {
        return super.isOkToCreateDelegate() && viewPart != null;
    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.PluginAction#dispose()
	 */
	public void dispose() {
		unregisterSelectionListener(viewPart);
		super.dispose();
	}
}
