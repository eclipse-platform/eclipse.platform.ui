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

package org.eclipse.jface.internal.databinding.internal.viewers;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.internal.databinding.provisional.observable.mapping.IMultiMapping;
import org.eclipse.jface.viewers.AbstractListViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IViewerLabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.graphics.Image;

/**
 * @since 3.2
 * 
 */
public class AbstractListViewerObservableCollectionWithLabels extends
		StructuredViewerObservableCollectionWithLabels {

	private LabelProvider labelProvider = new LabelProvider();

	private IMultiMapping labelMapping;

	private class LabelProvider implements IViewerLabelProvider, ILabelProvider {

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
			Object mapping = labelMapping.getMappingValues(element,
					new int[] { 0 })[0];
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

		public Image getImage(Object element) {
			Object mapping = labelMapping.getMappingValues(element,
					new int[] { 0 })[0];
			if (mapping instanceof ViewerLabel) {
				ViewerLabel viewerLabel = (ViewerLabel) mapping;
				return viewerLabel.getImage();
			}
			return null;
		}

		public String getText(Object element) {
			Object mapping = labelMapping.getMappingValues(element,
					new int[] { 0 })[0];
			if (mapping instanceof ViewerLabel) {
				ViewerLabel viewerLabel = (ViewerLabel) mapping;
				return viewerLabel.getText();
			} else if (mapping != null) {
				return mapping.toString();
			} else {
				return ""; //$NON-NLS-1$
			}
		}

	}

	/**
	 * @param abstractListViewer
	 */
	public AbstractListViewerObservableCollectionWithLabels(
			AbstractListViewer abstractListViewer) {
		super(abstractListViewer);
	}

	public void init(IMultiMapping labelMapping) {
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

	protected void addToViewer(int index, Object element) {
		// since there is no insert(index, element), we need to refresh
		getViewer().refresh();
	}

}
