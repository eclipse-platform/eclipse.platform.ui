/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.internal.markers;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;

class ActionSelectAll extends SelectionProviderAction {
	
	protected MarkerRegistry registry;

	/**
	 * @param provider
	 * @param text
	 */
	protected ActionSelectAll(ISelectionProvider provider, MarkerRegistry registry) {
		super(provider, Messages.getString("selectAllAction.title")); //$NON-NLS-1$
		this.registry = registry;
		setEnabled(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		StructuredSelection newSelection = new StructuredSelection(registry.getElements());
		super.getSelectionProvider().setSelection(newSelection);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(!selection.isEmpty());
	}
}
