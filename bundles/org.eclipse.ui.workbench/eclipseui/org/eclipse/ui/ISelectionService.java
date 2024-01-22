/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 * This service can be acquired from your service locator:
 * </p>
 *
 * <pre>
 * ISelectionService service = (ISelectionService) getSite().getService(ISelectionService.class);
 * </pre>
 *
 * This service is not available globally, only from the workbench window level
 * down.
 *
 * @see org.eclipse.ui.ISelectionListener
 * @see org.eclipse.ui.INullSelectionListener
 * @see org.eclipse.ui.services.IServiceLocator#getService(Class)
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ISelectionService {
	/**
	 * Adds the given selection listener. Has no effect if an identical listener is
	 * already registered.
	 * <p>
	 * <b>Note:</b> listeners should be removed when no longer necessary. If not,
	 * they will be removed when the IServiceLocator used to acquire this service is
	 * disposed.
	 * </p>
	 * <p>
	 * <b>Note:</b> only update the UI when the UI is visible. See the
	 * {@link SelectionListenerFactory} for listeners that will only be called when
	 * the UI is visible to the user.
	 * </p>
	 *
	 *
	 * @param listener a selection listener
	 * @see #removeSelectionListener(ISelectionListener)
	 * @see SelectionListenerFactory#createVisibleListener(IWorkbenchPart,
	 *      ISelectionListener)
	 * @see SelectionListenerFactory#createVisibleSelfMutedListener(IWorkbenchPart,
	 *      ISelectionListener)
	 */
	void addSelectionListener(ISelectionListener listener);

	/**
	 * Adds a part-specific selection listener which is notified when selection
	 * changes in the part with the given id. This is independent of part activation
	 * - the part need not be active for notification to be sent.
	 * <p>
	 * When the part is created, the listener is passed the part's initial
	 * selection. When the part is disposed, the listener is passed a
	 * <code>null</code> selection, but only if the listener implements
	 * <code>INullSelectionListener</code>.
	 * </p>
	 * <p>
	 * <b>Note:</b> this will not correctly track editor parts as each editor does
	 * not have a unique partId.
	 * </p>
	 * <p>
	 * <b>Note:</b> listeners should be removed when no longer necessary. If not,
	 * they will be removed when the IServiceLocator used to acquire this service is
	 * disposed.
	 * </p>
	 * <p>
	 * <b>Note:</b> only update the UI when the UI is visible. See the
	 * {@link SelectionListenerFactory} for listeners that will only be called when
	 * the UI is visible to the user.
	 * </p>
	 *
	 * @param partId   the id of the part to track
	 * @param listener a selection listener
	 * @since 2.0
	 * @see #removeSelectionListener(String, ISelectionListener)
	 * @see SelectionListenerFactory#createVisibleListener(IWorkbenchPart,
	 *      ISelectionListener)
	 * @see SelectionListenerFactory#createVisibleSelfMutedListener(IWorkbenchPart,
	 *      ISelectionListener)
	 */
	void addSelectionListener(String partId, ISelectionListener listener);

	/**
	 * Adds the given post selection listener.It is equivalent to selection changed
	 * if the selection was triggered by the mouse but it has a delay if the
	 * selection is triggered by the keyboard arrows. Has no effect if an identical
	 * listener is already registered.
	 *
	 * Note: Works only for StructuredViewer(s).
	 * <p>
	 * <b>Note:</b> listeners should be removed when no longer necessary. If not,
	 * they will be removed when the IServiceLocator used to acquire this service is
	 * disposed.
	 * </p>
	 * <p>
	 * <b>Note:</b> only update the UI when the UI is visible. See the
	 * {@link SelectionListenerFactory} for listeners that will only be called when
	 * the UI is visible to the user.
	 * </p>
	 *
	 *
	 * @param listener a selection listener
	 * @see #removePostSelectionListener(ISelectionListener)
	 * @see SelectionListenerFactory#createVisibleListener(IWorkbenchPart,
	 *      ISelectionListener)
	 * @see SelectionListenerFactory#createVisibleSelfMutedListener(IWorkbenchPart,
	 *      ISelectionListener)
	 */
	void addPostSelectionListener(ISelectionListener listener);

	/**
	 * Adds a part-specific selection listener which is notified when selection
	 * changes in the part with the given id. This is independent of part activation
	 * - the part need not be active for notification to be sent.
	 * <p>
	 * When the part is created, the listener is passed the part's initial
	 * selection. When the part is disposed, the listener is passed a
	 * <code>null</code> selection, but only if the listener implements
	 * <code>INullSelectionListener</code>.
	 * </p>
	 * <p>
	 * <b>Note:</b> this will not correctly track editor parts as each editor does
	 * not have a unique partId.
	 * </p>
	 * <p>
	 * <b>Note:</b> listeners should be removed when no longer necessary. If not,
	 * they will be removed when the IServiceLocator used to acquire this service is
	 * disposed.
	 * </p>
	 * <p>
	 * <b>Note:</b> only update the UI when the UI is visible. See the
	 * {@link SelectionListenerFactory} for listeners that will only be called when
	 * the UI is visible to the user.
	 * </p>
	 *
	 * @param partId   the id of the part to track
	 * @param listener a selection listener
	 * @since 2.0
	 * @see #removePostSelectionListener(String, ISelectionListener)
	 * @see SelectionListenerFactory#createVisibleListener(IWorkbenchPart,
	 *      ISelectionListener)
	 * @see SelectionListenerFactory#createVisibleSelfMutedListener(IWorkbenchPart,
	 *      ISelectionListener)
	 */
	void addPostSelectionListener(String partId, ISelectionListener listener);

	/**
	 * Returns the current selection in the active part. If the selection in the
	 * active part is <em>undefined</em> (the active part has no selection provider)
	 * the result will be <code>null</code>.
	 *
	 * @return the current selection, or <code>null</code> if undefined
	 */
	ISelection getSelection();

	/**
	 * Returns the current selection in the part with the given id. If the part is
	 * not open, or if the selection in the active part is <em>undefined</em> (the
	 * active part has no selection provider) the result will be <code>null</code>.
	 *
	 * @param partId the id of the part
	 * @return the current selection, or <code>null</code> if undefined
	 * @since 2.0
	 */
	ISelection getSelection(String partId);

	/**
	 * Removes the given selection listener. Has no effect if an identical listener
	 * is not registered.
	 *
	 * @param listener a selection listener
	 */
	void removeSelectionListener(ISelectionListener listener);

	/**
	 * Removes the given part-specific selection listener. Has no effect if an
	 * identical listener is not registered for the given part id.
	 *
	 * @param partId   the id of the part to track
	 * @param listener a selection listener
	 * @since 2.0
	 */
	void removeSelectionListener(String partId, ISelectionListener listener);

	/**
	 * Removes the given post selection listener. Has no effect if an identical
	 * listener is not registered.
	 *
	 * @param listener a selection listener
	 */
	void removePostSelectionListener(ISelectionListener listener);

	/**
	 * Removes the given part-specific post selection listener. Has no effect if an
	 * identical listener is not registered for the given part id.
	 *
	 * @param partId   the id of the part to track
	 * @param listener a selection listener
	 * @since 2.0
	 */
	void removePostSelectionListener(String partId, ISelectionListener listener);
}
