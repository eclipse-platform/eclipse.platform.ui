/*******************************************************************************
 * Copyright (c) 2023 Advantest Europe GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 				Raghunandana Murthappa
 *******************************************************************************/

package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Composite;

public class TableViewerWithLimitCompatibilityTest extends TableViewerTest {

	@Override
	protected StructuredViewer createViewer(Composite parent) {
		ColumnViewer viewer = (ColumnViewer) super.createViewer(parent);
		viewer.setDisplayIncrementally(Integer.MAX_VALUE);
		return viewer;
	}

}
