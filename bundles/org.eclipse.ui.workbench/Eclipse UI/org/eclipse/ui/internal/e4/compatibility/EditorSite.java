/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @since 3.5
 *
 */
public class EditorSite extends WorkbenchPartSite implements IEditorSite {

	EditorSite(MPart model, IWorkbenchPart part, IConfigurationElement element) {
		super(model, part, element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorSite#getActionBarContributor()
	 */
	public IEditorActionBarContributor getActionBarContributor() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorSite#getActionBars()
	 */
	public IActionBars getActionBars() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorSite#registerContextMenu(org.eclipse.jface.action.MenuManager, org.eclipse.jface.viewers.ISelectionProvider, boolean)
	 */
	public void registerContextMenu(MenuManager menuManager, ISelectionProvider selectionProvider,
			boolean includeEditorInput) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorSite#registerContextMenu(java.lang.String, org.eclipse.jface.action.MenuManager, org.eclipse.jface.viewers.ISelectionProvider, boolean)
	 */
	public void registerContextMenu(String menuId, MenuManager menuManager,
			ISelectionProvider selectionProvider, boolean includeEditorInput) {
		// TODO Auto-generated method stub

	}

}
