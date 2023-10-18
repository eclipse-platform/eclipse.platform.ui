/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.junit.Ignore;

/**
 * @since 3.0
 */
public class ComboViewerTest extends StructuredViewerTest {

	@Override
	protected StructuredViewer createViewer(Composite parent) {
		ComboViewer viewer = new ComboViewer(parent);
		viewer.setContentProvider(new TestModelContentProvider());
		return viewer;
	}

	@Override
	protected int getItemCount() {
		TestElement first = fRootElement.getFirstChild();
		Combo list = (Combo) fViewer.testFindItem(first);
		return list.getItemCount();
	}

	@Override
	protected String getItemText(int at) {
		Combo list = (Combo) fViewer.getControl();
		return list.getItem(at);
	}

	@Ignore("TODO: Determine if this test is applicable to ComboViewer")
	@Override
	public void testInsertChild() {

	}
}
