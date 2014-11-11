/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 443094
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.eclipse.e4.ui.css.swt.CSSSWTConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

public class MarginTest extends CSSSWTTestCase {

	private static final RGB RED = new RGB(255, 0, 0);

	private final static int TOP = 0;
	private final static int RIGHT = 1;
	private final static int BOTTOM = 2;
	private final static int LEFT = 3;

	protected Control createTestControl(String styleSheet) {
		engine = createEngine(styleSheet, display);

		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		Composite panel = new Composite(shell, SWT.NONE);
		panel.setData(CSSSWTConstants.MARGIN_WRAPPER_KEY, true);

		//Must be grid, see CSSPropertyMarginSWTHandler
		GridLayout layout = new GridLayout();
		layout.marginTop = 0;
		layout.marginRight = 0;
		layout.marginBottom = 0;
		layout.marginLeft = 0;
		panel.setLayout(layout);

		Button buttonToTest = new Button(panel, SWT.CHECK);
		buttonToTest.setText("Some button text");

		// Apply styles
		engine.applyStyles(shell, true);

		return buttonToTest;
	}

	protected Control createBadControlNoLayout(String styleSheet) {
		engine = createEngine(styleSheet, display);

		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		Composite panel = new Composite(shell, SWT.NONE);
		panel.setData(CSSSWTConstants.MARGIN_WRAPPER_KEY);
		//No layout
		Button buttonToTest = new Button(panel, SWT.CHECK);
		buttonToTest.setText("Some button text");
		engine.applyStyles(shell, true);

		return buttonToTest;
	}

	protected Control createBadControlNoComposite(String styleSheet) {
		engine = createEngine(styleSheet, display);

		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		//No composite
		Button buttonToTest = new Button(shell, SWT.CHECK);
		buttonToTest.setText("Some button text");
		engine.applyStyles(shell, true);

		return buttonToTest;
	}

	protected Control createBadControlNoKey(String styleSheet) {
		engine = createEngine(styleSheet, display);

		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		Composite panel = new Composite(shell, SWT.NONE);
		//No key panel.setData(CSSSWTConstants.MARGIN_WRAPPER_KEY);

		// Must be grid, see CSSPropertyMarginSWTHandler
		GridLayout layout = new GridLayout();
		layout.marginTop = 0;
		layout.marginRight = 0;
		layout.marginBottom = 0;
		layout.marginLeft = 0;
		panel.setLayout(layout);

		Button buttonToTest = new Button(panel, SWT.CHECK);
		buttonToTest.setText("Some button text");

		// Apply styles
		engine.applyStyles(shell, true);

		return buttonToTest;
	}

	@Test
	public void testTopMargin() {
		Control control = createTestControl("Button { margin-top: 10}");
		assertEquals(10, getMargin(control, TOP));
		assertEquals(0, getMargin(control, RIGHT));
		assertEquals(0, getMargin(control, BOTTOM));
		assertEquals(0, getMargin(control, LEFT));
	}

	@Test
	public void testRightMargin() {
		Control control = createTestControl("Button { margin-right: 20}");
		assertEquals(0, getMargin(control, TOP));
		assertEquals(20, getMargin(control, RIGHT));
		assertEquals(0, getMargin(control, BOTTOM));
		assertEquals(0, getMargin(control, LEFT));
	}

	@Test
	public void testBottomMargin() {
		Control control = createTestControl("Button { margin-bottom: 30}");
		assertEquals(0, getMargin(control, TOP));
		assertEquals(0, getMargin(control, RIGHT));
		assertEquals(30, getMargin(control, BOTTOM));
		assertEquals(0, getMargin(control, LEFT));
	}

	@Test
	public void testLeftMargin() {
		Control control = createTestControl("Button { margin-left: 40}");
		assertEquals(0, getMargin(control, TOP));
		assertEquals(0, getMargin(control, RIGHT));
		assertEquals(0, getMargin(control, BOTTOM));
		assertEquals(40, getMargin(control, LEFT));
	}

	@Test
	public void testMargin1Value() {
		Control control = createTestControl("Button { margin: 15}");
		assertEquals(15, getMargin(control, TOP));
		assertEquals(15, getMargin(control, RIGHT));
		assertEquals(15, getMargin(control, BOTTOM));
		assertEquals(15, getMargin(control, LEFT));
	}

	@Test
	public void testMargin2Values() {
		Control control = createTestControl("Button { margin: 10 15}");
		assertEquals(10, getMargin(control, TOP));
		assertEquals(15, getMargin(control, RIGHT));
		assertEquals(10, getMargin(control, BOTTOM));
		assertEquals(15, getMargin(control, LEFT));
	}

	@Test
	public void testMargin4Values() {
		Control control = createTestControl("Button { margin: 10 15 20 40}");
		assertEquals(10, getMargin(control, TOP));
		assertEquals(15, getMargin(control, RIGHT));
		assertEquals(20, getMargin(control, BOTTOM));
		assertEquals(40, getMargin(control, LEFT));
	}

	/*
	 * Test handling if there is no layout on the control so can't set margins
	 */
	@Test
	public void testMarginNoLayout() {
		//shouldn't blow up, nothing should happen
		Control control = createBadControlNoLayout("Button { margin: 10 15 20 40; background-color: #FF0000 }");
		//ensure that any styling after 'margin:' gets processed
		assertEquals(RED, control.getBackground().getRGB());
	}

	/*
	 * Test handling if there is no composite on the control so can't set margins
	 */
	@Test
	public void testMarginNoComposite() {
		//shouldn't blow up, nothing should happen
		Control control = createBadControlNoComposite("Button { margin: 10 15 20 40; background-color: #FF0000 }");
		//ensure that any styling after 'margin:' gets processed
		assertEquals(RED, control.getBackground().getRGB());
	}

	/*
	 * Test handling if there is no key to tell us we can manipulate the composite's layout
	 */
	@Test
	public void testMarginNoKey() {
		//shouldn't blow up, nothing should happen
		Control control = createBadControlNoKey("Button { margin: 10 15 20 40; background-color: #FF0000 }");
		//ensure that any styling after 'margin:' gets processed
		assertEquals(RED, control.getBackground().getRGB());
		assertNotSame(10, getMargin(control, TOP));
	}


	private int getMargin(Control control, int side) {
		//Note: relies on implementation details of how we achieve margins on the widgets
		//See CSSPropertyMarginSWTHandler
		GridLayout layout = (GridLayout) control.getParent().getLayout();
		switch (side) {
		case TOP:
			return layout.marginTop;
		case RIGHT:
			return layout.marginRight;
		case BOTTOM:
			return layout.marginBottom;
		case LEFT:
			return layout.marginLeft;
		}
		return -1;
	}

}
