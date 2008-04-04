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
package org.eclipse.ui.help;

import java.util.ArrayList;

import org.eclipse.core.runtime.Assert;
import org.eclipse.help.IContext;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.widgets.Control;

/**
 * For determining the help context for controls in a dialog page.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @deprecated nested contexts are no longer supported by the help support system
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DialogPageContextComputer implements IContextComputer {
    private IDialogPage page;

    private ArrayList contextList;

    private Object context;

    /**
     * Creates a new context computer for the given dialog page and help context.
     *
     * @param page the dialog page
     * @param helpContext a single help context id (type <code>String</code>) or
     *  help context object (type <code>IContext</code>)
     */
    public DialogPageContextComputer(IDialogPage page, Object helpContext) {
        Assert.isTrue(helpContext instanceof String
                || helpContext instanceof IContext);
        this.page = page;
        context = helpContext;
    }

    /**
     * Add the contexts to the context list.
     *
     * @param object the contexts (<code>Object[]</code> or <code>IContextComputer</code>)
     * @param event the help event 
     */
    private void addContexts(Object object, HelpEvent event) {
        Assert.isTrue(object instanceof Object[]
                || object instanceof IContextComputer
                || object instanceof String);

        if (object instanceof String) {
            contextList.add(object);
            return;
        }

        Object[] contexts;
        if (object instanceof IContextComputer) {
			// get local contexts
            contexts = ((IContextComputer) object).getLocalContexts(event);
		} else {
			contexts = (Object[]) object;
		}

        // copy the contexts into our list	
        for (int i = 0; i < contexts.length; i++) {
			contextList.add(contexts[i]);
		}
    }

    /**
     * Add the contexts for the given control to the context list.
     *
     * @param control the control from which to obtain the contexts
     * @param event the help event 
     */
    private void addContextsForControl(Control control, HelpEvent event) {
        // See if there is are help contexts on the control
        Object object = WorkbenchHelp.getHelp(control);

        if (object == null || object == this) {
			// We need to check for this in order to avoid recursion
            return;
		}

        addContexts(object, event);
    }

    /* (non-Javadoc)
     * Method declared on IContextComputer.
     */
    public Object[] computeContexts(HelpEvent event) {
        contextList = new ArrayList();

        // Add the local context
        contextList.add(context);

        // Add the contexts for the page
        addContextsForControl(page.getControl(), event);

        // Add the contexts for the container shell	
        addContextsForControl(page.getControl().getShell(), event);

        // Return the contexts
        return contextList.toArray();
    }

    /* (non-Javadoc)
     * Method declared on IContextComputer.
     */
    public Object[] getLocalContexts(HelpEvent event) {
        return new Object[] { context };
    }
}
