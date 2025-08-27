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

	@Override
	public IWorkbenchPart getStackedPart1() {
		return stackedView1;
	}

	@Override
	public IWorkbenchPart getStackedPart2() {
		return stackedView2;
	}

}
