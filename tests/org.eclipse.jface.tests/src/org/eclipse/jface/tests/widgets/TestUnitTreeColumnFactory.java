/*******************************************************************************
* Copyright (c) 2019 SAP SE and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https:www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     SAP SE - initial version
******************************************************************************/
package org.eclipse.jface.tests.widgets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.widgets.TreeColumnFactory;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.junit.Before;
import org.junit.Test;

public class TestUnitTreeColumnFactory extends AbstractFactoryTest {

	private Tree tree;

	@Override
	@Before
	public void setup() {
		super.setup();
		tree = WidgetFactory.tree(SWT.NONE).create(shell);
	}

	@Test
	public void createsTreeColumn() {
		TreeColumn treeColumn = TreeColumnFactory.newTreeColumn(SWT.LEFT).create(tree);

		assertEquals(tree.getColumn(0), treeColumn);
		assertEquals(treeColumn.getParent(), tree);
		assertEquals(SWT.LEFT, treeColumn.getStyle() & SWT.LEFT);
	}

	@Test
	public void setsSelectionListener() {
		final SelectionEvent[] raisedEvents = new SelectionEvent[1];
		TreeColumn treeColumn = TreeColumnFactory.newTreeColumn(SWT.NONE).onSelect(e -> raisedEvents[0] = e)
				.create(tree);

		treeColumn.notifyListeners(SWT.Selection, new Event());

		assertEquals(1, treeColumn.getListeners(SWT.Selection).length);
		assertNotNull(raisedEvents[0]);
	}

	@Test
	public void setsAlignment() {
		TreeColumn treeColumn = TreeColumnFactory.newTreeColumn(SWT.NONE).align(SWT.LEFT).create(tree);

		assertEquals(SWT.LEFT, treeColumn.getAlignment());
	}

	@Test
	public void setsToolTip() {
		TreeColumn treeColumn = TreeColumnFactory.newTreeColumn(SWT.NONE).tooltip("tooltip").create(tree);

		assertEquals("tooltip", treeColumn.getToolTipText());
	}

	@Test
	public void setsWidth() {
		TreeColumn treeColumn = TreeColumnFactory.newTreeColumn(SWT.NONE).width(20).create(tree);

		assertEquals(20, treeColumn.getWidth());
	}

	@Test
	public void setsMoveable() {
		TreeColumn treeColumn = TreeColumnFactory.newTreeColumn(SWT.NONE).moveable(true).create(tree);

		assertTrue(treeColumn.getMoveable());
	}

	@Test
	public void setsResizable() {
		TreeColumn treeColumn = TreeColumnFactory.newTreeColumn(SWT.NONE).resizable(true).create(tree);

		assertTrue(treeColumn.getResizable());
	}
}