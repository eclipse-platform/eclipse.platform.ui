/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.modeling;

import org.eclipse.e4.ui.services.IServiceConstants;

/**
 * This interface describes the workbench selection service
 * 
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ESelectionService {

	/**
	 * Due to the possibly misleading nature of this field's name, it has been replaced with
	 * {@link IServiceConstants#ACTIVE_SELECTION}. All clients of this API should change their
	 * references to <code>IServiceConstants.ACTIVE_SELECTION</code>.
	 */
	@Deprecated
	public static final String SELECTION = IServiceConstants.ACTIVE_SELECTION; // "in.selection";

	/**
	 * Sets the given selection in the active context
	 * 
	 * @param selection
	 *            the new selection
	 */
	public void setSelection(Object selection);

	/**
	 * Sets the given selection as the post selection for the active context
	 * 
	 * @param selection
	 *            the new selection
	 */
	public void setPostSelection(Object selection);

	/**
	 * Returns the current selection from the active context or <code>null</code> if the is nothing
	 * selected.
	 * 
	 * @return the current selection or <code>null</code>
	 */
	public Object getSelection();

	/**
	 * Returns the current selection from the the part with the given id. Returns <code>null</code>
	 * if there is no selection or the part does not exist.
	 * 
	 * @param partId
	 *            the id of the part to get the selection from
	 * @return the current selection in the part or <code>null</code>
	 */
	public Object getSelection(String partId);

	/**
	 * Adds the given {@link ISelectionListener} to the service
	 * 
	 * @param listener
	 *            the listener to register
	 */
	public void addSelectionListener(ISelectionListener listener);

	/**
	 * Removes the given {@link ISelectionListener} from the service
	 * 
	 * @param listener
	 *            the listener to unregister
	 */
	public void removeSelectionListener(ISelectionListener listener);

	/**
	 * Adds the {@link ISelectionListener} to the service for the part with the given id.
	 * 
	 * @param partId
	 *            the id of the part to add the listener for
	 * @param listener
	 *            the listener to register
	 */
	public void addSelectionListener(String partId, ISelectionListener listener);

	/**
	 * Removes the {@link ISelectionListener} from the service for the given part id.
	 * 
	 * @param partId
	 *            the id of the part to remove the listener for
	 * @param listener
	 *            the listener to unregister
	 */
	public void removeSelectionListener(String partId, ISelectionListener listener);

	/**
	 * Adds the {@link ISelectionListener} as a post selection listener for the service.
	 * 
	 * @param listener
	 *            the listener to register
	 */
	public void addPostSelectionListener(ISelectionListener listener);

	/**
	 * Removes the {@link ISelectionListener} as a post selection listener for the service.
	 * 
	 * @param listener
	 *            the listener to unregister
	 */
	public void removePostSelectionListener(ISelectionListener listener);

	/**
	 * Adds the {@link ISelectionListener} as a post selection listener for the part with the given
	 * id.
	 * 
	 * @param partId
	 *            the id of the part to add the listener for
	 * @param listener
	 *            the listener to register
	 */
	public void addPostSelectionListener(String partId, ISelectionListener listener);

	/**
	 * Removes the {@link ISelectionListener} as a post selection listener for the part with the
	 * given id.
	 * 
	 * @param partId
	 *            the id of the part to remove the listener for
	 * @param listener
	 *            the listener to unregister
	 */
	public void removePostSelectionListener(String partId, ISelectionListener listener);
}
