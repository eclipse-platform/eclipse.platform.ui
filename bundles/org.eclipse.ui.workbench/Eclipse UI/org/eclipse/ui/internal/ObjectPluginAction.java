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
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
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
	/**
	 * The configuration element attribute for the identifier of the action
	 * which this action is intended to override (i.e., replace).
	 */
	public static final String ATT_OVERRIDE_ACTION_ID = "overrideActionId";//$NON-NLS-1$

    private String overrideActionId;

    private IWorkbenchPart activePart;

    /**
	 * Constructs a new ObjectPluginAction.
	 * 
	 * @param actionElement
	 *            The configuration element used to construct this action; must
	 *            not be <code>null</code>.
	 * @param id
	 *            The identifier for this action; must not be <code>null</code>.
	 * @param style
	 *            The style bits
	 */
    public ObjectPluginAction(IConfigurationElement actionElement, String id,
            int style) {
        super(actionElement, id, style);
        overrideActionId = actionElement.getAttribute(ATT_OVERRIDE_ACTION_ID);
    }

    /* (non-Javadoc)
     * Method declared on PluginAction.
     */
    protected void initDelegate() {
        super.initDelegate();
		final IActionDelegate actionDelegate = getDelegate();
		if (actionDelegate instanceof IObjectActionDelegate
				&& activePart != null) {
			final IObjectActionDelegate objectActionDelegate = (IObjectActionDelegate) actionDelegate;
			final ISafeRunnable runnable = new ISafeRunnable() {
				public void run() throws Exception {
					objectActionDelegate.setActivePart(ObjectPluginAction.this,
							activePart);
				}

				public void handleException(Throwable exception) {
					// Do nothing.
				}
			};
			Platform.run(runnable);
		}
	}

    /**
	 * Sets the active part for the delegate.
	 * <p>
	 * This method will be called every time the action appears in a popup menu.
	 * The targetPart may change with each invocation.
	 * </p>
	 * 
	 * @param targetPart
	 *            the new part target
	 */
    public void setActivePart(IWorkbenchPart targetPart) {
        activePart = targetPart;
        IActionDelegate delegate = getDelegate();
        if (delegate instanceof IObjectActionDelegate && activePart != null) {
			final IObjectActionDelegate objectActionDelegate = (IObjectActionDelegate) delegate;
			final ISafeRunnable runnable = new ISafeRunnable() {
				public void run() throws Exception {
					objectActionDelegate.setActivePart(ObjectPluginAction.this,
							activePart);
				}

				public void handleException(Throwable exception) {
					// Do nothing.
				}
			};
			Platform.run(runnable);
		}
	}

    /**
     * Returns the action identifier this action overrides.
     * 
     * @return the action identifier to override or <code>null</code>
     */
    public String getOverrideActionId() {
        return overrideActionId;
    }
}
