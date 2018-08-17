/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import javax.inject.Inject;
import org.eclipse.swt.widgets.Composite;

public class LayoutView {

	public static String CONTRIBUTION_URI = "bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.LayoutView";

	@Inject
	public LayoutView(Composite parent) {
		parent.getShell().layout(true, true);
	}
}
