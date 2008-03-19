/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 124684)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import java.util.Arrays;
import java.util.Set;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.viewers.IViewerObservableSet;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * An observable set that tracks the checked elements in a CheckboxTableViewer
 * or CheckboxTreeViewer
 * 
 * @since 1.2
 */
public class CheckboxViewerCheckedElementsObservableSet extends
		CheckableCheckedElementsObservableSet implements IViewerObservableSet {
	private StructuredViewer viewer;

	/**
	 * Constructs a new instance on the given realm and checkable.
	 * 
	 * @param realm
	 *            the observable's realm
	 * @param viewer
	 *            the CheckboxTableViewer viewer to track.
	 * @param elementType
	 *            type of elements in the set
	 */
	public CheckboxViewerCheckedElementsObservableSet(Realm realm,
			CheckboxTableViewer viewer, Object elementType) {
		super(realm, viewer, elementType, createElementSet(viewer));
		this.viewer = viewer;
	}

	/**
	 * Constructs a new instance on the given realm and checkable.
	 * 
	 * @param realm
	 *            the observable's realm
	 * @param viewer
	 *            the CheckboxTreeViewer viewer to track.
	 * @param elementType
	 *            type of elements in the set
	 */
	public CheckboxViewerCheckedElementsObservableSet(Realm realm,
			CheckboxTreeViewer viewer, Object elementType) {
		super(realm, viewer, elementType, createElementSet(viewer));
		this.viewer = viewer;
	}

	Set createDiffSet() {
		return ViewerElementSet.withComparer(viewer.getComparer());
	}

	private static Set createElementSet(CheckboxTableViewer viewer) {
		Set set = ViewerElementSet.withComparer(viewer.getComparer());
		set.addAll(Arrays.asList(viewer.getCheckedElements()));
		return set;
	}

	private static Set createElementSet(CheckboxTreeViewer viewer) {
		Set set = ViewerElementSet.withComparer(viewer.getComparer());
		set.addAll(Arrays.asList(viewer.getCheckedElements()));
		return set;
	}

	public Viewer getViewer() {
		return viewer;
	}

	public synchronized void dispose() {
		viewer = null;
		super.dispose();
	}
}
