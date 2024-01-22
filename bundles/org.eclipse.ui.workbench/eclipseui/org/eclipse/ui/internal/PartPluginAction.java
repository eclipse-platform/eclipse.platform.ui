/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * This class adds to the PluginAction support by setting itself up for work
 * within a WorkbenchPart. The main difference is that it is capable of
 * processing local selection changes within a part.
 */
public class PartPluginAction extends PluginAction {
	/**
	 * PartPluginAction constructor.
	 */
	public PartPluginAction(IConfigurationElement actionElement, String id, int style) {
		super(actionElement, id, style);
	}

	/**
	 * Registers this action as a listener of the workbench part.
	 */
	protected void registerSelectionListener(IWorkbenchPart aPart) {
		ISelectionProvider selectionProvider = aPart.getSite().getSelectionProvider();
		if (selectionProvider != null) {
			selectionProvider.addSelectionChangedListener(this);
			selectionChanged(selectionProvider.getSelection());
		}
	}

	/**
	 * Unregisters this action as a listener of the workbench part.
	 */
	protected void unregisterSelectionListener(IWorkbenchPart aPart) {
		IWorkbenchPartSite site = aPart.getSite();
		if (site == null) {
			return;
		}
		ISelectionProvider selectionProvider = site.getSelectionProvider();
		if (selectionProvider != null) {
			selectionProvider.removeSelectionChangedListener(this);
		}
	}
}
