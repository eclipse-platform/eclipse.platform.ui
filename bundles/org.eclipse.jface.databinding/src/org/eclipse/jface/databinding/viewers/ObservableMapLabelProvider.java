/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bugs 164247, 164134
 *     Matthew Hall - bug 302860
 *******************************************************************************/

package org.eclipse.jface.databinding.viewers;

import java.util.Set;

import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;

/**
 * A label provider based on one or more observable maps that track attributes
 * that this label provider uses for display. Clients may customize by
 * subclassing and overriding {@link #getColumnText(Object, int)},
 * {@link #getColumnImage(Object, int)}, for tables or trees with columns, or by
 * implementing additional mixin interfaces for colors, fonts etc.
 * 
 * @since 1.1
 * 
 */
public class ObservableMapLabelProvider extends LabelProvider implements
		ILabelProvider, ITableLabelProvider {

	/**
	 * Observable maps typically mapping from viewer elements to label values.
	 * Subclasses may reference these maps to provide custom labels.
	 * 
	 * @since 1.4
	 */
	protected IObservableMap[] attributeMaps;

	private IMapChangeListener mapChangeListener = new IMapChangeListener() {
		public void handleMapChange(MapChangeEvent event) {
			Set affectedElements = event.diff.getChangedKeys();
			LabelProviderChangedEvent newEvent = new LabelProviderChangedEvent(
					ObservableMapLabelProvider.this, affectedElements.toArray());
			fireLabelProviderChanged(newEvent);
		}
	};

	/**
	 * @param attributeMap
	 */
	public ObservableMapLabelProvider(IObservableMap attributeMap) {
		this(new IObservableMap[] { attributeMap });
	}

	/**
	 * @param attributeMaps
	 */
	public ObservableMapLabelProvider(IObservableMap[] attributeMaps) {
		System.arraycopy(attributeMaps, 0,
				this.attributeMaps = new IObservableMap[attributeMaps.length],
				0, attributeMaps.length);
		for (int i = 0; i < attributeMaps.length; i++) {
			attributeMaps[i].addMapChangeListener(mapChangeListener);
		}
	}

	public void dispose() {
		for (int i = 0; i < attributeMaps.length; i++) {
			attributeMaps[i].removeMapChangeListener(mapChangeListener);
		}
		super.dispose();
		this.attributeMaps = null;
		this.mapChangeListener = null;
	}

	public Image getImage(Object element) {
		return getColumnImage(element, 0);
	}

	public String getText(Object element) {
		return getColumnText(element, 0);
	}

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (columnIndex < attributeMaps.length) {
			Object result = attributeMaps[columnIndex].get(element);
			return result == null ? "" : result.toString(); //$NON-NLS-1$
		}
		return null;
	}

}
