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
package org.eclipse.ui.views.navigator;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.RenameResourceAction;

/**
 * The ResourceNavigatorRenameAction is the rename action used by the
 * ResourceNavigator that also allows updating after rename.
 * <p>
 * As of 3.4 this action uses the LTK aware undoable operations.  The standard
 * undoable operations are still available.
 * </p>
 * @since 2.0
 */
public class ResourceNavigatorRenameAction extends RenameResourceAction {

    /**
     * Create a ResourceNavigatorRenameAction and use the tree of the supplied viewer
     * for editing.
     * @param shell Shell
     * @param treeViewer TreeViewer
     */
    public ResourceNavigatorRenameAction(Shell shell, TreeViewer treeViewer) {
        super(shell, treeViewer.getTree());
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
                INavigatorHelpContextIds.RESOURCE_NAVIGATOR_RENAME_ACTION);
    }


    /**
     * Handle the key release
     * @param event the SWT key event
     */
    public void handleKeyReleased(KeyEvent event) {
        if (event.keyCode == SWT.F2 && event.stateMask == 0 && isEnabled()) {
            run();
        }
    }
}
