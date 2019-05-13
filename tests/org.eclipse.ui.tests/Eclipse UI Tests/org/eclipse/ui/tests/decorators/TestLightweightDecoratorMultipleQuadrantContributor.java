/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.tests.decorators;

import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * This is a contributor that uses the multi quadrant
 * support.
 */

public class TestLightweightDecoratorMultipleQuadrantContributor implements
		ILightweightLabelDecorator {

	private Set<ILabelProviderListener> listeners = new HashSet<>();

	private ImageDescriptor descriptor;

	public TestLightweightDecoratorMultipleQuadrantContributor() {
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		listeners.add(listener);
	}

	@Override
	public void dispose() {
		listeners = new HashSet<>();
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Refresh the listeners to update the decorators for
	 * element.
	 */

	public void refreshListeners(Object element) {
		Iterator<ILabelProviderListener> iterator = listeners.iterator();
		while (iterator.hasNext()) {
			LabelProviderChangedEvent event = new LabelProviderChangedEvent(
					this, element);
			iterator.next()
					.labelProviderChanged(event);
		}
	}

	public ImageDescriptor getOverlay(Object element) {
		Assert.isTrue(element instanceof IResource);
		if (descriptor == null) {
			Bundle bundle = FrameworkUtil.getBundle(BadIndexDecorator.class);
			URL entry = bundle.getEntry("icons/binary_co.gif");
			descriptor = ImageDescriptor.createFromURL(entry);
		}
		return descriptor;

	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		decoration.addOverlay(getOverlay(element), IDecoration.BOTTOM_LEFT);
		decoration.addOverlay(getOverlay(element), IDecoration.BOTTOM_RIGHT);
		decoration.addOverlay(getOverlay(element), IDecoration.TOP_LEFT);
		decoration.addOverlay(getOverlay(element), IDecoration.TOP_RIGHT);
		decoration.addOverlay(getOverlay(element), IDecoration.UNDERLAY);
	}

}
