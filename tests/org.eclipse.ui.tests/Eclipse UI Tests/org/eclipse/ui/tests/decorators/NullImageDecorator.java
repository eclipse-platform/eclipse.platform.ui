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

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.ui.tests.internal.ForcedException;

/**
 * @see ILabelDecorator
 */
public class NullImageDecorator implements ILightweightLabelDecorator {
	/**
	 * Whether we should fail with an exception
	 */
	public static boolean fail = false;

	/**
	 *
	 */
	public NullImageDecorator() {
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		// XXX Auto-generated method stub
	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		if (fail) {
			fail = false;
			throw new ForcedException("Lighweight decorator boom");
		}
	}

	@Override
	public void dispose() {
		// XXX Auto-generated method stub
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// XXX Auto-generated method stub
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// XXX Auto-generated method stub
	}
}
