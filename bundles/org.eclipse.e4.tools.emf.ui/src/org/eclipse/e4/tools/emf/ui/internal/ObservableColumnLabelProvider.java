/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal;

import java.util.Set;

import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public class ObservableColumnLabelProvider<M> extends CellLabelProvider {
	private IObservableMap<M, ?>[] attributeMaps;

	private IMapChangeListener<M, Object> mapChangeListener = event -> {
		Set<? extends M> affectedElements = event.diff.getChangedKeys();
		LabelProviderChangedEvent newEvent = new LabelProviderChangedEvent(ObservableColumnLabelProvider.this, affectedElements.toArray());
		fireLabelProviderChanged(newEvent);
	};

	/**
	 * Creates a new label provider that tracks changes to one attribute.
	 *
	 * @param attributeMap
	 */
	@SuppressWarnings("unchecked")
	public ObservableColumnLabelProvider(IObservableMap<?, ?> attributeMap) {
		this(new IObservableMap[] { attributeMap });
	}

	/**
	 * Creates a new label provider that tracks changes to more than one
	 * attribute. This constructor should be used by subclasses that override
	 * {@link #update(ViewerCell)} and make use of more than one attribute.
	 *
	 * @param attributeMaps
	 */
	@SuppressWarnings("unchecked")
	protected ObservableColumnLabelProvider(IObservableMap<M, ?>[] attributeMaps) {
		System.arraycopy(attributeMaps, 0, this.attributeMaps = new IObservableMap[attributeMaps.length], 0, attributeMaps.length);
		for (int i = 0; i < attributeMaps.length; i++) {
			attributeMaps[i].addMapChangeListener(mapChangeListener);
		}
	}

	@Override
	public void dispose() {
		for (int i = 0; i < attributeMaps.length; i++) {
			attributeMaps[i].removeMapChangeListener(mapChangeListener);
		}
		super.dispose();
		this.attributeMaps = null;
		this.mapChangeListener = null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void update(ViewerCell cell) {
		M element = (M) cell.getElement();
		cell.setText(getText(element));
		cell.setImage(getImage(element));
		cell.setBackground(getBackground(element));
		cell.setForeground(getForeground(element));
		cell.setFont(getFont(element));
	}

	public String getText(M element) {
		Object value = attributeMaps[0].get(element);
		return value == null ? "" : value.toString(); //$NON-NLS-1$
	}

	public Font getFont(M element) {
		return null;
	}

	public Color getBackground(M element) {
		return null;
	}

	public Color getForeground(M element) {
		return null;
	}

	public Image getImage(M element) {
		return null;
	}

}
