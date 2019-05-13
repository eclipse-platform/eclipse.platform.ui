/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;

public class TestDecoratorContributor implements ILabelDecorator {

	public static TestDecoratorContributor contributor;

	private Set<ILabelProviderListener> listeners = new HashSet<>();

	public static String DECORATOR_SUFFIX = "_SUFFIX";

	public TestDecoratorContributor() {
		contributor = this;
	}

	@Override
	public String decorateText(String text, Object element) {
		//Check that the element is adapted to IResource
		Assert.isTrue(element instanceof IResource);
		return text + DECORATOR_SUFFIX;
	}

	@Override
	public Image decorateImage(Image image, Object element) {
		Assert.isTrue(element instanceof IResource);
		return image;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		listeners.add(listener);
	}

	@Override
	public void dispose() {
		contributor = null;
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
			LabelProviderChangedEvent event = new LabelProviderChangedEvent(this, element);
			iterator.next().labelProviderChanged(event);
		}
	}

}
