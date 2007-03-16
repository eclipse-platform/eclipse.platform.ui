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

package org.eclipse.ui.views.markers.internal;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * ActionTaskProperties is the action for setting a tasks properties.
 * 
 */
public class ActionTaskProperties extends MarkerSelectionProviderAction {

	private IWorkbenchPart part;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param part
	 * @param provider
	 */
	public ActionTaskProperties(IWorkbenchPart part, ISelectionProvider provider) {
		super(provider, MarkerMessages.propertiesAction_title);
		setEnabled(false);
		this.part = part;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {

		DialogMarkerProperties dialog = new DialogTaskProperties(part.getSite()
				.getShell());
		dialog.setMarker(getSelectedMarker());
		dialog.open();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(Util.isSingleConcreteSelection(selection));
	}
}
