/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.api;

import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * This mock is used to test IViewActionDelegate lifecycle.
 */
public class MockViewActionDelegate extends MockActionDelegate implements
		IViewActionDelegate {
	/**
	 * Constructor for MockWorkbenchWindowActionDelegate
	 */
	public MockViewActionDelegate() {
		super();
	}

	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	@Override
	public void init(IViewPart view) {
		callHistory.add("init");
	}
}

