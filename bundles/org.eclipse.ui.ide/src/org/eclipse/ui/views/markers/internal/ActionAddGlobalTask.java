/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPart;

public class ActionAddGlobalTask extends Action {

    private static final String ENABLED_IMAGE_PATH = "elcl16/addtsk_tsk.gif"; //$NON-NLS-1$

    private IWorkbenchPart part;

    public ActionAddGlobalTask(IWorkbenchPart part) {
        setText(Messages.getString("addGlobalTaskAction.title")); //$NON-NLS-1$
        setImageDescriptor(ImageFactory.getImageDescriptor(ENABLED_IMAGE_PATH));
        setToolTipText(Messages.getString("addGlobalTaskAction.tooltip")); //$NON-NLS-1$
        this.part = part;
    }

    public void run() {
        DialogTaskProperties dialog = new DialogTaskProperties(part.getSite()
                .getShell(), Messages.getString("addGlobalTaskDialog.title")); //$NON-NLS-1$
        dialog.open();
    }
}