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

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.internal.databinding.api.observable.mapping.IMapping;
import org.eclipse.jface.viewers.AbstractListViewer;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IViewerLabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.ViewerLabel;

/**
 * @since 3.2
 * 
 */
public class AbstractListViewerObservableSetWithLabels extends
		StructuredViewerObservableSetWithLabels {

	private LabelProvider labelProvider = new LabelProvider();

	private IMapping labelMapping;

	private class LabelProvider implements IViewerLabelProvider {

		ListenerList listeners = new ListenerList();

		public void addListener(ILabelProviderListener listener) {
			listeners.add(listener);
		}

		public void dispose() {
			listeners.clear();
		}

		public boolean isLabelProperty(Object element, String property) {
			return true;
		}

		public void removeListener(ILabelProviderListener listener) {
			listeners.remove(listener);
		}

		private void fireLabelsChanged(Object[] elements) {
			LabelProviderChangedEvent event = new LabelProviderChangedEvent(
					this, elements);
			Object[] listenerArray = listeners.getListeners();
			for (int i = 0; i < listenerArray.length; i++) {
				ILabelProviderListener listener = (ILabelProviderListener) listenerArray[i];
				listener.labelProviderChanged(event);
			}
		}

		public void updateLabel(ViewerLabel label, Object element) {
			Object mapping = labelMapping.getMappingValue(element);
			if (mapping instanceof ViewerLabel) {
				ViewerLabel viewerLabel = (ViewerLabel) mapping;
				label.setBackground(viewerLabel.getBackground());
				label.setForeground(viewerLabel.getForeground());
				label.setFont(viewerLabel.getFont());
				label.setImage(viewerLabel.getImage());
				label.setText(viewerLabel.getText());
			} else if (mapping != null) {
				label.setText(mapping.toString());
			}
		}

	}

	/**
	 * @param abstractListViewer
	 */
	public AbstractListViewerObservableSetWithLabels(
			AbstractListViewer abstractListViewer) {
		super(abstractListViewer);
	}

	public void init(IMapping labelMapping) {
		this.labelMapping = labelMapping;
		getViewer().setLabelProvider(labelProvider);
	}

	public void updateElements(Object[] elements) {
		labelProvider.fireLabelsChanged(elements);
	}

	public void dispose() {
		super.dispose();
		labelProvider = null;
		labelMapping = null;
	}

	protected void addToViewer(Object element) {
		((AbstractListViewer) getViewer()).add(element);
	}

	protected void addToViewer(Object[] elements) {
		((AbstractListViewer) getViewer()).add(elements);
	}

	protected void removeFromViewer(Object element) {
		((AbstractListViewer) getViewer()).remove(element);
	}

	protected void removeFromViewer(Object[] elements) {
		((AbstractListViewer) getViewer()).remove(elements);
	}

}
