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
package org.eclipse.ui.examples.readmetool;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

/**
 * This class is used to demonstrate editor action extensions.
 * An extension should be defined in the readme plugin.xml.
 */
public class EditorActionDelegate implements IEditorActionDelegate {
    private IEditorPart editor;

    /**
     * Creates a new EditorActionDelegate.
     */
    public EditorActionDelegate() {
        // do nothing
    }

    /* (non-Javadoc)
     * Method declared on IActionDelegate
     */
    public void run(IAction action) {
        MessageDialog.openInformation(editor.getSite().getShell(), MessageUtil
                .getString("Readme_Editor"), //$NON-NLS-1$
                MessageUtil.getString("Editor_Action_executed")); //$NON-NLS-1$
    }

    /** 
     * The <code>EditorActionDelegate</code> implementation of this
     * <code>IActionDelegate</code> method does nothing.
     *
     * Selection in the workbench has changed. Plugin provider
     * can use it to change the availability of the action
     * or to modify other presentation properties.
     *
     * <p>Action delegate cannot be notified about
     * selection changes before it is loaded. For that reason,
     * control of action's enable state should also be performed
     * through simple XML rules defined for the extension
     * point. These rules allow enable state control before
     * the delegate has been loaded.</p>
     */
    public void selectionChanged(IAction action, ISelection selection) {
        // do nothing
    }

    /** 
     * The <code>EditorActionDelegate</code> implementation of this
     * <code>IEditorActionDelegate</code> method remembers the active editor.
     *
     * The matching editor has been activated. Notification
     * guarantees that only editors that match the type for which 
     * this action has been registered will be tracked.
     *
     * @param action action proxy that represents this delegate in the workbench
     * @param editor the matching editor that has been activated
     */
    public void setActiveEditor(IAction action, IEditorPart editor) {
        this.editor = editor;
    }
}
