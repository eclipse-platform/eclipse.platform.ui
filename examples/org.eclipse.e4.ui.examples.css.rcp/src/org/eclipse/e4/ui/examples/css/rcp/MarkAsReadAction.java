/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.examples.css.rcp;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;


public class MarkAsReadAction extends Action {

    private final IWorkbenchWindow window;

    MarkAsReadAction(String text, IWorkbenchWindow window) {
        super(text);
        this.window = window;
        // The id is used to refer to the action in a menu or toolbar
        setId(ICommandIds.CMD_MARK_AS_READ);
        // Associate the action with a pre-defined command, to allow key bindings.
        setActionDefinitionId(ICommandIds.CMD_MARK_AS_READ);
        setImageDescriptor(org.eclipse.e4.ui.examples.css.rcp.Activator.getImageDescriptor("/icons/sample3.gif"));
    }

    public void run() {
        //Mark the message as read
    	
    	//TODO: Not the recommended way to do this but it's because this examples uses views not editors
		IWorkbenchPart part = window.getActivePage().getActivePart();		
		IViewReference[] viewRefs = window.getActivePage().getViewReferences();

		for (int i = 0; i < viewRefs.length; i++) {		
			IViewReference viewReference = viewRefs[i];
			if(viewReference.getId().equals(View.ID)) {
				View messageView = (View) viewReference.getPart(false);
				if(messageView.isTopMost()) {
					messageView.markAsRead();
					return;
				}
			}
		}
    }
}