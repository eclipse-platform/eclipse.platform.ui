/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * ActionMarkCompleted is the action for marking task completion.
 * 
 */
public class ActionMarkCompleted extends MarkerSelectionProviderAction {

	/**
	 * Create a new instance of the reciever.
	 * 
	 * @param provider
	 */
	public ActionMarkCompleted(ISelectionProvider provider) {
		super(provider, MarkerMessages.markCompletedAction_title);
		setEnabled(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		try {
			ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) {

					IMarker[] markers = getSelectedMarkers();
					for (int i = 0; i < markers.length; i++) {
						try {
							markers[i].setAttribute(IMarker.DONE, true);
						} catch (CoreException e) {
							Util.log(e);
						}

					}

				}
			}, null);
		} catch (CoreException e) {
			Util.log(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(false);
		if (selection == null || selection.isEmpty()) {
			return;
		}
		for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
			Object obj = iterator.next();
			if (!(obj instanceof ConcreteMarker)) {
				return;
			}
			IMarker marker = ((ConcreteMarker) obj).getMarker();
			if (!marker.getAttribute(IMarker.USER_EDITABLE, true)) {
				return;
			}
			if (marker.getAttribute(IMarker.DONE, false)) {
				return;
			}
		}
		setEnabled(true);
	}
}
