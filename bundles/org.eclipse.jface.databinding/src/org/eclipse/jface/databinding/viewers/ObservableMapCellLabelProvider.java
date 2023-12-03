/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 *     Brad Reynolds - bugs 164247, 164134
 *     Matthew Hall - bug 302860
 *******************************************************************************/

package org.eclipse.jface.databinding.viewers;

import java.util.Set;

import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.ViewerCell;

/**
 * A label provider based on one or more observable maps that track attributes
 * that this label provider uses for display. The default behavior is to display
 * the first attribute's value. Clients may customize by subclassing and
 * overriding {@link #update(ViewerCell)}.
 *
 * @since 1.3
 */
public class ObservableMapCellLabelProvider extends CellLabelProvider {

	/**
	 * Observable maps typically mapping from viewer elements to label values.
	 * Subclasses may use these maps to provide custom labels.
	 *
	 * @since 1.4
	 */
	protected IObservableMap<Object, Object>[] attributeMaps;

	private IMapChangeListener<Object, Object> mapChangeListener = event -> {
		Set<?> affectedElements = event.diff.getChangedKeys();
		LabelProviderChangedEvent newEvent = new LabelProviderChangedEvent(ObservableMapCellLabelProvider.this,
				affectedElements.toArray());
		fireLabelProviderChanged(newEvent);
	};

	/**
	 * Creates a new label provider that tracks changes to one attribute.
	 *
	 * @param attributeMap attribute map to track
	 */
	public ObservableMapCellLabelProvider(IObservableMap<?, ?> attributeMap) {
		this(new IObservableMap[] { attributeMap });
	}

	/**
	 * Creates a new label provider that tracks changes to more than one
	 * attribute. This constructor should be used by subclasses that override
	 * {@link #update(ViewerCell)} and make use of more than one attribute.
	 *
	 * @param attributeMaps attribute maps to track
	 */
	@SuppressWarnings("unchecked")
	protected ObservableMapCellLabelProvider(IObservableMap<?, ?>[] attributeMaps) {
		System.arraycopy(attributeMaps, 0, this.attributeMaps = new IObservableMap[attributeMaps.length], 0,
				attributeMaps.length);
		for (IObservableMap<?, ?> attributeMap : attributeMaps) {
			attributeMap.addMapChangeListener(mapChangeListener);
		}
	}

	@Override
	public void dispose() {
		for (IObservableMap<?, ?> attributeMap : attributeMaps) {
			attributeMap.removeMapChangeListener(mapChangeListener);
		}
		super.dispose();
		this.attributeMaps = null;
		this.mapChangeListener = null;
	}

	/**
	 * Updates the label of the cell with the value for the cell element. Note:
	 * The value for the first map is always used, for all columns.
	 *
	 * @param cell
	 *            The cell to be updated.
	 */
	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getElement();
		// Always use the value from the first map
		Object value = attributeMaps[0].get(element);
		cell.setText(value == null ? "" : value.toString()); //$NON-NLS-1$
	}
}
