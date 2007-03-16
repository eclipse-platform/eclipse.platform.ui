/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms;

import org.eclipse.jface.viewers.ISelection;

/**
 * Form parts can implement this interface if they want to be 
 * notified when another part on the same form changes selection 
 * state.
 * 
 * @see IFormPart
 * @since 3.0
 */
public interface IPartSelectionListener {
	/**
	 * Called when the provided part has changed selection state.
	 * 
	 * @param part
	 *            the selection source
	 * @param selection
	 *            the new selection
	 */
	public void selectionChanged(IFormPart part, ISelection selection);
}
