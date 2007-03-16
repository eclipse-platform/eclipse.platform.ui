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

package org.eclipse.ui.views.markers.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * The ActionAddGlobalTask is the action for adding a global task.
 *
 */
public class ActionAddGlobalTask extends Action {

    private static final String ENABLED_IMAGE_PATH = "elcl16/addtsk_tsk.gif"; //$NON-NLS-1$

    private IWorkbenchPart part;

    /**
     * Create a new instance of the global task.
     * @param part
     */
    public ActionAddGlobalTask(IWorkbenchPart part) {
        setText(MarkerMessages.addGlobalTaskAction_title); 
        setImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor(ENABLED_IMAGE_PATH));
        setToolTipText(MarkerMessages.addGlobalTaskAction_tooltip);
        this.part = part;
    }

    public void run() {
        DialogTaskProperties dialog = new DialogTaskProperties(part.getSite()
                .getShell(), MarkerMessages.addGlobalTaskDialog_title);
        dialog.open();
    }
}
