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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * The abstract superclass for actions that listen to selection changes
 * from a particular selection provider. This implementation splits the current
 * selection along structured/unstructured lines, providing a convenient place
 * to monitor selection changes that require adjusting action state.
 * <p>
 * Subclasses must implement the following <code>IAction</code> method:
 * <ul>
 *   <li><code>run</code> - to do the action's work</li>
 * </ul>
 * </p>
 * <p>
 * Subclasses may reimplement either of the following methods:
 * <ul>
 *   <li><code>selectionChanged(IStructuredSelection)</code></li> 
 *   <li><code>selectionChanged(ISelection)</code></li> 
 * </ul>
 * </p>
 */
public abstract class SelectionProviderAction extends Action implements
        ISelectionChangedListener {

    /**
     * The selection provider that is the target of this action.
     */
    private ISelectionProvider provider;

    /**
     * Creates a new action with the given text that monitors selection changes
     * within the given selection provider.
     * The resulting action is added as a listener on the selection provider.
     *
     * @param provider the selection provider that will provide selection notification
     * @param text the string used as the text for the action, 
     *   or <code>null</code> if there is no text
     */
    protected SelectionProviderAction(ISelectionProvider provider, String text) {
        super(text);
        this.provider = provider;
        provider.addSelectionChangedListener(this);
    }

    /**
     * Disposes this action by removing it as a listener from the selection provider.
     * This must be called by the creator of the action when the action is no longer needed.
     */
    public void dispose() {
        provider.removeSelectionChangedListener(this);
    }

    /**
     * Returns the current selection in the selection provider.
     *
     * @return the current selection in the selection provider
     */
    public ISelection getSelection() {
        return provider.getSelection();
    }

    /**
     * Returns the selection provider that is the target of this action.
     *
     * @return the target selection provider of this action
     */
    public ISelectionProvider getSelectionProvider() {
        return provider;
    }

    /**
     * Returns the current structured selection in the selection provider, or an
     * empty selection if nothing is selected or if selection does not include
     * objects (for example, raw text).
     *
     * @return the current structured selection in the selection provider
     */
    public IStructuredSelection getStructuredSelection() {
        ISelection selection = provider.getSelection();
        if (selection instanceof IStructuredSelection) {
			return (IStructuredSelection) selection;
		} else {
			return new StructuredSelection();
		}
    }

    /**
     * Notifies this action that the given (non-structured) selection has changed
     * in the selection provider.
     * <p>
     * The <code>SelectionProviderAction</code> implementation of this method
     * does nothing. Subclasses may reimplement to react to this selection change.
     * </p>
     *
     * @param selection the new selection
     */
    public void selectionChanged(ISelection selection) {
    }

    /**
     * Notifies this action that the given structured selection has changed
     * in the selection provider.
     * <p>
     * The <code>SelectionProviderAction</code> implementation of this method
     * does nothing. Subclasses may reimplement to react to this selection change.
     * </p>
     *
     * @param selection the new selection
     */
    public void selectionChanged(IStructuredSelection selection) {
        // Hook in subclass.
    }

    /**
     * The <code>SelectionProviderAction</code> implementation of this 
     * <code>ISelectionChangedListener</code> method calls 
     * <code>selectionChanged(IStructuredSelection)</code> if the selection is
     * a structured selection but <code>selectionChanged(ISelection)</code> if it is
     * not. Subclasses should override either of those methods method to react to
     * selection changes.
     */
    public final void selectionChanged(SelectionChangedEvent event) {
        ISelection selection = event.getSelection();
        if (selection instanceof IStructuredSelection) {
			selectionChanged((IStructuredSelection) selection);
		} else {
			selectionChanged(selection);
		}
    }
}
