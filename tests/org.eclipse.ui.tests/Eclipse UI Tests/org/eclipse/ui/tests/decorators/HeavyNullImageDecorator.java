/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
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

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.tests.internal.ForcedException;

/**
 * @see ILabelDecorator
 */
public class HeavyNullImageDecorator implements ILabelDecorator {

	/**
	 * Whether we should fail with an exception
	 */
	public static boolean fail = false;

	/**
	 *
	 */
	public HeavyNullImageDecorator() {
	}

	/**
	 * @see ILabelDecorator#addListener
	 */
	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	/**
	 * @see ILabelDecorator#dispose
	 */
	@Override
	public void dispose() {
	}

	/**
	 * @see ILabelDecorator#isLabelProperty
	 */
	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/**
	 * @see ILabelDecorator#removeListener
	 */
	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	/**
	 * @see ILabelDecorator#decorateImage
	 */
	@Override
	public Image decorateImage(Image image, Object element) {
		if (fail) {
			fail = false;
			throw new ForcedException("Heavy image decorator boom");
		}
		return null;
	}

	/**
	 * @see ILabelDecorator#decorateText
	 */
	@Override
	public String decorateText(String text, Object element) {
		return text;
	}
}
