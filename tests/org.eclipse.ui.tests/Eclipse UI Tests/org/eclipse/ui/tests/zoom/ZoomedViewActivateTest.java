/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
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
package org.eclipse.ui.tests.zoom;

import org.eclipse.ui.IWorkbenchPart;

/**
 * @since 3.1
 */
public class ZoomedViewActivateTest extends ActivateTest {

	/**
	 * @param name
	 */
	public ZoomedViewActivateTest(String name) {
		super(name);
	}

	@Override
	public IWorkbenchPart getStackedPart1() {
		return stackedView1;
	}

	@Override
	public IWorkbenchPart getStackedPart2() {
		return stackedView2;
	}

	@Override
	public IWorkbenchPart getUnstackedPart() {
		return unstackedView;
	}

	/**
	 * <p>Test: Zoom a view then activate an editor</p>
	 * <p>Expected result: page unzooms</p>
	 */
	public void testActivateEditor() {
		// We allow an editor to be activated *without* unzooming
		System.out.println("Bogus Test: " + getName());

//        zoom(stackedView1);
//        page.activate(editor1);
//
//        assertZoomed(null);
//        assertActive(editor1);
	}
}
