/*******************************************************************************
 * Copyright (c) 2011,2017 IBM Corporation and others.
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
 *     Ralf M Petter<ralf.petter@gmail.com> - Bug 510241
 *******************************************************************************/

package org.eclipse.ui.tests.forms.layout;

import static org.junit.Assert.assertEquals;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.internal.forms.widgets.ColumnLayoutUtils;
import org.eclipse.ui.tests.forms.layout.ControlFactory.TestLayout;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestColumnWrapLayout {

	private final Point p20 = new Point(100, 20);
	private final Point p30 = new Point(100, 30);
	private final Point p50 = new Point(100, 50);
	private final Point p100 = new Point(100, 100);
	private final Point p200 = new Point(100, 200);

	private Display display;
	private Shell shell;
	private Composite inner;
	private ColumnLayout layout;

	@Before
	public void setUp() {
		display = PlatformUI.getWorkbench().getDisplay();
		shell = new Shell(display);
		inner = new Composite(shell, SWT.NULL);
		inner.setSize(100, 300);
		layout = new ColumnLayout();
		layout.leftMargin = 0;
		layout.rightMargin = 0;
		layout.topMargin = 0;
		layout.bottomMargin = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		inner.setLayout(layout);
	}

	@After
	public void tearDown() {
		shell.dispose();
	}

	@Test
	public void testEqualSizeColumns() {
		Point[] sizes = { p20, p30, p30, p20, p20, p30 };
		assertEquals(50, ColumnLayoutUtils.computeColumnHeight(3, sizes, 237, 0));
	}

	@Test
	public void testEqualSizeColumnsWithMargins() {
		Point[] sizes = { p20, p30, p30, p20, p20, p30 };
		assertEquals(60, ColumnLayoutUtils.computeColumnHeight(3, sizes, 200, 10));
	}

	@Test
	public void testVariedSizeColumns() {
		Point[] sizes = { p200, p200, p30 };
		assertEquals(230, ColumnLayoutUtils.computeColumnHeight(2, sizes, 100, 0));
	}

	@Test
	public void testLastEntryLargest() {
		Point[] sizes = { p50, p30, p30, p30, p50, p50, p100 };
		assertEquals(100, ColumnLayoutUtils.computeColumnHeight(4, sizes, 100, 0));
	}

	@Test
	public void testLargeMargins() {
		Point[] sizes = { p20, p20, p20, p20, p20, p50, p50};
		assertEquals(260, ColumnLayoutUtils.computeColumnHeight(3, sizes, 100, 100));
	}

	/**
	 * Test that labels with the WRAP property set do indeed wrap.
	 */
	@Test
	public void testColumnLayoutInShell() {
		layout.verticalSpacing = 5;
		layout.horizontalSpacing = 5;
		layout.minNumColumns = 2;
		layout.maxNumColumns = 2;
		layout.topMargin=2;
		layout.bottomMargin=3;
		layout.leftMargin = 5;
		layout.rightMargin = 5;
		ControlFactory.create(inner, 20, 20, 30);
		ControlFactory.create(inner, 20, 20, 40);
		ControlFactory.create(inner, 20, 20, 20);
		Point size = inner.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		assertEquals(70, size.y);
		inner.setSize(size);
		inner.layout(true);
		assertEquals(new Rectangle(5, 2, 20, 30), inner.getChildren()[0].getBounds());
		assertEquals(new Rectangle(30, 2, 20, 40), inner.getChildren()[1].getBounds());
	}

	@Test
	public void testHorizontalSpacingHasNoEffectWhenOnlyOneColumn() {
		layout.horizontalSpacing = 1000;
		Composite control = ControlFactory.create(inner, 20, 20, 30);
		Point size = inner.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		assertEquals(20, size.x);
		inner.pack(true);
		assertEquals(new Rectangle(0, 0, 20, 30), control.getBounds());
	}

	@Test
	public void testHorizontalSpacing() {
		layout.horizontalSpacing = 1000;
		ControlFactory.create(inner, 20, 20, 30);
		Composite secondControl = ControlFactory.create(inner, 20, 20, 30);
		Point size = inner.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		assertEquals(1040, size.x);
		inner.pack(true);
		assertEquals(new Rectangle(1020, 0, 20, 30), secondControl.getBounds());
	}

	@Test
	public void testHorizontalMargins() {
		layout.leftMargin = 100;
		layout.rightMargin = 10;
		Control leftControl = ControlFactory.create(inner, 20, 20, 30);
		Control rightControl = ControlFactory.create(inner, 20, 20, 40);
		Point size = inner.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		assertEquals(150, size.x);
		inner.pack(true);
		assertEquals(new Rectangle(100, 0, 20, 30), leftControl.getBounds());
		assertEquals(new Rectangle(120, 0, 20, 40), rightControl.getBounds());
		assertEquals(new Rectangle(0, 0, 150, 40), inner.getBounds());
	}

	@Test
	public void testVerticalSpacingHasNoEffectWhenOnlyOneColumn() {
		layout.verticalSpacing = 1000;
		Composite control = ControlFactory.create(inner, 20, 20, 30);
		Point size = inner.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		assertEquals(20, size.x);
		inner.pack(true);
		assertEquals(new Rectangle(0, 0, 20, 30), control.getBounds());
	}

	@Test
	public void testVerticalSpacing() {
		layout.verticalSpacing = 1000;
		layout.maxNumColumns = 1;
		ControlFactory.create(inner, 20, 20, 30);
		Composite secondControl = ControlFactory.create(inner, 20, 20, 30);
		Point size = inner.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		assertEquals(1060, size.y);
		inner.pack(true);
		assertEquals(new Rectangle(0, 1030, 20, 30), secondControl.getBounds());
	}

	@Test
	public void testVerticalMargins() {
		layout.topMargin = 100;
		layout.bottomMargin = 10;
		layout.maxNumColumns = 1;
		Control control1 = ControlFactory.create(inner, 20, 20, 30);
		Control control2 = ControlFactory.create(inner, 20, 20, 40);
		Point size = inner.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		assertEquals(180, size.y);
		inner.pack();
		assertEquals(new Rectangle(0, 100, 20, 30), control1.getBounds());
		assertEquals(new Rectangle(0, 130, 20, 40), control2.getBounds());
		assertEquals(new Rectangle(0, 0, 20, 180), inner.getBounds());
	}

	@Test
	public void testSelectsCorrectNumberOfColumns() {
		layout.horizontalSpacing = 10;
		layout.leftMargin = 10;
		layout.rightMargin = 10;

		ControlFactory.create(inner, 21, 30, 50);
		ControlFactory.create(inner, 22, 40, 50);
		// This last control will have a preferred height of 108 when compressed to its
		// minimum width
		ControlFactory.create(inner, 23, 50, 50);

		// Should always use the maximum number of columns when the width is
		// unconstrained, and the preferred size
		// should be based on the maximum size of the children
		Point size = inner.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		assertEquals(190, size.x);
		assertEquals(50, size.y);

		// Should still use the maximum number of columns if the minimum size of the
		// children will fit in the area
		size = inner.computeSize(109, SWT.DEFAULT);
		assertEquals(109, size.x);
		assertEquals(108, size.y);

		// Lay out with the default width
		inner.pack(true);
		assertAllChildrenHaveWidth(50);

		for (Control next : inner.getChildren()) {
			assertEquals(0, next.getBounds().y);
		}

		// If we're one pixel less than the minimum size for the children, the number of
		// columns should be reduced
		size = inner.computeSize(108, SWT.DEFAULT);
		assertEquals(108, size.x);
		assertEquals(89, size.y);

		// Ensure that it falls back to the minimum number of columns when we're
		// requesting the minimum width
		int minWidth = layout.computeMinimumWidth(inner, false);
		assertEquals(43, minWidth);
	}

	@Test
	public void testFillAlignment() {
		layout.maxNumColumns = 1;

		// Control1 has a min size of 100, a default size of 800, and a width hint of
		// 400. When computing the default size of the column, the width hint should be
		// used rather than the control's preferred width and the control should end up
		// with a size 400x100.
		Composite control1 = ControlFactory.create(inner, 100, 800, 200);
		ColumnLayoutData data1 = new ColumnLayoutData();
		data1.widthHint = 400;
		control1.setLayoutData(data1);

		// Control2 is narrower than control1, but since it's fill-aligned it should be
		// stretched to match the size of control1.
		Composite control2 = ControlFactory.create(inner, 50, 100, 1200);
		ColumnLayoutData data2 = new ColumnLayoutData();
		data2.widthHint = 200;
		control2.setLayoutData(data2);

		Point size = inner.computeSize(SWT.DEFAULT, SWT.DEFAULT);

		// Both control should be resized to match the first control's width hint since
		// it's the widest and both are fill-aligned.
		assertEquals(400, size.x);

		// 400 pixels for the first control, 300 pixels for the second.
		assertEquals(700, size.y);

		inner.pack();

		assertEquals(new Rectangle(0, 0, 400, 400), control1.getBounds());
		assertEquals(new Rectangle(0, 400, 400, 300), control2.getBounds());
	}

	@Test
	public void testLeftAlignment() {
		layout.maxNumColumns = 1;

		// Create a large control to dominate the width of the columns
		Composite control1 = ControlFactory.create(inner, 200, 200, 200);

		// Control2 is narrower than control1, but since it's fill-aligned it
		// should be
		// stretched to match the size of control1.
		Composite control2 = ControlFactory.create(inner, 10, 100, 100);
		ColumnLayoutData data2 = new ColumnLayoutData();
		data2.horizontalAlignment = ColumnLayoutData.LEFT;
		data2.widthHint = 50;
		control2.setLayoutData(data2);

		Composite control3 = ControlFactory.create(inner, 10, 50, 100);
		ColumnLayoutData data3 = new ColumnLayoutData();
		data3.widthHint = 10;
		data3.horizontalAlignment = ColumnLayoutData.LEFT;
		control3.setLayoutData(data3);

		Point size = inner.computeSize(SWT.DEFAULT, SWT.DEFAULT);

		assertEquals(200, size.x);
		assertEquals(900, size.y);

		inner.pack(true);

		assertEquals(new Rectangle(0, 0, 200, 200), control1.getBounds());
		assertEquals(new Rectangle(0, 200, 50, 200), control2.getBounds());
		assertEquals(new Rectangle(0, 400, 10, 500), control3.getBounds());

		// Verify that if we shrink a left-aligned wrapping control to a size
		// smaller than its preferred width, it will still wrap correctly.

		size = inner.computeSize(25, SWT.DEFAULT);

		assertEquals(25, size.x);
		assertEquals(2500, size.y);
	}

	@Test
	public void testControlsFlushedCorrectly()
	{
		Composite composite = ControlFactory.create(inner, 200, 200, 200);
		TestLayout layout = (TestLayout) composite.getLayout();

		// Currently there is one unnecessary recursive flush on startup. Ignore it for
		// now.
		inner.computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
		layout.wasChanged = false;

		// Verify that there is no redundant cache flush.
		inner.computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
		assertEquals(false, layout.wasChanged);

		inner.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		assertEquals(true, layout.wasChanged);

		layout.wasChanged = false;
		inner.layout(false);
		assertEquals(false, layout.wasChanged);

		inner.layout(true);
		assertEquals(true, layout.wasChanged);
	}

	private void assertAllChildrenHaveWidth(int desiredWidth) {
		Control[] children = inner.getChildren();

		for (int idx = 0; idx < children.length; idx++) {
			Control next = children[idx];

			Rectangle bounds = next.getBounds();
			assertEquals("Child " + idx + " should have the correct width", desiredWidth, bounds.width);
		}
	}
}
