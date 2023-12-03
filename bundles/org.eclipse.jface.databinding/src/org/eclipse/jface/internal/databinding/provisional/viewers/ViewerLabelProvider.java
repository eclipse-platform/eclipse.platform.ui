/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
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
 *     Matthew Hall - bug 223123
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 413611
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.provisional.viewers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IViewerLabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.graphics.Image;

/**
 * NON-API - Generic viewer label provider.
 * @since 1.1
 */
public class ViewerLabelProvider implements IViewerLabelProvider,
		ILabelProvider {

	private List<ILabelProviderListener> listeners = new ArrayList<>();

	/**
	 * Subclasses should override this method. They should not call the base
	 * class implementation.
	 */
	@Override
	public void updateLabel(ViewerLabel label, Object element) {
		label.setText(element.toString());
	}

	protected final void fireChangeEvent(Collection<?> changes) {
		final LabelProviderChangedEvent event = new LabelProviderChangedEvent(
				this, changes.toArray());
		ILabelProviderListener[] listenerArray = listeners
				.toArray(new ILabelProviderListener[listeners.size()]);
		for (ILabelProviderListener listener : listenerArray) {
			try {
				listener.labelProviderChanged(event);
			} catch (Exception e) {
				Policy.getLog().log(
						new Status(IStatus.ERROR, Policy.JFACE_DATABINDING, e
								.getLocalizedMessage(), e));
			}
		}
	}

	@Override
	public final Image getImage(Object element) {
		ViewerLabel label = new ViewerLabel("", null); //$NON-NLS-1$
		updateLabel(label, element);
		return label.getImage();
	}

	@Override
	public final String getText(Object element) {
		ViewerLabel label = new ViewerLabel("", null); //$NON-NLS-1$
		updateLabel(label, element);
		return label.getText();
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		listeners.add(listener);
	}

	@Override
	public void dispose() {
		listeners.clear();
	}

	@Override
	public final boolean isLabelProperty(Object element, String property) {
		return true;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		listeners.remove(listener);
	}

}
