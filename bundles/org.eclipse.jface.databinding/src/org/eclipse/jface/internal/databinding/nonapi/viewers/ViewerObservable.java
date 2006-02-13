/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.nonapi.viewers;

import java.util.Collection;

import org.eclipse.jface.internal.databinding.api.observable.list.IObservableList;
import org.eclipse.jface.internal.databinding.api.viewers.ObservableListContentProvider;
import org.eclipse.jface.internal.databinding.api.viewers.ViewerLabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * @since 3.2
 * 
 */
public class ViewerObservable {
	private final class MyLabelProvider extends ViewerLabelProvider {
		public void notifyElementsChanged(Collection changedElements) {
			fireChangeEvent(changedElements);
		}
	}

	private StructuredViewer viewer;

	private ObservableListContentProvider contentProvider;

	private IObservableList observableList;

	private IObservableIndexMapping observableIndexMapping;

	private MyLabelProvider myLabelProvider;

	ViewerObservable(StructuredViewer viewer) {
		this.viewer = viewer;
		this.contentProvider = new ObservableListContentProvider();
		viewer.setContentProvider(contentProvider);
	}

	/**
	 * @param observableList
	 * @param observableIndexMapping
	 */
	public void init(IObservableList observableList,
			IObservableIndexMapping observableIndexMapping) {
		this.observableList = observableList;
		this.myLabelProvider = new MyLabelProvider();
		this.observableIndexMapping = observableIndexMapping;
		IIndexMappingChangeListener mappingListener = new IIndexMappingChangeListener() {};
		observableIndexMapping.addIndexMappingChangeListener(mappingListener);
		viewer.setLabelProvider(myLabelProvider);
		viewer.setInput(observableList);
	}
}
