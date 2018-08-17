/*******************************************************************************
 * Copyright (c) 2007, 2017 IBM Corporation and others.
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
import static org.junit.Assert.assertTrue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestTableWrapLayout {

	private Display display;
	private Shell shell;
	private Composite inner;
	private TableWrapLayout layout;

	// Returns the width + left
	private int rightEdge(Control lab) {
		Rectangle r = lab.getBounds();
		return r.x + r.width;
	}

	@Before
	public void setUp() {
		display = PlatformUI.getWorkbench().getDisplay();
		shell = new Shell(display);
		shell.setLayout(new FillLayout());
		inner = new Composite(shell, SWT.NONE);
		inner.setSize(100, 300);
		layout = new TableWrapLayout();
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

	/**
	 * Test a simple two-cell layout.
	 */
	@Test
	public void testSimpleTwoCellLayout() {
		Control l1 = ControlFactory.create(inner, 10, 100, 80);
		Control l2 = ControlFactory.create(inner, 80, 800, 15);

		Point preferredSize = inner.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		int minimumWidth = layout.computeMinimumWidth(inner, false);
		Point wrappedSize = inner.computeSize(400, SWT.DEFAULT);

		inner.pack();
		assertEquals(new Rectangle(0, 0, 800, 10), l1.getBounds());
		assertEquals(new Rectangle(0, 10, 800, 15), l2.getBounds());
		assertEquals(new Point(800, 25), preferredSize);
		assertEquals(80, minimumWidth);
		assertEquals(new Point(400, 50), wrappedSize);
	}

	/**
	 * Test that labels with the WRAP property set do indeed wrap.
	 */
	@Test
	public void testWrappingPoint() {
		Control l1 = ControlFactory.create(inner, 30, 100, 15);

		// Validate the behavior of computeSize()
		Point preferredSize = inner.computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
		int preferredHeightWheneThereIsExtraHorizontalSpace = inner.computeSize(200, SWT.DEFAULT).y;
		int preferredHeightWhenControlFillsSpace = inner.computeSize(100, SWT.DEFAULT).y;
		int preferredHeightWhenControlCompressed = inner.computeSize(50, SWT.DEFAULT).y;
		assertEquals(15, preferredHeightWheneThereIsExtraHorizontalSpace);
		assertEquals(15, preferredHeightWhenControlFillsSpace);
		assertEquals(30, preferredHeightWhenControlCompressed);
		assertEquals(new Point(100, 15), preferredSize);

		// Validate the behavior of layout()
		inner.setSize(100, 15);
		inner.layout();
		assertEquals(15, l1.getSize().y);

		inner.setSize(100, 300);
		inner.layout();
		assertEquals(15, l1.getSize().y);

		inner.setSize(50, 300);
		inner.layout();
		assertEquals(30, l1.getSize().y);

		// Validate the behavior of computeMinimumWidth
		assertEquals(30, layout.computeMinimumWidth(inner, false));
		assertEquals(100, layout.computeMaximumWidth(inner, false));
	}

	/**
	 * Test that wrapping controls do indeed wrap.
	 */
	@Test
	public void testTableWrapLayoutWrappingLabels() {
		Control l1 = ControlFactory.create(inner, 30, 100, 30);
		Control l2 = ControlFactory.create(inner, 50, 800, 15);

		inner.setSize(300, 1000);
		inner.layout(false);

		assertEquals("l1 had the wrong bounds", new Rectangle(0, 0, 300, 10), l1.getBounds());
		assertEquals("l2 had the wrong bounds", new Rectangle(0, 10, 300, 40), l2.getBounds());
	}

	/**
	 * Test a 2x2 grid with unequal sizes
	 */
	@Test
	public void testTableWrapLayoutTwoColumnsWrappingLabels() {
		layout.numColumns = 2;
		Control l1 = ControlFactory.create(inner, 31, 100, 15);
		Control l2 = ControlFactory.create(inner, 32, 200, 15);
		Control l3 = ControlFactory.create(inner, 33, 400, 15);
		Control l4 = ControlFactory.create(inner, 34, 800, 15);

		inner.setSize(300, 1000);
		inner.layout(false);

		assertEquals(300, l3.getBounds().width + l4.getBounds().width);
		assertTrue(rightEdge(l1) <= l2.getBounds().x);
		assertEquals(rightEdge(l3), l4.getBounds().x);
		assertTrue(bottomEdge(l1) <= l3.getBounds().y);
		assertTrue(bottomEdge(l1) <= l4.getBounds().y);

		Point preferredSize = inner.computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
		assertEquals(new Point(1200, 18), preferredSize);

		int minWidth = layout.computeMinimumWidth(inner, false);
		assertEquals(67, minWidth);
	}

	/**
	 * Test what happens when the grid is compressed below its minimum size. It
	 * should remove pixels from the column that creates the least amount of
	 * truncation.
	 * <p>
	 * Test is currently suppressed because this layout cannot handle this case
	 * properly yet.
	 */
	public void suppressed_testCompressedBelowMinimumSize() {
		layout.numColumns = 2;
		Control l1 = ControlFactory.create(inner, 50, 200, 50);
		l1.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL));
		Control l2 = ControlFactory.create(inner, 200, 200, 50);
		l2.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.FILL));
		Control l3 = ControlFactory.create(inner, 400, 400, 50);
		TableWrapData data = new TableWrapData(TableWrapData.FILL, TableWrapData.FILL);
		data.colspan = 2;
		l3.setLayoutData(data);

		inner.setSize(300, 1000);
		inner.layout(false);

		assertEquals(new Rectangle(0, 0, 100, 50), l1.getBounds());
		assertEquals(new Rectangle(100, 0, 200, 50), l1.getBounds());
		assertEquals(new Rectangle(0, 50, 300, 50), l1.getBounds());
	}

	/**
	 * Runs a horizontal alignment test for the given control. Returns true iff
	 * the control was fill-aligned.
	 */
	private boolean runAlignmentTest(Control control, int alignment) {
		TableWrapData dataLeft = new TableWrapData();
		dataLeft.align = alignment;
		dataLeft.grabHorizontal = true;
		control.setLayoutData(dataLeft);

		inner.setSize(1000, 1000);
		inner.layout(false);
		return control.getSize().x == 1000;
	}

	@Test
	public void testLeftAlignmentIsIgnoredForWrappingControls() {
		Label label = new Label(inner, SWT.WRAP);
		label.setText("test");

		assertEquals(true, runAlignmentTest(label, TableWrapData.LEFT));
	}

	@Test
	public void testLeftAlignmentIsRespectedForNonWrappingControls() {
		Label label = new Label(inner, SWT.NONE);
		label.setText("test");

		assertEquals(false, runAlignmentTest(label, TableWrapData.LEFT));
	}

	@Test
	public void testLeftAlignmentIsIgnoredForLayoutsImplementingLayoutExtension() {
		Control label = ControlFactory.create(inner, 10, 200, 100);

		assertEquals(true, runAlignmentTest(label, TableWrapData.LEFT));
	}

	@Test
	public void testRightAlignmentIsIgnoredForWrappingControls() {
		Label label = new Label(inner, SWT.WRAP);
		label.setText("test");

		assertEquals(true, runAlignmentTest(label, TableWrapData.RIGHT));
	}

	@Test
	public void testRightAlignmentIsRespectedForNonWrappingControls() {
		Label label = new Label(inner, SWT.NONE);
		label.setText("test");

		assertEquals(false, runAlignmentTest(label, TableWrapData.RIGHT));
	}

	@Test
	public void testRightAlignmentIsIgnoredForLayoutsImplementingLayoutExtension() {
		Control label = ControlFactory.create(inner, 10, 200, 100);

		assertEquals(true, runAlignmentTest(label, TableWrapData.RIGHT));
	}

	@Test
	public void testCenterAlignmentIsIgnoredForWrappingControls() {
		Label label = new Label(inner, SWT.WRAP);
		label.setText("test");

		assertEquals(true, runAlignmentTest(label, TableWrapData.CENTER));
	}

	@Test
	public void testCenterAlignmentIsRespectedForNonWrappingControls() {
		Label label = new Label(inner, SWT.NONE);
		label.setText("test");

		assertEquals(false, runAlignmentTest(label, TableWrapData.CENTER));
	}

	@Test
	public void testCenterAlignmentIsIgnoredForLayoutsImplementingLayoutExtension() {
		Control label = ControlFactory.create(inner, 10, 200, 100);

		assertEquals(true, runAlignmentTest(label, TableWrapData.CENTER));
	}

	/**
	 * Test alignments and margins
	 */
	@Test
	public void testTableWrapLayoutAlignment() {
		final int LEFT_MARGIN = 1;
		final int RIGHT_MARGIN = 2;
		final int TOP_MARGIN = 3;
		final int BOTTOM_MARGIN = 4;
		layout.leftMargin = LEFT_MARGIN;
		layout.rightMargin = RIGHT_MARGIN;
		layout.topMargin = TOP_MARGIN;
		layout.bottomMargin = BOTTOM_MARGIN;
		Control lab0 = ControlFactory.create(inner, 50, 800, 15);

		Control labLeft = ControlFactory.create(inner, 50, 100, 15);
		TableWrapData dataLeft = new TableWrapData();
		dataLeft.align = TableWrapData.LEFT;
		labLeft.setLayoutData(dataLeft);

		Control labRight = ControlFactory.create(inner, 100, 15);
		TableWrapData dataRight = new TableWrapData();
		dataRight.align = TableWrapData.RIGHT;
		labRight.setLayoutData(dataRight);

		Control labCenter = ControlFactory.create(inner, 50, 100, 15);
		TableWrapData dataCenter = new TableWrapData();
		dataCenter.align = TableWrapData.CENTER;
		labCenter.setLayoutData(dataCenter);

		Control labFill = ControlFactory.create(inner, 50, 100, 15);
		TableWrapData dataFill = new TableWrapData();
		dataFill.align = TableWrapData.FILL;
		labFill.setLayoutData(dataFill);

		inner.setSize(300 + LEFT_MARGIN + RIGHT_MARGIN, 1000);
		inner.layout(false);

		// Check layout
		assertEquals(new Rectangle(LEFT_MARGIN, TOP_MARGIN, 300, 40), lab0.getBounds());
		assertEquals(new Rectangle(LEFT_MARGIN, bottomEdge(lab0), 300, 5), labLeft.getBounds());
		assertEquals(new Rectangle(rightEdge(lab0) - 100, bottomEdge(labLeft), 100, 15), labRight.getBounds());

		int centerPoint = (leftEdge(labCenter) + rightEdge(labCenter)) / 2;
		assertEquals(150, centerPoint - LEFT_MARGIN);

		assertEquals(new Rectangle(LEFT_MARGIN, bottomEdge(labCenter), 300, 5), labFill.getBounds());
	}

	private int leftEdge(Control control) {
		Rectangle bounds = control.getBounds();

		return bounds.x;
	}

	private int bottomEdge(Control control) {
		Rectangle bounds = control.getBounds();

		return bounds.y + bounds.height;
	}

}
