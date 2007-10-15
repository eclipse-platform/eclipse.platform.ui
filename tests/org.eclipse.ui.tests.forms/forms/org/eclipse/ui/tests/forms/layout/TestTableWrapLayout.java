/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.forms.layout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import junit.framework.TestCase;

public class TestTableWrapLayout extends TestCase {

	private final String A1 = "A";
	private final String A10 = "A A A A A A A A A A";
	private final String A20 = A10 + " " + A10;
	private final String A40 = A20 + " " + A20;
	private final String A80 = A40 + " " + A40;
	
	// Returns the width + left
	private int rightEdge(Label lab) {
		Rectangle r = lab.getBounds();
		return r.x + r.width;
	}
	
	/**
	 * Test that labels with the WRAP property set do indeed wrap.
	 */
	public void testTableWrapLayoutNonWrappingLabels() {
		Display display = PlatformUI.getWorkbench().getDisplay();
		Shell shell = new Shell(display);
		shell.setSize(100, 300);
		shell.setLayout(new FillLayout());
		Composite inner = new Composite(shell, SWT.V_SCROLL);
		inner.setLayout(new TableWrapLayout());
		Label l1 = new Label(inner, SWT.NULL);
		l1.setText(A10);
		Label l2 = new Label(inner, SWT.NULL);
		l2.setText(A80);
		shell.layout();
		assertEquals(l1.getSize().y, l2.getSize().y);
		assertTrue(l2.getSize().x > 100);
		shell.dispose();
	}
	
	/**
	 * Test that labels with the WRAP property set do indeed wrap.
	 */
    // Test suppressed for now - does not pass but not sure if this is a bug
	public void suppressed_testWrappingPoint() {
		Display display = PlatformUI.getWorkbench().getDisplay();
		Shell shell = new Shell(display);
		shell.setSize(300, 300);
		shell.setLayout(new FillLayout());
		Composite inner = new Composite(shell, SWT.V_SCROLL);
		TableWrapLayout tableWrapLayout = new TableWrapLayout();
		tableWrapLayout.leftMargin = 0;
		tableWrapLayout.rightMargin = 0;
		inner.setLayout(tableWrapLayout);
		Label l1 = new Label(inner, SWT.WRAP);
		l1.setText(A10);
		shell.layout();
		int originalWidth = l1.getSize().x;
		int originalHeight = l1.getSize().y;
		shell.setSize(originalWidth, 300);
		shell.layout();
		assertEquals(l1.getSize().y, originalHeight);
		shell.setSize(originalWidth / 2, 300);
		shell.layout();
		inner.layout();
		assertTrue(l1.getSize().y > originalHeight);
		shell.dispose();
	}
	
	/**
	 * Test that labels with the WRAP property set do indeed wrap.
	 */
	// Test suppressed for now, see Bug 196686 
	public void suppressed_testTableWrapLayoutWrappingLabels() {
		Display display = PlatformUI.getWorkbench().getDisplay();
		Shell shell = new Shell(display);
		shell.setSize(100, 300);
		shell.setLayout(new FillLayout());
		Composite inner = new Composite(shell, SWT.V_SCROLL);
		inner.setLayout(new TableWrapLayout());
		Label l1 = new Label(inner, SWT.WRAP);
		l1.setText(A10);
		Label l2 = new Label(inner, SWT.WRAP);
		l2.setText(A80);
		shell.layout();
		assertTrue(l1.getSize().y < l2.getSize().y);
		assertTrue("Label is too wide for layout ", l1.getSize().x <= 100);
		assertTrue("Label is too wide for layout ", l2.getSize().x <= 100);
		assertTrue("Labels overlap", l2.getBounds().y >= l1.getBounds().y + l1.getBounds().height);
		shell.dispose();
	}
	
	/**
	 * Test a 2x2 grid with unequal sizes
	 */
	public void testTableWrapLayoutTwoColumnsWrappingLabels() {
		Display display = PlatformUI.getWorkbench().getDisplay();
		Shell shell = new Shell(display);
		shell.setSize(100, 300);
		shell.setLayout(new FillLayout());
		Composite inner = new Composite(shell, SWT.V_SCROLL);
		TableWrapLayout tableWrapLayout = new TableWrapLayout();
		tableWrapLayout.numColumns = 2;
		inner.setLayout(tableWrapLayout);
		Label l1 = new Label(inner, SWT.WRAP);
		l1.setText(A10);
		Label l2 = new Label(inner, SWT.WRAP);
		l2.setText(A20);
		Label l3 = new Label(inner, SWT.WRAP);
		l3.setText(A40);
		Label l4 = new Label(inner, SWT.WRAP);
		l4.setText(A80);
		shell.layout();
		assertTrue(l1.getSize().x < l2.getSize().x);
		assertTrue(l1.getSize().y < l3.getSize().y);
		assertTrue(l1.getSize().x < l4.getSize().x);
		assertTrue(l2.getSize().y < l3.getSize().y);
		assertTrue("Label is too wide for layout ", l1.getSize().x + l2.getSize().x <= 100);
		assertTrue("Labels overlap", l2.getBounds().x >= l1.getBounds().x + l1.getBounds().width);
		assertTrue("Labels overlap", l3.getBounds().y >= l1.getBounds().y + l1.getBounds().height);
		assertTrue("Labels overlap", l4.getBounds().x >= l3.getBounds().x + l3.getBounds().width);
		assertTrue("Labels overlap", l4.getBounds().y >= l2.getBounds().y + l2.getBounds().height);
		shell.dispose();
	}
	
	/**
	 * Test alignments and margins
	 */
	// Suppressed for now - see Bug 196686 
	public void suppressed_testTableWrapLayoutAlignment() {
		Display display = PlatformUI.getWorkbench().getDisplay();
		Shell shell = new Shell(display);
		shell.setSize(100, 300);
		shell.setLayout(new FillLayout());
		Composite inner = new Composite(shell, SWT.V_SCROLL);
		TableWrapLayout tableWrapLayout = new TableWrapLayout();
		final int LEFT_MARGIN = 1;
		final int RIGHT_MARGIN = 2;
		final int TOP_MARGIN = 3;
		final int BOTTOM_MARGIN = 4;
		tableWrapLayout.leftMargin = LEFT_MARGIN;
		tableWrapLayout.rightMargin = RIGHT_MARGIN;
		tableWrapLayout.topMargin = TOP_MARGIN;
		tableWrapLayout.bottomMargin = BOTTOM_MARGIN;
		inner.setLayout(tableWrapLayout);
		Label lab0 = new Label(inner, SWT.WRAP);
		lab0.setText(A80);
		Label labLeft = new Label(inner, SWT.NULL);
		labLeft.setText(A1);
		TableWrapData dataLeft = new TableWrapData();
		dataLeft.align = TableWrapData.LEFT;
		labLeft.setLayoutData(dataLeft);
		Label labRight = new Label(inner, SWT.NULL);
		labRight.setText(A1);
		TableWrapData dataRight = new TableWrapData();
		dataRight.align = TableWrapData.RIGHT;
		labRight.setLayoutData(dataRight);
		Label labCenter = new Label(inner, SWT.NULL);
		labCenter.setText(A1);
		TableWrapData dataCenter = new TableWrapData();
		dataCenter.align = TableWrapData.CENTER;
		labCenter.setLayoutData(dataCenter);
		Label labFill = new Label(inner, SWT.NULL);
		labFill.setText(A1);
		TableWrapData dataFill = new TableWrapData();
		dataFill.align = TableWrapData.FILL;
		labFill.setLayoutData(dataFill);
		shell.layout();
		// Check layout
		assertEquals(LEFT_MARGIN , labLeft.getBounds().x);
		assertTrue(rightEdge(lab0) > rightEdge(labLeft));
		assertTrue(rightEdge(labLeft) + tableWrapLayout.rightMargin < 100);
		
		assertEquals(rightEdge(labRight), rightEdge(lab0));
		assertTrue(labRight.getBounds().x > LEFT_MARGIN);
		
		assertTrue(labCenter.getBounds().x > LEFT_MARGIN);
		assertTrue(rightEdge(lab0) > rightEdge(labCenter));
		
		int offCenter = rightEdge(labCenter) + labCenter.getBounds().x 
		   - rightEdge(lab0) + lab0.getBounds().x;
		assertTrue(offCenter >= -2);
		assertTrue(offCenter <= 2);
		
		assertEquals(LEFT_MARGIN , labFill.getBounds().x);
		assertEquals(rightEdge(labFill), rightEdge(lab0));
		shell.dispose();
	}

}
