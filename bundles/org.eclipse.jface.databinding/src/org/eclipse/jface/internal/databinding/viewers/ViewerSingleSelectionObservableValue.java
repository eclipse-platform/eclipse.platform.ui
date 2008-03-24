/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 137877
 *     Brad Reynolds - bug 164653
 *     Brad Reynolds - bug 147515
 *     Ashley Cambrell - bug 198906
 *******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.viewers.Viewer;

/**
 * Observes single selection of a <code>Viewer</code>.
 * 
 * @since 1.2
 */
public class ViewerSingleSelectionObservableValue extends
		SelectionProviderSingleSelectionObservableValue implements
		IViewerObservableValue {

	private Viewer viewer;

	/**
	 * @param realm
	 * @param viewer
	 */
	public ViewerSingleSelectionObservableValue(Realm realm, Viewer viewer) {
		super(realm, viewer);
		this.viewer = viewer;
	}

	public Viewer getViewer() {
		return viewer;
	}
}
