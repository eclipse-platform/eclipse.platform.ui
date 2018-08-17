/*******************************************************************************
 * Copyright (c) 2018 Google, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stefan Xenos and Alena Laskavaia - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.tests.forms.widgets;

import static org.junit.Assert.assertEquals;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.ScrolledFormText;
import org.eclipse.ui.forms.widgets.ScrolledPageBook;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TreeNode;
import org.eclipse.ui.forms.widgets.Twistie;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * SWT controls have a complicated contract on
 * {@link Control#computeSize(int, int)}. Specifically, if a non-SWT.DEFAULT
 * width (or height) hint is passed as an argument, that argument is supposed to
 * be a few pixels off from the constrained width being queried. That means you
 * need to subtract a small adjustment to the hints prior to invoking
 * computeSize. The implementation of computeSize is required to add these
 * adjustments back on when it receives the arguments. The exact adjustment to
 * use is well-defined (see the implementation of {@link #verifyComputeSize} for
 * details).
 * <p>
 * This API contract is poorly documented and may seem a bit crazy, but since
 * every layout depends on it and every control implements it, there's no good
 * way to fix it without API breakage -- so libraries like forms need to
 * implement it correctly.
 * <p>
 * The purpose of this test is to ensure that all the controls in the forms
 * library add the correct adjustments to the hints they receive. One way to
 * verify this is to invoke computeSize with constant hints and assert that the
 * result differs by the hint by exactly the amount expected for the adjustment
 * value.
 */
public class HintAdjustmentTest {
	private static Display display;

	static {
		try {
			display = PlatformUI.getWorkbench().getDisplay();
		} catch (Throwable e) {
			// this is to run without eclipse
			display = new Display();
		}
	}

	private Shell shell;

	@Before
	public void setUp() throws Exception {
		shell = new Shell(display);
	}

	@After
	public void tearDown() throws Exception {
		shell.dispose();
	}

	void verifyComputeSize(Control control) {
		int widthAdjustment;
		int heightAdjustment;
		if (control instanceof Scrollable) {
			// For scrollables, subtract off the trim size
			Scrollable scrollable = (Scrollable) control;
			Rectangle trim = scrollable.computeTrim(0, 0, 0, 0);

			widthAdjustment = trim.width;
			heightAdjustment = trim.height;
		} else {
			// For non-composites, subtract off 2 * the border size
			widthAdjustment = control.getBorderWidth() * 2;
			heightAdjustment = widthAdjustment;
		}

		final int TEST_VALUE = 100;

		Point computedSize = control.computeSize(TEST_VALUE, TEST_VALUE);

		assertEquals("control is not applying the width adjustment correctly", TEST_VALUE + widthAdjustment,
				computedSize.x);
		assertEquals("control is not applying the height adjustment correctly", TEST_VALUE + heightAdjustment,
				computedSize.y);
	}

	@Test
	public void testScrollingHyperlink() {
		Hyperlink link = new Hyperlink(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		link.setText("This is some sample text");
		verifyComputeSize(link);
	}

	@Test
	public void testHyperlink() {
		Hyperlink link = new Hyperlink(shell, SWT.NONE);
		link.setText("This is some sample text");
		verifyComputeSize(link);
	}

	@Test
	public void testScrollingExpandableComposite() {
		ExpandableComposite ec = new ExpandableComposite(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		ec.setText("Foo bar baz zipp");
		verifyComputeSize(ec);
	}

	@Test
	public void testExpandableComposite() {
		ExpandableComposite ec = new ExpandableComposite(shell, SWT.NONE);
		ec.setText("Foo bar baz zipp");
		verifyComputeSize(ec);
	}

	@Test
	public void testScrollingForm() {
		Form form = new Form(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		form.setMessage("Hello world");
		verifyComputeSize(form);
	}

	@Test
	public void testForm() {
		Form form = new Form(shell, SWT.NONE);
		form.setMessage("Hello world");
		verifyComputeSize(form);
	}

	@Test
	public void testScrollingFormText() {
		FormText formText = new FormText(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		formText.setText("This izza test", false, false);
		verifyComputeSize(formText);
	}

	@Test
	public void testFormText() {
		FormText formText = new FormText(shell, SWT.NONE);
		formText.setText("This izza test", false, false);
		verifyComputeSize(formText);
	}

	@Test
	public void testScrollingImageHyperlink() {
		ImageHyperlink hyperlink = new ImageHyperlink(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		hyperlink.setText("Foo, bar, baz");
		verifyComputeSize(hyperlink);
	}

	@Test
	public void testImageHyperlink() {
		ImageHyperlink hyperlink = new ImageHyperlink(shell, SWT.NONE);
		hyperlink.setText("Foo, bar, baz");
		verifyComputeSize(hyperlink);
	}

	@Test
	public void testScrolledForm() {
		ScrolledForm scrolledForm = new ScrolledForm(shell, SWT.NONE);
		scrolledForm.setText("Foo, bar, baz");
		verifyComputeSize(scrolledForm);
	}

	@Test
	public void testScrollingScrolledForm() {
		ScrolledForm scrolledForm = new ScrolledForm(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledForm.setText("Foo, bar, baz");
		verifyComputeSize(scrolledForm);
	}

	@Test
	public void testScrolledFormText() {
		ScrolledFormText scrolledForm = new ScrolledFormText(shell, SWT.NONE, true);
		scrolledForm.setText("Foo, bar, baz");
		verifyComputeSize(scrolledForm);
	}

	@Test
	public void testScrollingScrolledFormText() {
		ScrolledFormText scrolledForm = new ScrolledFormText(shell, SWT.H_SCROLL | SWT.V_SCROLL, true);
		scrolledForm.setText("Foo, bar, baz");
		verifyComputeSize(scrolledForm);
	}

	@Test
	public void testScrolledPageBook() {
		ScrolledPageBook scrolledPageBook = new ScrolledPageBook(shell, SWT.NONE);
		verifyComputeSize(scrolledPageBook);
	}

	@Test
	public void testScrollingScrolledPageBook() {
		ScrolledPageBook scrolledPageBook = new ScrolledPageBook(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		verifyComputeSize(scrolledPageBook);
	}

	@Test
	public void testSection() {
		Section section = new Section(shell, SWT.NONE);
		section.setText("Hi ho he hum de da doo dum");
		verifyComputeSize(section);
	}

	@Test
	public void testScrollingSection() {
		Section section = new Section(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		section.setText("Hi ho he hum de da doo dum");
		verifyComputeSize(section);
	}

	@Test
	public void testTreeNode() {
		TreeNode treeNode = new TreeNode(shell, SWT.NONE);
		verifyComputeSize(treeNode);
	}

	@Test
	public void testScrollingTreeNode() {
		TreeNode treeNode = new TreeNode(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		verifyComputeSize(treeNode);
	}

	@Test
	public void testTwistie() {
		Twistie twistie = new Twistie(shell, SWT.NONE);
		verifyComputeSize(twistie);
	}

	@Test
	public void testScrollingTwistie() {
		Twistie twistie = new Twistie(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		verifyComputeSize(twistie);
	}
}
