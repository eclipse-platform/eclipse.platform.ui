/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.modeling;

/**
 * @since 1.0
 */
public interface ESelectionService {

	public static final String SELECTION = "in.selection"; //$NON-NLS-1$

	public void setSelection(Object selection);

	public Object getSelection();

	public Object getSelection(String partId);

	public void addSelectionListener(ISelectionListener listener);

	public void removeSelectionListener(ISelectionListener listener);

	public void addSelectionListener(String partId, ISelectionListener listener);

	public void removeSelectionListener(String partId, ISelectionListener listener);
}
