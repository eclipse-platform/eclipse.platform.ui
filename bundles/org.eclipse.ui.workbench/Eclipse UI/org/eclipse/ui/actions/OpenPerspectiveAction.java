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
package org.eclipse.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.util.Util;

/**
 * Opens a perspective.
 * 
 * @since 3.1
 */
public final class OpenPerspectiveAction extends Action implements
        IPluginContribution {

    /**
     * The perspective menu that will handle the execution of this action. This
     * allows subclasses of <code>PerspectiveMenu</code> to define custom
     * behaviour for these actions. This value should not be <code>null</code>.
     */
    private final PerspectiveMenu callback;

    /**
     * The descriptor for the perspective that this action should open. This
     * value is never <code>null</code>.
     */
    private final IPerspectiveDescriptor descriptor;

    /**
     * Constructs a new instance of <code>OpenPerspectiveAction</code>
     * 
     * @param window
     *            The workbench window in which this action is created; should
     *            not be <code>null</code>.
     * @param descriptor
     *            The descriptor for the perspective that this action should
     *            open; must not be <code>null</code>.
     * @param callback
     *            The perspective menu who will handle the actual execution of
     *            this action; should not be <code>null</code>.
     */
    public OpenPerspectiveAction(final IWorkbenchWindow window,
            final IPerspectiveDescriptor descriptor,
            final PerspectiveMenu callback) {
        super(Util.ZERO_LENGTH_STRING);

        this.descriptor = descriptor;
        this.callback = callback;

        final String label = descriptor.getLabel();
        setText(label);
        setToolTipText(label);
        setImageDescriptor(descriptor.getImageDescriptor());

        window.getWorkbench().getHelpSystem().setHelp(this,
                IWorkbenchHelpContextIds.OPEN_PERSPECTIVE_ACTION);
    }

  
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IAction#runWithEvent(org.eclipse.swt.widgets.Event)
     */
    public final void runWithEvent(final Event event) {
        callback.run(descriptor, new SelectionEvent(event));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.activities.support.IPluginContribution#getLocalId()
     */
    public String getLocalId() {
        return descriptor.getId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.activities.support.IPluginContribution#getPluginId()
     */
    public String getPluginId() {
        return descriptor instanceof IPluginContribution ? ((IPluginContribution) descriptor)
                .getPluginId()
                : null;
    }
}
