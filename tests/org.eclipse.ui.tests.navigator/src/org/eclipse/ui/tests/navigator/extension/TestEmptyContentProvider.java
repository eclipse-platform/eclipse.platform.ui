/*******************************************************************************
 * Copyright (c) 2009, 2010 Fair Isaac Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Fair Isaac Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.navigator.extension;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class TestEmptyContentProvider implements ITreeContentProvider {

	private static final Object[] NO_CHILDREN = new Object[0];

	public static boolean _throw;

	public static void resetTest() {
		_throw = false;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (_throw)
			throw new RuntimeException("Throwing...");
		return NO_CHILDREN;
	}

	@Override
	public Object getParent(Object element) {
		if (_throw)
			throw new RuntimeException("Throwing...");
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (_throw)
			throw new RuntimeException("Throwing...");
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (_throw)
			throw new RuntimeException("Throwing...");
		return NO_CHILDREN;
	}

	@Override
	public void dispose() {}
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

}
