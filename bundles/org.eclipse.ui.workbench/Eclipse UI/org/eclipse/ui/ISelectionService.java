/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.jface.viewers.ISelection;

/**
 * A selection service tracks the selection within an object.
 * <p>
 * A listener that wants to be notified when the selection becomes
 * <code>null</code> must implement the <code>INullSelectionListener</code>
 * interface.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * It can also be acquired from your service locator: e.g.
 * getSite().getService(ISelectionService.class)
 * </p>
 * @see org.eclipse.ui.ISelectionListener
 * @see org.eclipse.ui.INullSelectionListener
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ISelectionService {
    /**
     * Adds the given selection listener.
     * Has no effect if an identical listener is already registered.
	 * <p>
	 * <b>Note:</b> listeners should be removed when no longer necessary. If
	 * not, they will be removed when the IServiceLocator used to acquire this
	 * service is disposed.
	 * </p>
     *
     * @param listener a selection listener
     * @see #removeSelectionListener(ISelectionListener)
     */
    public void addSelectionListener(ISelectionListener listener);

    /**
     * Adds a part-specific selection listener which is notified when selection changes
     * in the part with the given id. This is independent of part activation - the part
     * need not be active for notification to be sent.
     * <p>
     * When the part is created, the listener is passed the part's initial selection.
     * When the part is disposed, the listener is passed a <code>null</code> selection,
     * but only if the listener implements <code>INullSelectionListener</code>.
     * </p>
     * <p>
     * Note: This will not correctly track editor parts as each editor does 
     * not have a unique partId.
     * </p>
	 * <p>
	 * <b>Note:</b> listeners should be removed when no longer necessary. If
	 * not, they will be removed when the IServiceLocator used to acquire this
	 * service is disposed.
	 * </p>
     *
     * @param partId the id of the part to track
     * @param listener a selection listener
     * @since 2.0
     * @see #removeSelectionListener(String, ISelectionListener)
     */
    public void addSelectionListener(String partId, ISelectionListener listener);

    /**
     * Adds the given post selection listener.It is equivalent to selection 
     * changed if the selection was triggered by the mouse but it has a 
     * delay if the selection is triggered by the keyboard arrows.
     * Has no effect if an identical listener is already registered.
     * 
     * Note: Works only for StructuredViewer(s).
	 * <p>
	 * <b>Note:</b> listeners should be removed when no longer necessary. If
	 * not, they will be removed when the IServiceLocator used to acquire this
	 * service is disposed.
	 * </p>
     *
     * @param listener a selection listener
     * @see #removePostSelectionListener(ISelectionListener)
     */
    public void addPostSelectionListener(ISelectionListener listener);

    /**
     * Adds a part-specific selection listener which is notified when selection changes
     * in the part with the given id. This is independent of part activation - the part
     * need not be active for notification to be sent.
     * <p>
     * When the part is created, the listener is passed the part's initial selection.
     * When the part is disposed, the listener is passed a <code>null</code> selection,
     * but only if the listener implements <code>INullSelectionListener</code>.
     * </p>
     * <p>
     * Note: This will not correctly track editor parts as each editor does 
     * not have a unique partId.
     * </p>
	 * <p>
	 * <b>Note:</b> listeners should be removed when no longer necessary. If
	 * not, they will be removed when the IServiceLocator used to acquire this
	 * service is disposed.
	 * </p>
     *
     * @param partId the id of the part to track
     * @param listener a selection listener
     * @since 2.0
     * @see #removePostSelectionListener(String, ISelectionListener)
     */
    public void addPostSelectionListener(String partId,
            ISelectionListener listener);

    /**
     * Returns the current selection in the active part.  If the selection in the
     * active part is <em>undefined</em> (the active part has no selection provider)
     * the result will be <code>null</code>.
     *
     * @return the current selection, or <code>null</code> if undefined
     */
    public ISelection getSelection();

    /**
     * Returns the current selection in the part with the given id.  If the part is not open,
     * or if the selection in the active part is <em>undefined</em> (the active part has no selection provider)
     * the result will be <code>null</code>.
     *
     * @param partId the id of the part
     * @return the current selection, or <code>null</code> if undefined
     * @since 2.0
     */
    public ISelection getSelection(String partId);

    /**
     * Removes the given selection listener.
     * Has no effect if an identical listener is not registered.
     *
     * @param listener a selection listener
     */
    public void removeSelectionListener(ISelectionListener listener);

    /**
     * Removes the given part-specific selection listener.
     * Has no effect if an identical listener is not registered for the given part id.
     *
     * @param partId the id of the part to track
     * @param listener a selection listener
     * @since 2.0
     */
    public void removeSelectionListener(String partId,
            ISelectionListener listener);

    /**
     * Removes the given post selection listener.
     * Has no effect if an identical listener is not registered.
     *
     * @param listener a selection listener
     */
    public void removePostSelectionListener(ISelectionListener listener);

    /**
     * Removes the given part-specific post selection listener.
     * Has no effect if an identical listener is not registered for the given part id.
     *
     * @param partId the id of the part to track
     * @param listener a selection listener
     * @since 2.0
     */
    public void removePostSelectionListener(String partId,
            ISelectionListener listener);
}
