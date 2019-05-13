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
package org.eclipse.jface.tests.viewers;

import java.util.Vector;

public class TestModel {
	Vector<ITestModelListener> fListeners = new Vector<>();

	int fNumLevels;

	int fNumChildren;

	public TestModel(int numLevels, int numChildren) {
		fNumLevels = numLevels;
		fNumChildren = numChildren;
	}

	public void addListener(ITestModelListener listener) {
		fListeners.addElement(listener);
	}

	/**
	 * Fires a model changed event to all listeners.
	 */
	public void fireModelChanged(TestModelChange change) {
		for (int i = 0; i < fListeners.size(); ++i) {
			ITestModelListener listener = fListeners
					.get(i);
			listener.testModelChanged(change);
		}
	}

	public int getNumChildren() {
		return fNumChildren;
	}

	public int getNumLevels() {
		return fNumLevels;
	}

	public void removeListener(ITestModelListener listener) {
		fListeners.removeElement(listener);
	}
}
