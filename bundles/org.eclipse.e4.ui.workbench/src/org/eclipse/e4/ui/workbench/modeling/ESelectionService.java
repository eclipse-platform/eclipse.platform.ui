/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
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
 * @since 1.0
 */
public interface ESelectionService {

	/**
	 * Due to the possibly misleading nature of this field's name, it has been replaced with
	 * {@link IServiceConstants#ACTIVE_SELECTION}. All clients of this API should change their
	 * references to <code>IServiceConstants.ACTIVE_SELECTION</code>.
	 */
	@Deprecated
	public static final String SELECTION = IServiceConstants.ACTIVE_SELECTION; // "in.selection";

	public void setSelection(Object selection);

	public void setPostSelection(Object selection);

	public Object getSelection();

	public Object getSelection(String partId);

	public void addSelectionListener(ISelectionListener listener);

	public void removeSelectionListener(ISelectionListener listener);

	public void addSelectionListener(String partId, ISelectionListener listener);

	public void removeSelectionListener(String partId, ISelectionListener listener);

	public void addPostSelectionListener(ISelectionListener listener);

	public void removePostSelectionListener(ISelectionListener listener);

	public void addPostSelectionListener(String partId, ISelectionListener listener);

	public void removePostSelectionListener(String partId, ISelectionListener listener);
}
