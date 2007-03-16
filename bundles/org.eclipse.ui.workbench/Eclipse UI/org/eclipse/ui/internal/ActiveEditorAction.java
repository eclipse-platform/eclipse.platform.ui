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

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

/**
 * The abstract superclass for actions that depend on the active editor.
 * This implementation tracks the active editor (see <code>getActiveEditor</code>)
 * and updates the availability of the action when an editor becomes
 * active.
 * <p>
 * Subclasses must implement the following <code>IAction</code> method:
 * <ul>
 *   <li><code>run</code> - to do the action's work</li>
 * </ul>
 * </p>
 * <p>
 * Subclasses may extend any of the <code>IPartListener</code> methods if the
 * action availablity needs to be recalculated:
 * <ul>
 *   <li><code>partActivated</code></li> 
 *   <li><code>partDeactivated</code></li>
 *   <li><code>partOpened</code></li>
 *   <li><code>partClosed</code></li>
 *   <li><code>partBroughtToTop</code></li>
 * </ul>
 * </p>
 * <p>
 * Subclasses may extend any of the <code>IPageListener</code> methods if the
 * action availablity needs to be recalculated:
 * <ul>
 *   <li><code>pageActivated</code></li> 
 *   <li><code>pageClosed</code></li>
 *   <li><code>pageOpened</code></li>
 * </ul>
 * </p>
 * <p>
 * This method implements the <code>IPartListener</code> and
 * <code>IPageListener</code>interfaces, and automatically registers listeners
 * so that it can keep its enablement state up to date. Ordinarily, the
 * window's references to these listeners will be dropped automatically when
 * the window closes. However, if the client needs to get rid of an action
 * while the window is still open, the client must call 
 * {@link IWorkbenchAction#dispose dispose} to give the action an
 * opportunity to deregister its listeners and to perform any other cleanup.
 * </p>
 */
public abstract class ActiveEditorAction extends PageEventAction {

    private IEditorPart activeEditor;

    /**
     * Creates a new action with the given text.
     *
     * @param text the string used as the text for the action, 
     *   or <code>null</code> if there is no text
     * @param window the workbench window this action is
     *   registered with.
     */
    protected ActiveEditorAction(String text, IWorkbenchWindow window) {
        super(text, window);
        updateState();
    }

    /**
     * Notification that the active editor tracked
     * by the action is being activated.
     *
     * Subclasses may override.
     */
    protected void editorActivated(IEditorPart part) {
    }

    /**
     * Notification that the active editor tracked
     * by the action is being deactivated.
     *
     * Subclasses may override.
     */
    protected void editorDeactivated(IEditorPart part) {
    }

    /**
     * Return the active editor
     *
     * @return the page's active editor, and <code>null</code>
     *		if no active editor or no active page.
     */
    public final IEditorPart getActiveEditor() {
        return activeEditor;
    }

    /* (non-Javadoc)
     * Method declared on PageEventAction.
     */
    public void pageActivated(IWorkbenchPage page) {
        super.pageActivated(page);
        updateActiveEditor();
        updateState();
    }

    /* (non-Javadoc)
     * Method declared on PageEventAction.
     */
    public void pageClosed(IWorkbenchPage page) {
        super.pageClosed(page);
        updateActiveEditor();
        updateState();
    }

    /* (non-Javadoc)
     * Method declared on PartEventAction.
     */
    public void partActivated(IWorkbenchPart part) {
        super.partActivated(part);
        if (part instanceof IEditorPart) {
            updateActiveEditor();
            updateState();
        }
    }

    /* (non-Javadoc)
     * Method declared on PartEventAction.
     */
    public void partBroughtToTop(IWorkbenchPart part) {
        super.partBroughtToTop(part);
        if (part instanceof IEditorPart) {
            updateActiveEditor();
            updateState();
        }
    }

    /* (non-Javadoc)
     * Method declared on PartEventAction.
     */
    public void partClosed(IWorkbenchPart part) {
        super.partClosed(part);
        if (part instanceof IEditorPart) {
            updateActiveEditor();
            updateState();
        }
    }

    /* (non-Javadoc)
     * Method declared on PartEventAction.
     */
    public void partDeactivated(IWorkbenchPart part) {
        super.partDeactivated(part);
        if (part instanceof IEditorPart) {
            updateActiveEditor();
            updateState();
        }
    }

    /**
     * Set the active editor
     */
    private void setActiveEditor(IEditorPart part) {
        if (activeEditor == part) {
            return;
        }
        if (activeEditor != null) {
            editorDeactivated(activeEditor);
        }
        activeEditor = part;
        if (activeEditor != null) {
            editorActivated(activeEditor);
        }
    }

    /**
     * Update the active editor based on the current
     * active page.
     */
    private void updateActiveEditor() {
        if (getActivePage() == null) {
            setActiveEditor(null);
        } else {
            setActiveEditor(getActivePage().getActiveEditor());
        }
    }

    /**
     * Update the state of the action. By default, the action
     * is enabled if there is an active editor.
     *
     * Subclasses may override or extend this method.
     */
    protected void updateState() {
        setEnabled(getActiveEditor() != null);
    }

}
